package org.codesharp.traffic.netty;

import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.codesharp.traffic.Connection;
import org.codesharp.traffic.Node;

public abstract class NettyConnection extends Connection {
	private final static EventLoopGroup group = new NioEventLoopGroup();

	private Node local;
	private Channel channel;
	
	protected URI uri;
	
	protected NettyConnection(Node local) {
		super(local);
		this.local = local;
	}
	
	public NettyConnection(Node local, Channel channel) {
		this(local);
		this.channel(channel);
	}
	
	public NettyConnection(Node local, URI uri) throws Throwable {
		this(local);
		this.uri = uri;
		this.channel(this.connect());
	}
	
	protected void channel(Channel channel) {
		this.channel = channel;
		this.local.accept(this);
	}
	
	@Override
	public void send(Object msg) {
		this.channel.writeAndFlush(msg);
	}
	
	protected void preparePipeline(ChannelPipeline pipeline) {
		pipeline.addLast(
				new MessageDecoder(new MessageHandleImpl(ByteBufAllocator.DEFAULT)), new NettyHandler() {
					@Override
					protected Connection newConnection(ChannelHandlerContext ctx, Object msg) {
						return null;
					}
				});
	}
	
	protected Channel connect() throws Throwable {
		Bootstrap b = new Bootstrap();
		b.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						preparePipeline(ch.pipeline());
					}
				});
		return b.connect(this.uri.getHost(), this.uri.getPort()).sync().channel();
	}
}