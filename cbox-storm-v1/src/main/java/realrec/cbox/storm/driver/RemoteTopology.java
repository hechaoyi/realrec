package realrec.cbox.storm.driver;

import realrec.common.config.Configuration;
import backtype.storm.StormSubmitter;

public class RemoteTopology extends LocalTopology {

	public static void main(String[] args) throws Exception {
		TopologyConfig conf = Configuration.bootstrap(args,
				TopologyConfig.class);
		conf.initialize();
		StormSubmitter.submitTopology(conf.getName(), conf.config(),
				conf.build());
	}

}
