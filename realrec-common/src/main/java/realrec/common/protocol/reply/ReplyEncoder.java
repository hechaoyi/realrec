package realrec.common.protocol.reply;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ReplyEncoder extends MessageToByteEncoder<Reply<?>> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Reply<?> msg, ByteBuf out)
			throws Exception {
		msg.writeTo(out);
	}

}
