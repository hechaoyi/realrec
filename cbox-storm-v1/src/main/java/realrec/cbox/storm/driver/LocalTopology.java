package realrec.cbox.storm.driver;

import realrec.common.config.Configuration;
import backtype.storm.LocalCluster;

public class LocalTopology {

	public static void main(String[] args) throws Exception {
		TopologyConfig conf = Configuration.bootstrap(args,
				TopologyConfig.class);
		conf.initialize();
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(conf.getName(), conf.config(), conf.build());
	}

}
