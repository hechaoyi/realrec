package realrec.common.protocol.client;

import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import realrec.common.protocol.command.Command;
import realrec.common.protocol.command.CommandEncoder;
import realrec.common.protocol.reply.Reply;
import realrec.common.protocol.reply.ReplyDecoder;

public class UnifiedClient implements Closeable {

	private static final Logger log = LoggerFactory
			.getLogger(UnifiedClient.class);
	public static final AttributeKey<Queue<Promise<Reply<?>>>> PROMISE_QUEUE = new AttributeKey<>(
			"PromiseQueue");
	private static ExecutorService executor = Executors.newCachedThreadPool();
	private static Random rand = new Random();
	private EventLoopGroup group;
	private List<SocketChannel> channels;

	public UnifiedClient(String hosts, int nConns, int nThreads) {
		group = new NioEventLoopGroup(nThreads);
		channels = new CopyOnWriteArrayList<>();
		for (InetSocketAddress addr : parseHosts(hosts))
			for (int i = 0; i < nConns; i++)
				add(addr);
	}

	@Override
	public void close() throws IOException {
		executor.shutdown();
		group.shutdown();
	}

	public Promise<Reply<?>> send(Command command) {
		SocketChannel channel;
		synchronized (channels) {
			int size = channels.size();
			if (size == 0)
				throw new IllegalStateException("no active channel available");
			channel = channels.get(rand.nextInt(size));
		}
		Queue<Promise<Reply<?>>> queue = channel.attr(PROMISE_QUEUE).get();

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

	private void add(InetSocketAddress addr) {
		SocketChannel channel = new NioSocketChannel();
		channel.pipeline().addLast(new CommandEncoder(), new ReplyDecoder(),
				new UnifiedClientHandler());
		group.register(channel).syncUninterruptibly();
		if (!channel.connect(addr).syncUninterruptibly().isSuccess())
			throw new ChannelException("cannot connect to host: " + addr);
		channel.closeFuture().addListener(new ReconnectAfterClose(addr));
		channels.add(channel);
		System.out.println(channels.size());
		channel.attr(PROMISE_QUEUE).set(
				new ConcurrentLinkedQueue<Promise<Reply<?>>>());
	}

	private class ReconnectAfterClose implements ChannelFutureListener {

		InetSocketAddress addr;

		ReconnectAfterClose(InetSocketAddress addr) {
			this.addr = addr;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			channels.remove(future.channel());
			System.out.println(channels.size());
			if (!executor.isShutdown())
				executor.execute(reconnect);
		}

		Runnable reconnect = new Runnable() {
			@Override
			public void run() {
				try {
					add(addr);
				} catch (ChannelException e) {
					log.warn("reconnect {} error: {}", addr, e.getMessage());
					try {
						Thread.sleep(5000);
						if (!executor.isShutdown())
							executor.execute(reconnect);
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
			}
		};

	}

}
