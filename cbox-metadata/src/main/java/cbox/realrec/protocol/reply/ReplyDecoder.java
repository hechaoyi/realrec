package cbox.realrec.protocol.reply;

import static cbox.realrec.protocol.command.ByteBufUtils.readBulk;
import static cbox.realrec.protocol.command.ByteBufUtils.readInt;
import static cbox.realrec.protocol.command.ByteBufUtils.readLine;
import static cbox.realrec.protocol.command.ByteBufUtils.readLong;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class ReplyDecoder extends ReplayingDecoder<Void> {

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		switch (in.readByte()) {
		case StatusReply.MARKER:
			return new StatusReply(readLine(in));
		case ErrorReply.MARKER:
			return new ErrorReply(readLine(in));
		case IntegerReply.MARKER:
			return new IntegerReply(readLong(in));
		case BulkReply.MARKER:
			return new BulkReply(readBulk(in));
		case MultiBulkReply.MARKER:
			int size = readInt(in);
			if (size < 0)
				return new MultiBulkReply(null);
			Reply<?>[] repls = new Reply<?>[size];
			for (int i = 0; i < size; i++)
				repls[i] = (Reply<?>) decode(ctx, in);
			return new MultiBulkReply(repls);
		default:
			throw new IllegalArgumentException("unexpected character");
		}
	}

}
