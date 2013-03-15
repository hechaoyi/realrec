package realrec.cbox.metadata.hash;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.cbox.metadata.bdb.BerkeleyDB;
import realrec.cbox.metadata.hash.HashRecord.Domain;

import com.google.common.hash.Hashing;

public class HashService {

	private static final Logger log = LoggerFactory
			.getLogger(HashService.class);
	private Map<Long, HashRecord> hash2rcd;
	private Map<String, HashRecord> orig2rcd;

	public HashService(BerkeleyDB bdb) {
		hash2rcd = bdb.hash2rcd();
		orig2rcd = bdb.orig2rcd();
	}

	public long hash(Domain domain, String origin) {
		if (orig2rcd.containsKey(origin))
			return orig2rcd.get(origin).getHash();
		long hash = doHash(origin);
		HashRecord hr = new HashRecord();
		hr.setHash(hash);
		hr.setOrigin(origin);
		hr.setDomain(domain);
		hash2rcd.put(hash, hr);
		return hash;
	}

	public HashRecord show(long hash) {
		return hash2rcd.get(hash);
	}

	private long doHash(String origin) {
		long hash = Hashing.md5().hashString(origin).padToLong();
		if (hash < 0)
			hash >>>= 1;
		if (hash2rcd.containsKey(hash)) {
			int offset = 1;
			while ((hash2rcd.containsKey(hash + offset) && hash + offset < Long.MAX_VALUE))
				offset++;
			log.warn("hash conflict, origin: {}, hash: {}, offset: {}", origin,
					hash, offset);
		}
		return hash;
	}

}
