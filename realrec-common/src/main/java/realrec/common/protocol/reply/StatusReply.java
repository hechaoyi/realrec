package realrec.common.protocol.reply;

import static realrec.common.protocol.command.ByteBufUtils.CRLF;
import static realrec.common.protocol.command.ByteBufUtils.UTF8;
import io.netty.buffer.ByteBuf;

public class StatusReply implements Reply<String> {

	public static final byte MARKER = '+';
	public static final StatusReply OK = new StatusReply("OK");
	public static final StatusReply PONG = new StatusReply("PONG");
	private String status;

	public StatusReply(String status) {
		this.status = status;
	}

	@Override
	public String data() {
		return status;
	}

	@Override
	public void writeTo(ByteBuf out) {
		out.writeByte(MARKER);
		out.writeBytes(status.getBytes(UTF8));
		out.writeBytes(CRLF);
	}

	@Override
	public String toString() {
		return "StatusReply [status=" + status + "]";
	}

}
