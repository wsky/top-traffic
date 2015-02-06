package org.codesharp.traffic.netty;

import org.codesharp.traffic.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.util.CharsetUtil;

public class WebSocketClientHandler extends NettyHandler {
	private final static Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);
	private WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	
	public WebSocketClientHandler(Connection connection, WebSocketClientHandshaker handshaker) {
		super(connection);
		this.handshaker = handshaker;
	}
	
	@Override
	protected Connection newConnection(ChannelHandlerContext ctx, Object msg) {
		return this.connection;
	}
	
	public ChannelFuture handshakeFuture() {
		return this.handshakeFuture;
	}
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.handshakeFuture = ctx.newPromise();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		this.handshaker.handshake(ctx.channel());
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			System.out.println("WebSocket Client connected!");
			handshakeFuture.setSuccess();
			return;
		}
		
		if (msg instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) msg;
			throw new IllegalStateException(
					"Unexpected FullHttpResponse (getStatus=" + response.status() +
							", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
		}
		
		if (msg instanceof PongWebSocketFrame) {
			System.out.println("WebSocket Client received pong");
			return;
		}
		
		if (msg instanceof CloseWebSocketFrame) {
			System.out.println("WebSocket Client received closing");
			ch.close();
			return;
		}
		
		if (msg instanceof BinaryWebSocketFrame) {
			try {
				this.connection.onMessage(((BinaryWebSocketFrame) msg).content().retain());
			} catch (Exception e) {
				logger.error("onMessage error", e);
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (!handshakeFuture.isDone())
			handshakeFuture.setFailure(cause);
		ctx.close();
	}
}