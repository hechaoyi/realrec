package realrec.cbox.restful.resource;

import java.util.List;

import realrec.cbox.restful.data.VideoSetDetail;

public class RecommendResult {

	private boolean ok;
	private String msg;
	private List<VideoSetDetail> items;

	public RecommendResult(List<VideoSetDetail> items) {
		this.ok = true;
		this.msg = "ok";
		this.items = items;
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

	public List<VideoSetDetail> getItems() {
		return items;
	}

	public void setItems(List<VideoSetDetail> items) {
		this.items = items;
	}

}
