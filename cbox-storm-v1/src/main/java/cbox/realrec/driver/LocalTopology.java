package cbox.realrec.driver;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.utils.Utils;
import cbox.realrec.utils.LangUtils;

public class LocalTopology extends RemoteTopology {

	public static final String TOPOLOGY_EXECUTE_SECONDS = "topology.execute.seconds";

	public Config config() {
		Config conf = new Config();
		// conf.setDebug(true);
		return conf;
	}

	public static void main(String[] args) {
		LocalTopology topo = new LocalTopology();
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(topo.name(), topo.config(), topo.build());
		Utils.sleep(LangUtils.property(TOPOLOGY_EXECUTE_SECONDS, 600) * 1000L);
		cluster.killTopology(topo.name());
		cluster.shutdown();
	}

}