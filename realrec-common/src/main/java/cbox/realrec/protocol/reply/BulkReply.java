package cbox.realrec.protocol.reply;

import static cbox.realrec.protocol.command.ByteBufUtils.writeBulk;
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
