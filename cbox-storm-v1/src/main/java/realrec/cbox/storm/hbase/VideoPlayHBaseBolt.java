package realrec.cbox.storm.hbase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

public class VideoPlayHBaseBolt extends BaseBasicBolt {

	private static final long serialVersionUID = -5163932866345752592L;
	private static final byte[] columnFamily = "cf".getBytes();
	private HTable table;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TopologyContext context) {
		try {
			Configuration config = HBaseConfiguration.create();
			table = new HTable(config, "cbox_user_video_preferences");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void cleanup() {
		try {
			if (table != null)
				table.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		try {
			long userId = input.getLongByField("user_id");
			long videoSetId = input.getLongByField("videoset_id");
			long videoId = input.getLongByField("video_id");
			float preference = input.getFloatByField("preference");
			byte[] row = rowKey(userId, videoSetId);
			byte[] col = Bytes.toBytes(videoId + ":played");
			Float lastPref = get(row, col);
			if (lastPref == null || preference > lastPref) {
				put(row, col, preference);
				collector.emit(Arrays.<Object> asList(userId, videoSetId));
			}
		} catch (IOException e) {
			throw new FailedException(e);
		}
	}

	private byte[] rowKey(long userId, long videoSetId) {
		byte[] row = new byte[Bytes.SIZEOF_LONG * 2];
		Bytes.putLong(row, 0, userId);
		Bytes.putLong(row, Bytes.SIZEOF_LONG, videoSetId);
		return row;
	}

	private Float get(byte[] row, byte[] col) throws IOException {
		Get get = new Get(row);
		get.addColumn(columnFamily, col);
		byte[] value = table.get(get).value();
		if (value == null)
			return null;
		return Bytes.toFloat(value);
	}

	private void put(byte[] row, byte[] col, float value) throws IOException {
		Put put = new Put(row);
		put.add(columnFamily, col, Bytes.toBytes(value));
		table.put(put);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("user_id", "videoset_id"));
	}

}
