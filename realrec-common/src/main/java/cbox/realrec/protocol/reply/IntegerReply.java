package cbox.realrec.protocol.reply;

import static cbox.realrec.protocol.command.ByteBufUtils.CRLF;
import io.netty.buffer.ByteBuf;

public class IntegerReply implements Reply<Long> {

	public static final byte MARKER = ':';
	private Long integer;

	public IntegerReply(Long integer) {
		this.integer = integer;
	}

	@Override
	public Long data() {
		return integer;
	}

	@Override
	public void writeTo(ByteBuf out) {
		out.writeByte(MARKER);
		out.writeBytes(String.valueOf(integer).getBytes());
		out.writeBytes(CRLF);
	}

	@Override
	public String toString() {
		return "IntegerReply [integer=" + integer + "]";
	}

}
