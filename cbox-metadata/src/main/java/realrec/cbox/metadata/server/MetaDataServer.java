package realrec.cbox.metadata.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.cbox.metadata.hash.HashService;
import realrec.common.protocol.command.CommandDecoder;
import realrec.common.protocol.reply.ReplyEncoder;

public class MetaDataServer {

	private static final Logger log = LoggerFactory
			.getLogger(MetaDataServer.class);

	public static void main(String[] args) throws Exception {
		ServerBootstrap sb = new ServerBootstrap();
		try (HashService hash = HashService.init()) {
			ChannelFuture cf = sb.channel(NioServerSocketChannel.class)
					.group(new NioEventLoopGroup()).localAddress(5587)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline().addLast(new ReplyEncoder(),
									new CommandDecoder(),
									new MetaDataRequestHandler(hash));
						}
					}).bind().sync();
			log.info("ready");
			cf.channel().closeFuture().sync();
		} finally {
			sb.shutdown();
		}
	}

}
