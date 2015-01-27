package org.codesharp.traffic.netty;

import java.net.URI;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import org.codesharp.traffic.Commands;
import org.codesharp.traffic.Connection;
import org.codesharp.traffic.Node;
import org.codesharp.traffic.Status;
import org.codesharp.traffic.drpc.Frontend;
import org.junit.Test;

public class NettySocketTest {
	@Test
	public void base_connection_test() throws Throwable {
		final MessageHandleImpl handle = new MessageHandleImpl(ByteBufAllocator.DEFAULT);
		final Node node1 = new Node(handle) {
			@Override
			protected void process(Object msg) {
				System.out.println("node1: " + handle.toString(msg));
			}
			
			@Override
			public Object flag() {
				return 1L;
			}
		};
		final Node node2 = new Node(handle) {
			@Override
			protected void process(Object msg) {
				System.out.println("node2: " + handle.toString(msg));
			}
			
			@Override
			public Object flag() {
				return 2L;
			}
		};
		
		URI uri = new URI("tcp://localhost:8889");
		
		NettyServer server = new NettyServer(node2, uri.getPort()) {
			@Override
			protected Connection newConnection(ChannelHandlerContext ctx, Object msg) {
				return new NettyConnection(node2, ctx.channel()) {
					@Override
					public Object id() {
						return 21L;
					}
					
					@Override
					public Object flag() {
						return 1L;
					}
				};
			}
		};
		
		server.start();
		
		Connection conn = new NettyConnection(node1, uri) {
			@Override
			public Object id() {
				return 12L;
			}
			
			@Override
			public Object flag() {
				return 2L;
			}
		};
		
		conn.send(handle.newMessage(Commands.MSG, Status.NORMAL, 1L, 2, "hi".getBytes()));
		conn.send(handle.newMessage(Commands.MSG, Status.NORMAL, 2L, 2, "hi".getBytes()));
		conn.send(handle.newMessage(Commands.MSG, Status.NORMAL, 0L, 2, "hi".getBytes()));
		
		Thread.sleep(100);
	}
	
	@Test
	public void websocket_connection_test() throws Throwable {
		final DRPCMessageHandleImpl handle = new DRPCMessageHandleImpl(ByteBufAllocator.DEFAULT);
		final Node node1 = new Node(handle) {
			@Override
			protected void process(Object msg) {
				System.out.println("node1: " + handle.toString(msg));
			}
			
			@Override
			public Object flag() {
				return 1L;
			}
		};
		final Node node2 = new Node(handle) {
			@Override
			protected void process(Object msg) {
				System.out.println("node2: " + handle.toString(msg));
			}
			
			@Override
			public Object flag() {
				return 2L;
			}
		};
		
		URI uri = new URI("ws://localhost:8890");
		
		NettyServer server = new WebSocketServer(node2, uri.getPort()) {
			@Override
			protected Connection newConnection(ChannelHandlerContext ctx, Object msg) {
				return new Frontend(new WebSocketConnection(node2, ctx.channel()) {
					@Override
					public Object id() {
						return 21L;
					}
					
					@Override
					public Object flag() {
						return 1L;
					}
				}, handle);
			}
		};
		
		server.start();
		
		WebSocketConnection conn = new WebSocketConnection(node1, uri) {
			@Override
			public Object id() {
				return 12L;
			}
			
			@Override
			public Object flag() {
				return 2L;
			}
		};
		
		conn.send("{ type:'REQ', id:1024, to:1 }");
		conn.send("{ type:'REQ', id:1024, to:2 }");
		conn.send("{ type:'REQ', id:1024, to:0 }");
		Thread.sleep(100);
	}
}
