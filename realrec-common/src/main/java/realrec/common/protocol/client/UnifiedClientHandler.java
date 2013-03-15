package realrec.common.protocol.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import realrec.common.protocol.reply.Reply;

public class UnifiedClientHandler extends
		ChannelInboundMessageHandlerAdapter<Reply<?>> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Reply<?> msg)
			throws Exception {
		Promise<Reply<?>> promise = ctx.channel()
				.attr(UnifiedClient.PROMISE_QUEUE).get().poll();
		if (promise != null)
			promise.set(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		Promise<Reply<?>> promise = ctx.channel()
				.attr(UnifiedClient.PROMISE_QUEUE).get().poll();
		if (promise != null)
			promise.setException(cause);
	}

}
