package org.codesharp.traffic.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.codesharp.traffic.Asserter;
import org.codesharp.traffic.Node;
import org.codesharp.traffic.NodeProxy;
import org.codesharp.traffic.drpc.Frontend;
import org.junit.Test;

public class WebSocketServerTest {
	DRPCMessageHandleImpl handle = new DRPCMessageHandleImpl(ByteBufAllocator.DEFAULT);
	Node node = new Node(handle) {
		@Override
		protected void process(Object msg) {
		}
		
		@Override
		public Object flag() {
			return null;
		}
	};
	
	@Test
	public void run_test() throws InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new WebSocketServerInitializer());
			
			Channel ch = b.bind(8080).sync().channel();
			
			ch.closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new HttpServerCodec());
			pipeline.addLast(new HttpObjectAggregator(65536));
			pipeline.addLast(new WebSocketServerHandler() {
				@Override
				protected Frontend newFrontend(
						final ChannelHandlerContext ctx, HttpRequest req)
						throws Exception {
					
					final long idAndFlag = System.currentTimeMillis();
					System.out.println(idAndFlag);
					
					Frontend frontend = new Frontend(node, handle) {
						@Override
						public Object id() {
							return idAndFlag;
						}
						
						@Override
						protected void internalSend(Object msg) {
							renderBody((ByteBuf) msg);
							ctx.channel().write(new TextWebSocketFrame((ByteBuf) msg));
						}
					};
					
					node.accept(new NodeProxy() {
						@Override
						public void send(Object msg) {
							Asserter.throwUnsupported(null);
						}
						
						@Override
						public Object flag() {
							return idAndFlag;
						}
					}, frontend);
					
					return frontend;
				}
			});
		}
	}
	
	private void renderBody(ByteBuf msg) {
		byte[] bytes = new byte[msg.readableBytes()];
		msg.getBytes(msg.readerIndex(), bytes);
		System.out.println(new String(bytes));
	}
}
