package realrec.cbox.restful.data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import realrec.common.protocol.client.UnifiedClient;
import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.BulkReply;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Repository
public class MetaDataStore {

	private UnifiedClient store;
	@Value("${metadata.hosts}")
	private String hosts;
	@Value("${metadata.conns}")
	private int conns;
	@Value("${metadata.threads}")
	private int threads;
	@Value("${metadata.cache.hours}")
	private long cacheHours;
	private LoadingCache<Long, VideoSetDetail> details;
	private LoadingCache<String, Long> ids;

	@PostConstruct
	public void init() {
		store = new UnifiedClient(hosts, conns, threads);
		details = CacheBuilder.newBuilder()
				.expireAfterAccess(cacheHours, TimeUnit.HOURS)
				.build(new CacheLoader<Long, VideoSetDetail>() {
					@Override
					public VideoSetDetail load(Long key) throws Exception {
						VideoSetDetail vsd = new VideoSetDetail();
						BulkReply[] repl = (BulkReply[]) store
								.send(new Command("detail", String.valueOf(key)))
								.get().data();
						if (repl == null || repl.length < 3)
							return vsd;
						vsd.setContent_id(repl[0].data());
						vsd.setLogo(repl[1].data());
						vsd.setTitle(repl[2].data());
						vsd.setContent_type_id(3);
						return vsd;
					}
				});
		ids = CacheBuilder.newBuilder()
				.expireAfterAccess(cacheHours, TimeUnit.HOURS)
				.build(new CacheLoader<String, Long>() {
					@Override
					public Long load(String key) throws Exception {
						return (Long) store
								.send(new Command("hash", "user", key)).get()
								.data();
					}
				});
	}

	public VideoSetDetail detail(long videoSetId) throws ExecutionException {
		return details.get(videoSetId);
	}

	public long hash(String clientid) throws ExecutionException {
		return ids.get(clientid);
	}

}
