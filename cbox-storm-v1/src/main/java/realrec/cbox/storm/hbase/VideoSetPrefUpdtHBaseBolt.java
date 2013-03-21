package realrec.cbox.storm.hbase;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import realrec.cbox.storm.utils.RunningAverage;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class VideoSetPrefUpdtHBaseBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 3975443271669534747L;
	private static final byte[] columnFamily = "cf".getBytes();
	private HTable table1;
	private HTable table2;
	private HTable table3;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TopologyContext context) {
		try {
			Configuration config = HBaseConfiguration.create();
			table1 = new HTable(config, "cbox_user_video_preferences");
			table2 = new HTable(config, "cbox_user_prefs");
			table3 = new HTable(config, "cbox_video_prefs");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cleanup() {
		try {
			if (table1 != null)
				table1.close();
			if (table2 != null)
				table2.close();
			if (table3 != null)
				table3.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		try {
			long userId = input.getLongByField("user_id");
			long videoSetId = input.getLongByField("videoset_id");
			RunningAverage avg = new RunningAverage();
			for (Map<String, Float> actions : get(userId, videoSetId).values())
				avg.addDatum(score(actions));
			float preference = (float) avg.getAverage();
			put(table2, userId, videoSetId, preference);
			put(table3, videoSetId, userId, preference);
			collector.emit(Arrays.<Object> asList(userId, videoSetId,
					preference));
		} catch (Exception e) {
			throw new FailedException(e);
		}
	}

	private Map<Long, Map<String, Float>> get(long userId, long videoSetId)
			throws IOException {
		byte[] row = new byte[Bytes.SIZEOF_LONG * 2];
		Bytes.putLong(row, 0, userId);
		Bytes.putLong(row, Bytes.SIZEOF_LONG, videoSetId);
		Get get = new Get(row);
		get.addFamily(columnFamily);
		Map<Long, Map<String, Float>> result = new HashMap<>();
		for (Entry<byte[], byte[]> data : table1.get(get)
				.getFamilyMap(columnFamily).entrySet()) {
			String col = Bytes.toString(data.getKey());
			int idx = col.indexOf(':');
			long videoId = Long.parseLong(col.substring(0, idx));
			float preference = Bytes.toFloat(data.getValue());
			String action = col.substring(idx + 1);
			if (!result.containsKey(videoId))
				result.put(videoId, new HashMap<String, Float>());
			result.get(videoId).put(action, preference);
		}
		return result;
	}

	private float score(Map<String, Float> actions) {
		return actions.get("played");
	}

	private void put(HTable table, long row, long col, float value)
			throws IOException {
		Put put = new Put(Bytes.toBytes(row));
		put.add(columnFamily, Bytes.toBytes(col), Bytes.toBytes(value));
		table.put(put);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("user_id", "videoset_id", "preference"));
	}

}
