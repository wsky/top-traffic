package org.codesharp.traffic.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

import org.codesharp.traffic.Node;

public abstract class WebSocketConnection extends NettyConnection {
	public WebSocketConnection(Node local, Channel channel) {
		super(local, channel);
	}
	
	public WebSocketConnection(Node local, URI uri) throws Throwable {
		super(local, uri);
	}
	
	@Override
	protected void preparePipeline(ChannelPipeline pipeline) {
		pipeline.addLast(
				new HttpClientCodec(),
				new HttpObjectAggregator(8192));
		pipeline.addLast("handler",
				new WebSocketClientHandler(
						WebSocketClientHandshakerFactory.newHandshaker(
								this.uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
	}
	
	@Override
	protected Channel connect() throws Throwable {
		Channel channel = super.connect();
		
		WebSocketClientHandler handler = (WebSocketClientHandler) channel.pipeline().get("handler");
		handler.handshakeFuture().sync();
		
		if (!handler.handshakeFuture().sync().isSuccess())
			throw handler.handshakeFuture().cause();
		
		return channel;
	}
}