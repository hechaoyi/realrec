package cbox.realrec.protocol.reply;

import static cbox.realrec.protocol.command.ByteBufUtils.CRLF;
import static cbox.realrec.protocol.command.ByteBufUtils.UTF8;
import io.netty.buffer.ByteBuf;

public class ErrorReply implements Reply<String> {

	public static final byte MARKER = '-';
	private String error;

	public ErrorReply(String error) {
		this.error = error;
	}

	@Override
	public String data() {
		return error;
	}

	@Override
	public void writeTo(ByteBuf out) {
		out.writeByte(MARKER);
		out.writeBytes(error.getBytes(UTF8));
		out.writeBytes(CRLF);
	}

	@Override
	public String toString() {
		return "ErrorReply [error=" + error + "]";
	}

}
