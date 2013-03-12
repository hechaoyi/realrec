package cbox.realrec.protocol.command;

import static cbox.realrec.protocol.command.ByteBufUtils.readBulk;
import static cbox.realrec.protocol.command.ByteBufUtils.readInt;
import static cbox.realrec.protocol.command.ByteBufUtils.readLine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class CommandDecoder extends ReplayingDecoder<Void> {

	public static final byte ARGS_SIZE_MARKER = '*';
	public static final byte ARG_LENGTH_MARKER = '$';

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
