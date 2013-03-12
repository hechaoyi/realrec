package test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import cbox.realrec.protocol.command.Command;
import cbox.realrec.protocol.command.CommandDecoder;

public class RedisProtocolServer extends
		ChannelInboundMessageHandlerAdapter<Command> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Command msg)
			throws Exception {
		System.out.println(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	public static void main(String[] args) throws InterruptedException {
		ServerBootstrap sb = new ServerBootstrap();
		try {
			sb.group(new NioEventLoopGroup())
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel channel)
								throws Exception {
							channel.pipeline().addLast(new CommandDecoder(),
									new RedisProtocolServer());
						}
					}).bind(6379).sync().channel().closeFuture().sync();
		} finally {
			sb.shutdown();
		}
	}

}
