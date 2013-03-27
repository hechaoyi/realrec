package realrec.cbox.metadata.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.common.config.Configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetaDataConfig extends Configuration {

	private static final Logger log = LoggerFactory
			.getLogger(MetaDataConfig.class);

	@JsonProperty(required = true)
	private File dataDir;
	@JsonProperty
	private String bind = "0.0.0.0";
	@JsonProperty
	private int port = 5587;

	public File getDataDir() {
		return dataDir;
	}

	public void setDataDir(File dataDir) {
		this.dataDir = dataDir;
	}

	public String getBind() {
		return bind;
	}

	public void setBind(String bind) {
		this.bind = bind;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void initialize() throws Exception {
		if (!dataDir.exists())
			dataDir.mkdirs();
		log.info("configuration loaded. dataDir: {}, bind: {}, port: {}",
				dataDir.getAbsolutePath(), bind, port);
	}

}
