package realrec.cbox.storm.notice;

import static realrec.cbox.storm.driver.TopologyConfig.MYRRIX_HOST;
import static realrec.cbox.storm.driver.TopologyConfig.MYRRIX_PORT;

import java.io.IOException;
import java.util.Map;

import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;
import net.myrrix.common.MyrrixRecommender;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

public class UserPreferenceMyrrixBolt extends BaseBasicBolt {

	private static final long serialVersionUID = 4118165591913396843L;
	private MyrrixRecommender recommender;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TopologyContext context) {
		try {
			MyrrixClientConfiguration mc = new MyrrixClientConfiguration();
			mc.setHost((String) conf.get(MYRRIX_HOST));
			mc.setPort((int) (long) conf.get(MYRRIX_PORT));
			recommender = new ClientRecommender(mc);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
		try {
			long userId = input.getLongByField("user_id");
			long videoSetId = input.getLongByField("videoset_id");
			float preference = input.getFloatByField("preference");
			recommender.setPreference(userId, videoSetId, preference);
		} catch (Exception e) {
			throw new FailedException(e);
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

}
