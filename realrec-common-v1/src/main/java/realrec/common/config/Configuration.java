package realrec.common.config;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public abstract class Configuration {

	public abstract void initialize() throws Exception;

	public static <T extends Configuration> T bootstrap(String[] args,
			Class<T> confCls) throws IOException {
		ObjectMapper om = new ObjectMapper(new YAMLFactory());
		String configFileName = "default.yaml";
		if (args.length >= 2 && "-f".equals(args[0]))
			configFileName = args[1];
		File configFile = new File(configFileName);
		if (configFile.exists())
			return om.readValue(configFile, confCls);
		return om.readValue(confCls.getClassLoader()
				.getResource(configFileName), confCls);
	}

}
