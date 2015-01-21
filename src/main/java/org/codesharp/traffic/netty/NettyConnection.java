package org.codesharp.traffic.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.URI;

import org.codesharp.traffic.Connection;
import org.codesharp.traffic.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyConnection extends Connection {
	private final static Logger logger = LoggerFactory.getLogger(NettyConnection.class);
	private final static EventLoopGroup group = new NioEventLoopGroup();
	
	private Node local;
	private Channel channel;
	private Connection wrapped;
	private MessageHandleImpl handle = new MessageHandleImpl(ByteBufAllocator.DEFAULT);
	
	public NettyConnection(Node local) {
		super(local);
		this.local = local;
	}
	
	public NettyConnection(Node local, URI uri) throws Throwable {
		super(local);
		this.local = local;
		this.channel(this.connect(uri, this.newHandlers(uri)));
	}
	
	public void channel(Channel channel) {
		this.channel = channel;
		this.local.accept(this.wrapped = this.wrap(this));
	}
	
	public void messsageHandle(MessageHandleImpl handle) {
		this.handle = handle;
	}
	
	@Override
	public void send(Object msg) {
		this.channel.writeAndFlush(msg);
	}
	
	public void onWrappedMessage(Object msg) {
		this.wrapped.onMessage(msg);
	}
	
	public ChannelHandler[] newHandlers(URI uri) {
		return new ChannelHandler[] {
				new MessageDecoder(this.handle),
				new SimpleChannelInboundHandler<Object>() {
					@Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						if (NettyConnection.this.channel == null)
							NettyConnection.this.channel(ctx.channel());
					}
					
					@Override
					public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
						NettyConnection.this.onWrappedMessage(msg);
					}
					
					@Override
					public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
						// if read and write at once event,
						// write will be executed until next event, call flush to force write
						ctx.flush();
					}
					
					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
						logger.error(String.format("%s#%s",
								NettyConnection.this.flag(),
								NettyConnection.this.id()), cause);
						ctx.close();
					}
				}
		};
	}
	
	protected Channel connect(URI uri, final ChannelHandler... handlers) throws Throwable {
		Bootstrap b = new Bootstrap();
		b.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						p.addLast(handlers);
					}
				});
		return b.connect(uri.getHost(), uri.getPort()).sync().channel();
	}
	
	protected Connection wrap(Connection conn) {
		return conn;
	}
}