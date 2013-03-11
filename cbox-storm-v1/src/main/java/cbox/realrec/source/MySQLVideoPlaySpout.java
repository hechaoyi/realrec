package cbox.realrec.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class MySQLVideoPlaySpout extends BaseRichSpout {
	private static final long serialVersionUID = 3905325943237475222L;
	private SpoutOutputCollector collector;
	private SqlSessionFactory sessionFactory;
	private Queue<VideoPlay> buffer = Queues.newConcurrentLinkedQueue();
	private Map<Long, VideoPlay> acks = Maps.newConcurrentMap();
	private static final int BATCH_SIZE = 10;
	private long index = 0;

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context,
			SpoutOutputCollector collector) {
		this.collector = collector;
		try (InputStream is = Resources
				.getResourceAsStream("mybatis-config.xml")) {
			sessionFactory = new SqlSessionFactoryBuilder().build(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void nextTuple() {
		if (buffer.isEmpty())
			tryFillBuffer();
		VideoPlay item = buffer.poll();
		if (item != null) {
			collector.emit(item.toList(), item.getId());
		} else {
			Utils.sleep(100);
		}
	}

	private synchronized void tryFillBuffer() {
		if (index == -1) {
			Utils.sleep(60000);
			return;
		}
		try (SqlSession session = sessionFactory.openSession()) {
			List<VideoPlay> plays = session.selectList("VideoPlay.batch",
					ImmutableMap.of("start", index, "limit", BATCH_SIZE));
			for (VideoPlay play : plays) {
				buffer.offer(play);
				acks.put(play.getId(), play);
			}
			index = plays.size() > 0 ? index + plays.size() : -1;
		}
		Utils.sleep(10000);
	}

	@Override
	public void ack(Object msgId) {
		acks.remove(msgId);
	}

	@Override
	public void fail(Object msgId) {
		VideoPlay item = acks.remove(msgId);
		if (item != null)
			buffer.offer(item);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("client_id", "videoset_id", "video_id",
				"played_time", "type"));
	}

}
