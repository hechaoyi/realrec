package realrec.cbox.metadata.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.ErrorReply;
import realrec.common.protocol.reply.StatusReply;

public class MetaDataRequestHandler extends
		ChannelInboundMessageHandlerAdapter<Command> {

	private static final Logger log = LoggerFactory
			.getLogger(MetaDataRequestHandler.class);

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Command msg)
			throws Exception {
		ctx.write(StatusReply.OK);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.debug("network/codec error", cause);
		if (ctx.channel().isActive()) {
			ctx.write(new ErrorReply(cause.getMessage())).addListener(
					ChannelFutureListener.CLOSE);
		} else {
			ctx.close();
		}
	}

}
