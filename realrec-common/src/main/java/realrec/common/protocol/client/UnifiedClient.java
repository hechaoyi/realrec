package realrec.common.protocol.client;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.common.protocol.command.Command;
import realrec.common.protocol.command.CommandEncoder;
import realrec.common.protocol.reply.Reply;
import realrec.common.protocol.reply.ReplyDecoder;

public class UnifiedClient implements Closeable {

	private static final Logger log = LoggerFactory
			.getLogger(UnifiedClient.class);
	private static ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	public static final int RECONNECT_INTERVAL = 5;
	public static final int APPLY_CHANNEL_TIMEOUT = 1;
	private EventLoopGroup group;
	private BlockingQueue<SocketChannel> channels;
	private volatile boolean closed;

	public UnifiedClient(String hosts, int nConns, int nThreads) {
		group = new NioEventLoopGroup(nThreads);
		channels = new LinkedBlockingQueue<>();
		closed = false;
		for (InetSocketAddress addr : parseHosts(hosts))
			for (int i = 0; i < nConns; i++)
				connect(addr);
	}

	@Override
	public void close() throws IOException {
		closed = true;
		for (SocketChannel channel : channels)
			channel.close();
		group.shutdown();
	}

	public Promise<Reply<?>> send(Command command) {
		SocketChannel channel = null;
		try {
			channel = channels.poll(APPLY_CHANNEL_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (channel == null)
			throw new IllegalStateException("no active channel available");
		channels.offer(channel);
		Queue<Promise<Reply<?>>> queue = channel.attr(
				UnifiedClientHandler.PROMISE_QUEUE).get();
		Promise<Reply<?>> reply = new Promise<>();
		synchronized (queue) {
			queue.offer(reply);
			channel.write(command);
		}
		return reply;
	}

	private List<InetSocketAddress> parseHosts(String hosts) {
		if (hosts.trim().isEmpty())
			throw new IllegalArgumentException("no hosts in list: " + hosts);
		List<InetSocketAddress> addrs = new ArrayList<>();
		for (String hp : hosts.split("(?:\\s|,)+")) {
			if (hp.isEmpty())
				continue;
			int colon = hp.lastIndexOf(':');
			if (colon < 1)
				throw new IllegalArgumentException("invalid server: " + hp);
			String h = hp.substring(0, colon);
			String p = hp.substring(colon + 1);
			addrs.add(new InetSocketAddress(h, Integer.parseInt(p)));
		}
		if (addrs.size() == 0)
			throw new IllegalArgumentException("no hosts in list: " + hosts);
		return addrs;
	}

	private void connect(final InetSocketAddress addr) {
		if (closed)
			return;
		SocketChannel channel = new NioSocketChannel();
		channel.pipeline().addLast(new CommandEncoder(), new ReplyDecoder(),
				new UnifiedClientHandler());
		group.register(channel).syncUninterruptibly();
		if (!channel.connect(addr).syncUninterruptibly().isSuccess())
			throw new ChannelException("cannot connect to host: " + addr);
		channels.offer(channel);
		channel.closeFuture().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				channels.remove(future.channel());
				executor.execute(new Runnable() {
					@Override
					public void run() {
						try {
							connect(addr);
						} catch (ChannelException e) {
							log.warn("reconnect {} error: {}", addr,
									e.getMessage());
							executor.schedule(this, RECONNECT_INTERVAL,
									TimeUnit.SECONDS);
						}
					}
				});
			}
		});
	}

}
