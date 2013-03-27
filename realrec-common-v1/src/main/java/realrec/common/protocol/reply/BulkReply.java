package realrec.common.protocol.reply;

import static realrec.common.protocol.command.ByteBufUtils.writeBulk;
import io.netty.buffer.ByteBuf;

public class BulkReply implements Reply<String> {

	public static final byte MARKER = '$';
	private String bulk;

	public BulkReply(String bulk) {
		this.bulk = bulk;
	}

	@Override
	public String data() {
		return bulk;
	}

	@Override
	public void writeTo(ByteBuf out) {
		out.writeByte(MARKER);
		writeBulk(out, bulk);
	}

	@Override
	public String toString() {
		return "BulkReply [bulk=" + bulk + "]";
	}

}
