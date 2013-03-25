package realrec.cbox.restful.resource;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import realrec.cbox.restful.service.RecommendService;

@Component
@Path("/rec")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendResource {

	private static final Logger log = LoggerFactory
			.getLogger(RecommendResource.class);
	@Autowired
	private RecommendService service;

	@GET
	public RecommendResult recommend(@QueryParam("userid") long userid,
			@DefaultValue("0") @QueryParam("start") int start,
			@DefaultValue("10") @QueryParam("limit") int limit) {
		if (userid <= 0 || start < 0 || limit <= 0)
			return new RecommendResult("parameters: userid,start,limit");
		try {
			return service.recommend(userid, start, limit);
		} catch (Exception e) {
			log.warn("service error", e);
			return new RecommendResult(e.getMessage());
		}
	}

}
