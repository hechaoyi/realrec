package realrec.cbox.restful.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;
import net.myrrix.common.MyrrixRecommender;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import realrec.cbox.restful.data.MetaDataStore;
import realrec.cbox.restful.data.VideoSetDetail;
import realrec.cbox.restful.resource.RecommendResult;

@Service
public class RecommendService {

	public static enum UserIdType {
		clientid, userid
	}

	private MyrrixRecommender recommender;
	@Value("${myrrix.host}")
	private String host;
	@Value("${myrrix.port}")
	private int port;
	@Autowired
	private MetaDataStore metadata;

	@PostConstruct
	public void init() throws IOException {
		MyrrixClientConfiguration mc = new MyrrixClientConfiguration();
		mc.setHost(host);
		mc.setPort(port);
		recommender = new ClientRecommender(mc);
	}

	public RecommendResult recommend(UserIdType type, String id, int start,
			int limit) throws Exception {
		long userid = metadata.hash(id);
		List<RecommendedItem> items = recommender.recommend(userid, start
				+ limit);
		List<VideoSetDetail> vsds = new ArrayList<>();
		for (int i = start; i < items.size() && i < start + limit; i++)
			vsds.add(metadata.detail(items.get(i).getItemID()));
		return new RecommendResult(vsds);
	}

}
