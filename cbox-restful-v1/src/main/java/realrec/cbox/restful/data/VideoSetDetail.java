package realrec.cbox.restful.data;

public class VideoSetDetail {

	private String content_id;
	private String logo;
	private String title;
	private int content_type_id;

	public VideoSetDetail(String content_id) {
		this.content_id = content_id;
	}

	public String getContent_id() {
		return content_id;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getContent_type_id() {
		return content_type_id;
	}

	public void setContent_type_id(int content_type_id) {
		this.content_type_id = content_type_id;
	}

}
