package realrec.cbox.restful.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import net.myrrix.client.ClientRecommender;
import net.myrrix.client.MyrrixClientConfiguration;
import net.myrrix.common.MyrrixRecommender;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import realrec.cbox.restful.resource.RecommendResult;

@Service
public class RecommendService {

	private MyrrixRecommender recommender;
	@Value("${myrrix.host}")
	private String host;
	@Value("${myrrix.port}")
	private int port;

	@PostConstruct
	public void init() throws IOException {
		MyrrixClientConfiguration mc = new MyrrixClientConfiguration();
		mc.setHost(host);
		mc.setPort(port);
		recommender = new ClientRecommender(mc);
	}

	public RecommendResult recommend(long userid, int start, int limit)
			throws Exception {
		List<RecommendedItem> items = recommender.recommend(userid, start
				+ limit);
		RecommendResult result = new RecommendResult();
		for (int i = start; i < items.size() && i < start + limit; i++)
			result.getItems().add(items.get(i));
		return result;
	}

}
