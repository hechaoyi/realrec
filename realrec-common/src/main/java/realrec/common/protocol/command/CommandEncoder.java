package realrec.common.protocol.command;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CommandEncoder extends MessageToByteEncoder<Command> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out)
			throws Exception {
		msg.writeTo(out);
	}

}
