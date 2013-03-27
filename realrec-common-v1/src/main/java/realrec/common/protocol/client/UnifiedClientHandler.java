package realrec.common.protocol.client;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.util.AttributeKey;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.common.protocol.reply.Reply;

@Sharable
public class UnifiedClientHandler extends
		ChannelInboundMessageHandlerAdapter<Reply<?>> {

	private static final Logger log = LoggerFactory
			.getLogger(UnifiedClientHandler.class);
	private static final ChannelException CLOSED = new ChannelException(
			"remote closed this channel");
	static final AttributeKey<Queue<Promise<Reply<?>>>> PROMISE_QUEUE = new AttributeKey<>(
			"PromiseQueue");

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().attr(PROMISE_QUEUE)
				.set(new ConcurrentLinkedQueue<Promise<Reply<?>>>());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Reply<?> msg)
			throws Exception {
		Promise<Reply<?>> promise = ctx.channel().attr(PROMISE_QUEUE).get()
				.poll();
		if (promise != null)
			promise.set(msg);
		else
			log.debug("resp received, but no req: {}", msg);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Queue<Promise<Reply<?>>> queue = ctx.channel().attr(PROMISE_QUEUE)
				.get();
		Promise<Reply<?>> promise = null;
		while ((promise = queue.poll()) != null)
			promise.setException(CLOSED);
		ctx.channel().attr(PROMISE_QUEUE).remove();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.debug("network error", cause);
		Promise<Reply<?>> promise = ctx.channel().attr(PROMISE_QUEUE).get()
				.poll();
		if (promise != null)
			promise.setException(cause);
		ctx.channel().close();
	}

}
