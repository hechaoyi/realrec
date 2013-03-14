package realrec.cbox.storm.utils;

public final class LangUtils {

	private LangUtils() {
	}

	public static String property(String key, String def) {
		return System.getProperty(key, def);
	}

	public static int property(String key, int def) {
		try {
			return Integer.valueOf(System.getProperty(key), 10);
		} catch (NumberFormatException e) {
			return def;
		}
	}

}
