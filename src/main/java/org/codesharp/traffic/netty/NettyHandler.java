package org.codesharp.traffic.netty;

import org.codesharp.traffic.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCounted;

public abstract class NettyHandler extends SimpleChannelInboundHandler<Object> {
	private final static Logger logger = LoggerFactory.getLogger(NettyHandler.class);
	protected Connection connection;
	
	public NettyHandler() {
	}
	
	public NettyHandler(Connection connection) {
		this.connection = connection;
	}
	
	protected abstract Connection newConnection(ChannelHandlerContext ctx, Object msg);
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (this.connection == null)
			this.connection = this.newConnection(ctx, null);
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		this.onMessage(msg);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// if read and write at once event,
		// write will be executed until next event, call flush to force write
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error(this.connection != null
				? String.format("%s#%s"
						, this.connection.flag()
						, this.connection.id())
				: "exceptionCaught", cause);
		ctx.close();
	}
	
	protected void onMessage(Object msg) {
		if (this.connection == null)
			return;
		
		if (msg instanceof ReferenceCounted)
			((ReferenceCounted) msg).retain();
		
		this.connection.onMessage(msg);
	}
}
