package realrec.cbox.storm.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.cbox.storm.hbase.VideoPlayHBaseBolt;
import realrec.cbox.storm.hbase.VideoSetPrefUpdtHBaseBolt;
import realrec.cbox.storm.proc.VideoPlayNormalizeBolt;
import realrec.cbox.storm.source.MySQLVideoPlaySpout;
import realrec.cbox.storm.utils.DebugBolt;
import realrec.common.config.Configuration;
import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopologyConfig extends Configuration {

	private static final Logger log = LoggerFactory
			.getLogger(TopologyConfig.class);

	@JsonProperty
	private String name = "realrec";
	@JsonProperty
	private long mysqlBatchSize = 1000;
	@JsonProperty
	private long mysqlWaitMillis = 10000;
	@JsonProperty(required = true)
	private String metadataHosts;
	@JsonProperty
	private long metadataConns = 8;
	@JsonProperty
	private long metadataThreads = 4;
	@JsonProperty
	private long hashCacheHours = 6;
	@JsonProperty
	private long detailCacheHours = 6;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMysqlBatchSize() {
		return mysqlBatchSize;
	}

	public void setMysqlBatchSize(long mysqlBatchSize) {
		this.mysqlBatchSize = mysqlBatchSize;
	}

	public long getMysqlWaitMillis() {
		return mysqlWaitMillis;
	}

	public void setMysqlWaitMillis(long mysqlWaitMillis) {
		this.mysqlWaitMillis = mysqlWaitMillis;
	}

	public String getMetadataHosts() {
		return metadataHosts;
	}

	public void setMetadataHosts(String metadataHosts) {
		this.metadataHosts = metadataHosts;
	}

	public long getMetadataConns() {
		return metadataConns;
	}

	public void setMetadataConns(long metadataConns) {
		this.metadataConns = metadataConns;
	}

	public long getMetadataThreads() {
		return metadataThreads;
	}

	public void setMetadataThreads(long metadataThreads) {
		this.metadataThreads = metadataThreads;
	}

	public long getHashCacheHours() {
		return hashCacheHours;
	}

	public void setHashCacheHours(long hashCacheHours) {
		this.hashCacheHours = hashCacheHours;
	}

	public long getDetailCacheHours() {
		return detailCacheHours;
	}

	public void setDetailCacheHours(long detailCacheHours) {
		this.detailCacheHours = detailCacheHours;
	}

	@Override
	public void initialize() throws Exception {
		log.info("configuration loaded. "
				+ "name: {}, mysqlBatchSize: {}, mysqlWaitMillis: {}, "
				+ "metadataHosts: {}, metadataConns: {}, metadataThreads: {}, "
				+ "hashCacheHours: {}, detailCacheHours: {}", name,
				mysqlBatchSize, mysqlWaitMillis, metadataHosts, metadataConns,
				metadataThreads, hashCacheHours, detailCacheHours);
	}

	public static final String MYSQL_BATCH_SIZE = "mysqlBatchSize";
	public static final String MYSQL_WAIT_MILLIS = "mysqlWaitMillis";
	public static final String METADATA_HOSTS = "metadataHosts";
	public static final String METADATA_CONNS = "metadataConns";
	public static final String METADATA_THREADS = "metadataThreads";
	public static final String HASH_CACHE_HOURS = "hashCacheHours";
	public static final String DETAIL_CACHE_HOURS = "detailCacheHours";

	public Config config() {
		Config conf = new Config();
		conf.put(MYSQL_BATCH_SIZE, mysqlBatchSize);
		conf.put(MYSQL_WAIT_MILLIS, mysqlWaitMillis);
		conf.put(METADATA_HOSTS, metadataHosts);
		conf.put(METADATA_CONNS, metadataConns);
		conf.put(METADATA_THREADS, metadataThreads);
		conf.put(HASH_CACHE_HOURS, hashCacheHours);
		conf.put(DETAIL_CACHE_HOURS, detailCacheHours);
		return conf;
	}

	public StormTopology build() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("mysql", new MySQLVideoPlaySpout(), 1);
		builder.setBolt("normalize", new VideoPlayNormalizeBolt())
				.fieldsGrouping("mysql", new Fields("video_id"));
		builder.setBolt("hbase", new VideoPlayHBaseBolt()).fieldsGrouping(
				"normalize", new Fields("user_id", "videoset_id"));
		builder.setBolt("prefUpdt", new VideoSetPrefUpdtHBaseBolt())
				.fieldsGrouping("hbase", new Fields("user_id", "videoset_id"));
		builder.setBolt("debug", new DebugBolt()).shuffleGrouping("prefUpdt");
		return builder.createTopology();
	}

}
