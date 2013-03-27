package realrec.cbox.restful.data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import realrec.common.protocol.client.UnifiedClient;
import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.MultiBulkReply;

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
	@Value("${metadata.detail.cache.hours}")
	private long detailsCacheHours;
	private LoadingCache<String, VideoSetDetail> details;

	@PostConstruct
	public void init() {
		store = new UnifiedClient(hosts, conns, threads);
		details = CacheBuilder.newBuilder()
				.expireAfterAccess(detailsCacheHours, TimeUnit.HOURS)
				.build(new CacheLoader<String, VideoSetDetail>() {
					@Override
					public VideoSetDetail load(String key) throws Exception {
						VideoSetDetail vsd = new VideoSetDetail(key);
						MultiBulkReply repl = (MultiBulkReply) store.send(
								new Command("detail", key)).get();
						if (repl.data() == null || repl.data().length < 2)
							return vsd;
						vsd.setLogo((String) repl.data()[0].data());
						vsd.setTitle((String) repl.data()[1].data());
						vsd.setContent_type_id(3);
						return vsd;
					}
				});
	}

	public VideoSetDetail detail(String videoSetId) throws ExecutionException {
		return details.get(videoSetId);
	}

}
