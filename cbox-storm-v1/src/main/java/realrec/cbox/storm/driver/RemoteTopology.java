package realrec.cbox.storm.driver;

import realrec.cbox.storm.preproc.VideoPlayNormalizeBolt;
import realrec.cbox.storm.source.MySQLVideoPlaySpout;
import realrec.cbox.storm.utils.DebugBolt;
import realrec.cbox.storm.utils.LangUtils;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class RemoteTopology {

	public static final String TOPOLOGY_NAME = "topology.name";
	public static final String TOPOLOGY_WORKERS = "topology.workers";
	public static final String METADATA_HOSTS = "metadata.hosts";

	public StormTopology build() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("mysql", new MySQLVideoPlaySpout(), 1);
		builder.setBolt("normalize", new VideoPlayNormalizeBolt(), 4)
				.fieldsGrouping("mysql", new Fields("video_id"));
		builder.setBolt("debug", new DebugBolt()).shuffleGrouping("normalize");
		return builder.createTopology();
	}

	public Config config() {
		Config conf = new Config();
		conf.setNumWorkers(LangUtils.property(TOPOLOGY_WORKERS, 8));
		conf.put(METADATA_HOSTS,
				LangUtils.property(METADATA_HOSTS, "127.0.0.1:6379"));
		return conf;
	}

	public String name() {
		return LangUtils.property(TOPOLOGY_NAME, "realrec");
	}

	public static void main(String[] args) throws Exception {
		RemoteTopology topo = new RemoteTopology();
		StormSubmitter.submitTopology(topo.name(), topo.config(), topo.build());
	}

}
