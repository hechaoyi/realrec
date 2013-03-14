package realrec.cbox.metadata.hash;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.Hashing;

public class HashService {

	private BiMap<String, Long> originToId = HashBiMap.create();
	private Map<Long, String> idToOrigin = originToId.inverse();

	public long hash(String origin) {
		synchronized (this) {
			if (originToId.containsKey(origin))
				return originToId.get(origin);
			long id = doHash(origin);
			originToId.put(origin, id);
			return id;
		}
	}

	public String show(long id) {
		synchronized (this) {
			return idToOrigin.get(id);
		}
	}

	private long doHash(String origin) {
		long id = Hashing.md5().hashString(origin).padToLong();
		if (id < 0)
			id = id >>> 1;
		while (originToId.containsValue(id) && id <= Long.MAX_VALUE)
			id++;
		return id;
	}

	private static final HashService instance = new HashService();

	public static HashService instance() {
		return instance;
	}

}
