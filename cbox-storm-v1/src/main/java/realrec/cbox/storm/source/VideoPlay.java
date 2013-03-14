package realrec.cbox.storm.source;

import java.util.Arrays;
import java.util.List;

public class VideoPlay {

	public enum Type {
		vod, p2p, lba
	}

	private long id;
	private String videoset_id;
	private String video_id;
	private String played_time;
	private String client_id;
	private String type;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getVideoset_id() {
		return videoset_id;
	}

	public void setVideoset_id(String videoset_id) {
		this.videoset_id = videoset_id;
	}

	public String getVideo_id() {
		return video_id;
	}

	public void setVideo_id(String video_id) {
		this.video_id = video_id;
	}

	public String getPlayed_time() {
		return played_time;
	}

	public void setPlayed_time(String played_time) {
		this.played_time = played_time;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Object> toList() {
		return Arrays.<Object> asList(client_id, videoset_id, video_id,
				played_time, type);
	}

	@Override
	public String toString() {
		return "VideoPlay [id=" + id + ", videoset_id=" + videoset_id
				+ ", video_id=" + video_id + ", played_time=" + played_time
				+ ", client_id=" + client_id + ", type=" + type + "]";
	}

}
