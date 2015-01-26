package org.codesharp.traffic.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import org.codesharp.traffic.Connection;

public abstract class WebSocketServer extends NettyServer {
	public WebSocketServer(int port) {
		super(port);
	}
	
	@Override
	protected void preparePipeline(ChannelPipeline pipeline) {
		pipeline.addLast(
				new HttpServerCodec(),
				new HttpObjectAggregator(65536),
				new WebSocketServerHandler() {
					@Override
					protected Connection newConnection(ChannelHandlerContext ctx, Object msg) {
						return WebSocketServer.this.newConnection(ctx, msg);
					}
				});
	}
}
