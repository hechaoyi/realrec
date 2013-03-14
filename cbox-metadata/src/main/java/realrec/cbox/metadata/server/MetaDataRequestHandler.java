package realrec.cbox.metadata.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.cbox.metadata.hash.HashService;
import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.BulkReply;
import realrec.common.protocol.reply.ErrorReply;
import realrec.common.protocol.reply.IntegerReply;
import realrec.common.protocol.reply.MultiBulkReply;
import realrec.common.protocol.reply.StatusReply;

public class MetaDataRequestHandler extends
		ChannelInboundMessageHandlerAdapter<Command> {

	private static final Logger log = LoggerFactory
			.getLogger(MetaDataRequestHandler.class);
	private HashService hash;

	public MetaDataRequestHandler(HashService hash) {
		this.hash = hash;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Command msg)
			throws Exception {
		try {
			String[] tokens = msg.tokens();
			if (tokens.length == 0)
				return;
			switch (tokens[0].toLowerCase()) {
			case "hash":
				if (tokens.length == 1) {
					ctx.write(new ErrorReply(
							"expect more parameters: hash <origin>"));
				} else if (tokens.length == 2) {
					long id = hash.hash(tokens[1]);
					ctx.write(new IntegerReply(id));
				} else {
					IntegerReply[] repls = new IntegerReply[tokens.length - 1];
					for (int i = 1; i < tokens.length; i++) {
						long id = hash.hash(tokens[i]);
						repls[i - 1] = new IntegerReply(id);
					}
					ctx.write(new MultiBulkReply(repls));
				}
				break;
			case "show":
				if (tokens.length == 1) {
					ctx.write(new ErrorReply(
							"expect more parameters: show <hash>"));
				} else if (tokens.length == 2) {
					String origin = hash.show(Long.parseLong(tokens[1]));
					ctx.write(new BulkReply(origin));
				} else {
					BulkReply[] repls = new BulkReply[tokens.length - 1];
					for (int i = 1; i < tokens.length; i++) {
						String origin = hash.show(Long.parseLong(tokens[i]));
						repls[i - 1] = new BulkReply(origin);
					}
					ctx.write(new MultiBulkReply(repls));
				}
				break;
			case "ping":
				ctx.write(StatusReply.PONG);
				break;
			default:
				ctx.write(new ErrorReply("no such command: " + tokens[0]));
				break;
			}
		} catch (Exception e) {
			log.warn("internal error", e);
			ctx.write(new ErrorReply("internal error: " + e.getMessage()));
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.debug("network error", cause);
		if (ctx.channel().isActive()) {
			ctx.write(new ErrorReply("network error: " + cause.getMessage()))
					.addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.close();
		}
	}

}
