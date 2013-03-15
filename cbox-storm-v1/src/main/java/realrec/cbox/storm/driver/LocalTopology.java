package realrec.cbox.storm.driver;

import realrec.cbox.storm.utils.LangUtils;
import backtype.storm.LocalCluster;
import backtype.storm.utils.Utils;

public class LocalTopology extends RemoteTopology {

	public static final String TOPOLOGY_EXECUTE_SECONDS = "topology.execute.seconds";

	public static void main(String[] args) {
		LocalTopology topo = new LocalTopology();
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(topo.name(), topo.config(), topo.build());
		Utils.sleep(LangUtils.property(TOPOLOGY_EXECUTE_SECONDS, 600) * 1000L);
		cluster.killTopology(topo.name());
		cluster.shutdown();
	}

}
