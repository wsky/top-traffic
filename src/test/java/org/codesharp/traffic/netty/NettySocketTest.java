package org.codesharp.traffic.netty;

import java.net.URI;

import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.Commands;
import org.codesharp.traffic.Connection;
import org.codesharp.traffic.Node;
import org.codesharp.traffic.Status;
import org.junit.Test;

public class NettySocketTest {
	@Test
	public void base_connection_test() throws Throwable {
		MessageHandleImpl handle = new MessageHandleImpl(ByteBufAllocator.DEFAULT);
		final Node node = new Node(handle) {
			@Override
			protected void process(Object msg) {
				System.out.println(msg);
			}
			
			@Override
			public Object flag() {
				return "NODE";
			}
		};
		
		URI uri = new URI("tcp://localhost:8889");
		
		NettyServer server = new NettyServer(uri.getPort()) {
			@Override
			protected NettyConnection newConnection() {
				return new NettyConnection(node) {
					@Override
					public Object id() {
						return 1L;
					}
					
					@Override
					public Object flag() {
						return "client";
					}
				};
			}
		};
		
		server.start();
		
		Connection conn = new NettyConnection(node, uri) {
			@Override
			public Object id() {
				return 2L;
			}
			
			@Override
			public Object flag() {
				return "server";
			}
		};
		
		conn.send(handle.newMessage(Commands.MSG, Status.NORMAL, 0L, 2, "hi".getBytes()));
		
		Thread.sleep(1000);
	}
}
