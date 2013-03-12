package cbox.realrec.protocol.reply;

import static cbox.realrec.protocol.command.ByteBufUtils.CRLF;
import static cbox.realrec.protocol.command.ByteBufUtils.NEGCRLF;
import static cbox.realrec.protocol.command.ByteBufUtils.UTF8;
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
		if (bulk == null) {
			out.writeBytes(NEGCRLF);
		} else {
			byte[] bytes = bulk.getBytes(UTF8);
			out.writeBytes(String.valueOf(bytes.length).getBytes());
			out.writeBytes(CRLF);
			out.writeBytes(bytes);
			out.writeBytes(CRLF);
		}
	}

	@Override
	public String toString() {
		return "BulkReply [bulk=" + bulk + "]";
	}

}
