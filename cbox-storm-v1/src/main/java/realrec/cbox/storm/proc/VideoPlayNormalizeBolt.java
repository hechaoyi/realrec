package realrec.cbox.storm.proc;

import static realrec.cbox.storm.driver.TopologyConfig.DETAIL_CACHE_HOURS;
import static realrec.cbox.storm.driver.TopologyConfig.HASH_CACHE_HOURS;
import static realrec.cbox.storm.driver.TopologyConfig.METADATA_CONNS;
import static realrec.cbox.storm.driver.TopologyConfig.METADATA_HOSTS;
import static realrec.cbox.storm.driver.TopologyConfig.METADATA_THREADS;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import realrec.cbox.storm.source.VideoPlay.Type;
import realrec.common.protocol.client.UnifiedClient;
import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.BulkReply;
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

public class VideoPlayNormalizeBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 5630048289401669758L;
	private UnifiedClient metadata;
	private LoadingCache<String, Long> hashes;
	private LoadingCache<String, Integer> lengths;
	private PeriodFormatter periodFormatter;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TopologyContext context) {
		metadata = new UnifiedClient((String) conf.get(METADATA_HOSTS),
				(int) (long) conf.get(METADATA_CONNS),
				(int) (long) conf.get(METADATA_THREADS));

		hashes = CacheBuilder
				.newBuilder()
				.expireAfterAccess((long) conf.get(HASH_CACHE_HOURS),
						TimeUnit.HOURS).build(new CacheLoader<String, Long>() {
					@Override
					public Long load(String key) throws Exception {
						int idx = key.indexOf(':');
						String domain = key.substring(0, idx);
						String origin = key.substring(idx + 1);
						IntegerReply reply = (IntegerReply) metadata.send(
								new Command("hash", domain, origin)).get();
						return reply.data();
					}
				});
		lengths = CacheBuilder
				.newBuilder()
				.expireAfterAccess((long) conf.get(DETAIL_CACHE_HOURS),
						TimeUnit.HOURS)
				.build(new CacheLoader<String, Integer>() {
					@Override
					public Integer load(String key) throws Exception {
						BulkReply reply = (BulkReply) metadata.send(
								new Command("length", key)).get();
						return seconds(reply.data());
					}
				});
		periodFormatter = new PeriodFormatterBuilder().appendHours()
				.appendSeparator(":").appendMinutes().appendSeparator(":")
				.appendSeconds().toFormatter();
	}

	@Override
	public void cleanup() {
		try {
			if (metadata != null)
				metadata.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		try {
			String userId = input.getStringByField("client_id");
			String videoSetId = input.getStringByField("videoset_id");
			String videoId = input.getStringByField("video_id");
			int playedTime = seconds(input.getStringByField("played_time"));
			if (Type.valueOf(input.getStringByField("type")) == Type.vod) {
				int videoTime = lengths.get(videoId);
				collector.emit(Arrays.<Object> asList(hash("user", userId),
						hash("videoset", videoSetId), hash("video", videoId),
						scoreVOD(playedTime, videoTime)));
			} else {
				collector.emit(Arrays.<Object> asList(hash("user", userId),
						hash("videoset", videoSetId), hash("video", videoId),
						scoreP2P(playedTime)));
			}
		} catch (Exception e) {
			throw new FailedException(e);
		}
	}

	private long hash(String domain, String origin) throws ExecutionException {
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

	private float scoreVOD(int playedTime, int videoTime) {
		if (videoTime == 0)
			return 0;
		double score = (double) playedTime / videoTime;
		// 对短视频降权，长视频提权，取值区间为0.6 ~ 1.2
		// 30秒视频系数为0.619，5分钟视频系数为0.751，1个小时视频系数为1.092，3个小时视频系数为1.16
		double factor = Math.atan(videoTime / 600.0 + 1) / (Math.PI / 2) * 1.1;
		if (score * factor <= 1)
			score = score * factor;
		return (float) (score < 0 ? 0 : (score > 1 ? 1 : score));
	}

	private float scoreP2P(int playedTime) {
		// 取值范围0.0 ~ 1.0
		// 播放5分钟有0.156分，播放20分钟有0.5分，播放1个小时有0.795分，播放2个小时有0.895分，播放3个小时有0.93分
		double score = Math.atan(playedTime / 1200.0) / (Math.PI / 2);
		return (float) (score < 0 ? 0 : (score > 1 ? 1 : score));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("user_id", "videoset_id", "video_id",
				"preference"));
	}

}
