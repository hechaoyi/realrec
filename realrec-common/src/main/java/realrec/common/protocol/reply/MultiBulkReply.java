package realrec.common.protocol.reply;

import static realrec.common.protocol.command.ByteBufUtils.CRLF;
import static realrec.common.protocol.command.ByteBufUtils.NEGCRLF;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class MultiBulkReply implements Reply<Reply<?>[]> {

	public static final byte MARKER = '*';
	private Reply<?>[] repls;

	public MultiBulkReply(Reply<?>[] repls) {
		this.repls = repls;
	}

	@Override
	public Reply<?>[] data() {
		return repls;
	}

	@Override
	public void writeTo(ByteBuf out) {
		out.writeByte(MARKER);
		if (repls == null) {
			out.writeBytes(NEGCRLF);
		} else {
			out.writeBytes(String.valueOf(repls.length).getBytes());
			out.writeBytes(CRLF);
			for (Reply<?> repl : repls)
				repl.writeTo(out);
		}
	}

	@Override
	public String toString() {
		return "MultiBulkReply [repls=" + Arrays.toString(repls) + "]";
	}

}
