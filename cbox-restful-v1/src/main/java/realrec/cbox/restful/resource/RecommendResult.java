package realrec.cbox.restful.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;

public class RecommendResult {

	private boolean ok;
	private String msg;
	private List<RecommendedItem> items;

	public RecommendResult() {
		this.ok = true;
		this.msg = "ok";
		this.items = new ArrayList<>();
	}

	public RecommendResult(String errorMsg) {
		this.ok = false;
		this.msg = errorMsg;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<RecommendedItem> getItems() {
		return items;
	}

	public void setItems(List<RecommendedItem> items) {
		this.items = items;
	}

}
