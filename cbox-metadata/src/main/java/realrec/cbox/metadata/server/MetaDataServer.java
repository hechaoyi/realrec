package realrec.cbox.metadata.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.cbox.metadata.bdb.BerkeleyDB;
import realrec.cbox.metadata.hash.HashService;
import realrec.cbox.metadata.video.VideoService;
import realrec.common.config.Configuration;
import realrec.common.protocol.command.CommandDecoder;
import realrec.common.protocol.reply.ReplyEncoder;

public class MetaDataServer {

	private static final Logger log = LoggerFactory
			.getLogger(MetaDataServer.class);

	public static void main(String[] args) throws Exception {
		MetaDataConfig conf = Configuration.bootstrap(args,
				MetaDataConfig.class);
		conf.initialize();
		ServerBootstrap sb = new ServerBootstrap();
		try (BerkeleyDB bdb = BerkeleyDB.instance(conf.getDataDir())) {
			final ChannelHandler handler = new MetaDataRequestHandler(
					new HashService(bdb), VideoService.instance());
			ChannelFuture cf = sb.channel(NioServerSocketChannel.class)
					.group(new NioEventLoopGroup())
					.localAddress(conf.getBind(), conf.getPort())
					.option(ChannelOption.SO_BACKLOG, 100)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline().addLast(new ReplyEncoder(),
									new CommandDecoder(), handler);
						}
					}).bind().sync();
			log.info("ready");
			cf.channel().closeFuture().sync();
			log.info("closed");
		} finally {
			sb.shutdown();
		}
	}
}
