package realrec.cbox.storm.driver;

import realrec.cbox.storm.source.MySQLVideoPlaySpout;
import realrec.cbox.storm.utils.DebugBolt;
import realrec.cbox.storm.utils.LangUtils;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;

public class RemoteTopology {

	public static final String TOPOLOGY_NAME = "topology.name";
	public static final String TOPOLOGY_WORKERS = "topology.workers";

	public StormTopology build() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("mysql", new MySQLVideoPlaySpout(), 1);
		builder.setBolt("debug", new DebugBolt()).shuffleGrouping("mysql");
		return builder.createTopology();
	}

	public Config config() {
		Config conf = new Config();
		conf.setNumWorkers(LangUtils.property(TOPOLOGY_WORKERS, 8));
		return conf;
	}

	public String name() {
		return System.getProperty(TOPOLOGY_NAME, "realrec");
	}

	public static void main(String[] args) throws Exception {
		RemoteTopology topo = new RemoteTopology();
		StormSubmitter.submitTopology(topo.name(), topo.config(), topo.build());
	}

}
