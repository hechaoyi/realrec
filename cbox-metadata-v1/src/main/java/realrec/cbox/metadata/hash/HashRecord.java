package realrec.cbox.metadata.hash;

public class HashRecord {

	public static enum Domain {
		user, videoset, video
	}

	private long hash;
	private String origin;
	private Domain domain;

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
