package realrec.cbox.storm.preproc;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import realrec.cbox.storm.driver.RemoteTopology;
import realrec.common.protocol.client.UnifiedClient;
import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.IntegerReply;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

public class VideoPlayNormalizeBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 5630048289401669758L;
	private UnifiedClient metadata;
	private LoadingCache<String, Long> hashes;
	private PeriodFormatter periodFormatter;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context) {
		metadata = new UnifiedClient(
				(String) stormConf.get(RemoteTopology.METADATA_HOSTS), 8, 4);
		hashes = CacheBuilder.newBuilder().expireAfterAccess(6, TimeUnit.HOURS)
				.build(new CacheLoader<String, Long>() {
					@Override
					public Long load(String key) throws Exception {
						int idx = key.indexOf(':');
						String domain = key.substring(0, idx);
						String origin = key.substring(idx + 1);
						IntegerReply reply = (IntegerReply) metadata.send(
								new Command(new String[] { "hash", domain,
										origin })).get();
						return reply.data();
					}
				});
		periodFormatter = new PeriodFormatterBuilder().appendHours()
				.appendSeparator(":").appendMinutes().appendSeparator(":")
				.appendSeconds().toFormatter();
	}

	@Override
	public void cleanup() {
		Closeables.closeQuietly(metadata);
	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		try {
			long userId = hash("user", input.getStringByField("client_id"));
			long videoSetId = hash("videoset",
					input.getStringByField("videoset_id"));
			long videoId = hash("video", input.getStringByField("video_id"));
			int playedTime = seconds(input.getStringByField("played_time"));
			String type = input.getStringByField("type");
			collector.emit(Lists.<Object> newArrayList(userId, videoSetId,
					videoId, playedTime, type));
		} catch (Exception e) {
			throw new FailedException(e);
		}
	}

	public long hash(String domain, String origin) throws ExecutionException {
		return hashes.get(domain + ":" + origin);
	}

	private int seconds(String input) {
		try {
			return Period.parse(input, periodFormatter).toStandardSeconds()
					.getSeconds();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("user_id", "videoset_id", "video_id",
				"preference", "video_type"));
	}

}
