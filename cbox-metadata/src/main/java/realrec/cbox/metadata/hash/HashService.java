package realrec.cbox.metadata.hash;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.Hashing;

public class HashService implements Closeable {

	private static final Logger log = LoggerFactory
			.getLogger(HashService.class);
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
		if (originToId.containsValue(id)) {
			int offset = 1;
			while ((originToId.containsValue(id + offset) && id + offset < Long.MAX_VALUE))
				offset++;
			log.warn("hash conflict, origin: {}, hash: {}, offset: {}", origin,
					id, offset);
		}
		return id;
	}

	private static final HashService instance = new HashService();

	public static HashService init() {
		return instance;
	}

	@Override
	public void close() throws IOException {

	}

}
