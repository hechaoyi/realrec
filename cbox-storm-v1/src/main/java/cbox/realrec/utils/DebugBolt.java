package cbox.realrec.utils;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class DebugBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 4186490815247609011L;

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		System.out.println(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

}
