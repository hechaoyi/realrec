package realrec.cbox.metadata.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.cbox.metadata.hash.HashRecord;
import realrec.cbox.metadata.hash.HashRecord.Domain;
import realrec.cbox.metadata.hash.HashService;
import realrec.cbox.metadata.video.VideoService;
import realrec.cbox.metadata.video.VideoSetDetail;
import realrec.common.protocol.command.Command;
import realrec.common.protocol.reply.BulkReply;
import realrec.common.protocol.reply.ErrorReply;
import realrec.common.protocol.reply.IntegerReply;
import realrec.common.protocol.reply.MultiBulkReply;
import realrec.common.protocol.reply.Reply;
import realrec.common.protocol.reply.StatusReply;

@Sharable
public class MetaDataRequestHandler extends
		ChannelInboundMessageHandlerAdapter<Command> {

	private static final Logger log = LoggerFactory
			.getLogger(MetaDataRequestHandler.class);
	private HashService hashService;
	private VideoService videoService;

	public MetaDataRequestHandler(HashService hashService,
			VideoService videoService) {
		this.hashService = hashService;
		this.videoService = videoService;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Command msg)
			throws Exception {
		try {
			String[] tokens = msg.tokens();
			if (tokens.length == 0)
				return;
			switch (tokens[0].toLowerCase()) {
			case "hash":
				if (tokens.length < 3)
					ctx.write(new ErrorReply("usage: hash <domain> <origin>"));
				else
					ctx.write(hash(tokens[1], tokens[2]));
				break;
			case "show":
				if (tokens.length < 2)
					ctx.write(new ErrorReply("usage: show <hash>"));
				else
					ctx.write(show(tokens[1]));
				break;
			case "length":
				if (tokens.length < 2)
					ctx.write(new ErrorReply("usage: length <video_id>"));
				else
					ctx.write(length(tokens[1]));
				break;
			case "detail":
				if (tokens.length < 2)
					ctx.write(new ErrorReply("usage: detail <id>"));
				else
					ctx.write(detail(tokens[1]));
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

	private IntegerReply hash(String domain, String origin) {
		long hash = hashService.hash(Domain.valueOf(domain.toLowerCase()),
				origin);
		return new IntegerReply(hash);
	}

	private MultiBulkReply show(String hash) {
		HashRecord hr = hashService.show(Long.parseLong(hash));
		if (hr == null)
			return new MultiBulkReply((Reply<?>[]) null);
		return new MultiBulkReply(new BulkReply(hr.getOrigin()), new BulkReply(
				hr.getDomain().name()));
	}

	private BulkReply length(String videoId) {
		String length = videoService.videoLength(videoId);
		return new BulkReply(length);
	}

	private MultiBulkReply detail(String id) {
		HashRecord hr = hashService.show(Long.parseLong(id));
		if (hr == null)
			return new MultiBulkReply((Reply<?>[]) null);
		VideoSetDetail detail = videoService.videoSetDetail(hr.getOrigin());
		if (detail == null)
			return new MultiBulkReply((Reply<?>[]) null);
		return new MultiBulkReply(new BulkReply(hr.getOrigin()), new BulkReply(
				detail.getLogo()), new BulkReply(detail.getTitle()));
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
