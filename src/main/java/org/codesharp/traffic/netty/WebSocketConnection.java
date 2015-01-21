package org.codesharp.traffic.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

import org.codesharp.traffic.Node;

public abstract class WebSocketConnection extends NettyConnection {
	public WebSocketConnection(Node local) throws Throwable {
		super(local);
	}
	
	public WebSocketConnection(Node local, URI uri) throws Throwable {
		super(local, uri);
	}
	
	@Override
	public ChannelHandler[] newHandlers(URI uri) {
		return uri == null ? new ChannelHandler[] {
				new HttpServerCodec(),
				new HttpObjectAggregator(65536),
				new WebSocketServerHandler(this)
		} : new ChannelHandler[] {
				new HttpClientCodec(),
				new HttpObjectAggregator(8192),
				new WebSocketClientHandler(
						WebSocketClientHandshakerFactory.newHandshaker(
								uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()))
		};
	}
	
	@Override
	protected Channel connect(URI uri, ChannelHandler... handlers) throws Throwable {
		Channel channel = super.connect(uri, handlers);
		
		WebSocketClientHandler handler = (WebSocketClientHandler) handlers[handlers.length - 1];
		handler.handshakeFuture().sync();
		
		if (!handler.handshakeFuture().sync().isSuccess())
			throw handler.handshakeFuture().cause();
		
		return channel;
	}
}