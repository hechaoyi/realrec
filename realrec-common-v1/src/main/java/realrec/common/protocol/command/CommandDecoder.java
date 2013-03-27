package realrec.common.protocol.command;

import static realrec.common.protocol.command.ByteBufUtils.readBulk;
import static realrec.common.protocol.command.ByteBufUtils.readInt;
import static realrec.common.protocol.command.ByteBufUtils.readLine;
import static realrec.common.protocol.command.Command.ARGS_SIZE_MARKER;
import static realrec.common.protocol.command.Command.ARG_LENGTH_MARKER;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class CommandDecoder extends ReplayingDecoder<Void> {

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		if (in.readByte() == ARGS_SIZE_MARKER) {
			int size = readInt(in);
			if (size <= 0)
				throw new IllegalArgumentException("invalid arguments size");
			String[] unified = new String[size];
			for (int i = 0; i < size; i++) {
				if (in.readByte() != ARG_LENGTH_MARKER)
					throw new IllegalArgumentException("unexpected character");
				unified[i] = readBulk(in);
			}
			return new Command(unified);
		} else {
			in.readerIndex(in.readerIndex() - 1);
			String inline = readLine(in).trim();
			if (!inline.isEmpty())
				return new Command(inline);
			checkpoint();
			return null;
		}
	}

}
