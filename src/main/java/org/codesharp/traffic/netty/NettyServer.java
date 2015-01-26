package org.codesharp.traffic.netty;

import org.codesharp.traffic.Connection;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public abstract class NettyServer {
	private final static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private final static EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	private int port;
	private ServerBootstrap bootstrap;
	
	public NettyServer(int port) {
		this.port = port;
	}
	
	protected abstract Connection newConnection(ChannelHandlerContext ctx, Object msg);
	
	protected void preparePipeline(ChannelPipeline pipeline) {
		pipeline.addLast(new MessageDecoder(new MessageHandleImpl(ByteBufAllocator.DEFAULT)));
		pipeline.addLast(new NettyHandler() {
			@Override
			protected Connection newConnection(ChannelHandlerContext ctx, Object msg) {
				return NettyServer.this.newConnection(ctx, msg);
			}
		});
	}
	
	public synchronized void start() throws InterruptedException {
		if (this.bootstrap != null)
			return;
		
		this.bootstrap = new ServerBootstrap();
		this.bootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch) throws Exception {
						preparePipeline(ch.pipeline());
					}
				})
				.bind(this.port)
				.sync();
	}
	
	public synchronized void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
}
