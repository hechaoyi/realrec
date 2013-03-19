package realrec.cbox.metadata.bdb;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import realrec.cbox.metadata.hash.HashRecord;
import realrec.cbox.metadata.hash.HashRecord.Domain;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Durability;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class BerkeleyDB implements Closeable {

	public static final String HASH_RECORD_DBNAME = "hash_record";
	private Environment env;
	private Database hashRcdDB;
	private SecondaryDatabase hashRcdDBOrigIdx;
	private Map<Long, HashRecord> hash2rcd;
	private Map<String, HashRecord> orig2rcd;

	private BerkeleyDB(File envHome) {
		EnvironmentConfig envCfg = new EnvironmentConfig().setAllowCreate(true)
				.setTransactional(true);
		envCfg.setDurability(Durability.COMMIT_WRITE_NO_SYNC);
		env = new Environment(envHome, envCfg);

		hashRcdDB = env.openDatabase(null, HASH_RECORD_DBNAME,
				new DatabaseConfig().setAllowCreate(true)
						.setTransactional(true));
		SecondaryConfig secCfg = new SecondaryConfig()
				.setKeyCreator(hashRcdDBOrigIdxCreator);
		secCfg.setAllowCreate(true).setTransactional(true);
		hashRcdDBOrigIdx = env.openSecondaryDatabase(null, HASH_RECORD_DBNAME,
				hashRcdDB, secCfg);

		hash2rcd = new StoredMap<>(hashRcdDB, new LongBinding(),
				hashRcdBinding, true);
		orig2rcd = new StoredMap<>(hashRcdDBOrigIdx, new StringBinding(),
				hashRcdBinding, true);
	}

	@Override
	public void close() throws IOException {
		hashRcdDBOrigIdx.close();
		hashRcdDB.close();
		env.close();
	}

	public Map<Long, HashRecord> hash2rcd() {
		return hash2rcd;
	}

	public Map<String, HashRecord> orig2rcd() {
		return orig2rcd;
	}

	private TupleBinding<HashRecord> hashRcdBinding = new TupleBinding<HashRecord>() {
		@Override
		public HashRecord entryToObject(TupleInput input) {
			HashRecord hr = new HashRecord();
			hr.setHash(input.readLong());
			hr.setOrigin(input.readString());
			hr.setDomain(Domain.values()[input.readInt()]);
			return hr;
		}

		@Override
		public void objectToEntry(HashRecord object, TupleOutput output) {
			output.writeLong(object.getHash());
			output.writeString(object.getOrigin());
			output.writeInt(object.getDomain().ordinal());
		}

	};

	private SecondaryKeyCreator hashRcdDBOrigIdxCreator = new SecondaryKeyCreator() {
		@Override
		public boolean createSecondaryKey(SecondaryDatabase secondary,
				DatabaseEntry key, DatabaseEntry data, DatabaseEntry result) {
			try (TupleInput input = new TupleInput(data.getData())) {
				input.readLong();
				StringBinding.stringToEntry(input.readString(), result);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	};

	private static BerkeleyDB instance = null;

	public static BerkeleyDB instance(File envHome) {
		if (instance == null) {
			synchronized (BerkeleyDB.class) {
				if (instance == null)
					instance = new BerkeleyDB(envHome);
			}
		}
		return instance;
	}

}
