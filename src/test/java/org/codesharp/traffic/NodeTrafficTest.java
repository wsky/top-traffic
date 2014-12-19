package org.codesharp.traffic;

import static junit.framework.Assert.*;

import java.util.LinkedList;
import java.util.Queue;

import org.codesharp.traffic.SimpleMessageHandle.Message;
import org.junit.After;
import org.junit.Test;

public class NodeTrafficTest {
	private Object n1_flag = "N1";
	private Object n2_flag = "N2";
	private Object n3_flag = "N3";
	private Object n4_flag = "N4";
	private Object n2_n1_id = "2-1";
	private Object n2_n3_id = "2-3";
	private Object n3_n4_id = "3-4";
	
	private Queue<Object> path = new LinkedList<Object>();
	
	@After
	public void before() {
		path.clear();
	}
	
	@Test
	public void N1_N2_N3_traffic_test() {
		MessageHandle handle = new SimpleMessageHandle();
		Message msg = new Message();
		msg.Destination = n3_flag;
		
		Node n2 = newNode(handle, n2_flag);
		// n1,n2 connect
		Connection n1_n2 = newConnection(n2_n1_id, n2);
		NodeProxy n1 = newProxy(n1_flag);
		n2.accept(n1, n1_n2);
		// n2,n3 connect
		Connection n2_n3 = newConnection(n2_n3_id, n2);
		NodeProxy n3 = newProxy(n3_flag);
		n2.accept(n3, n2_n3);
		
		n1_n2.onMessage(msg);
		msg.Command = Commands.ACK;
		n2_n3.onMessage(msg);
		
		assertPath(n2_n1_id, n2_flag, n2_n3_id, n2_flag);
	}
	
	@Test
	public void N1_N2_N3_N4_traffic_test() {
		MessageHandle handle = new SimpleMessageHandle();
		Message msg = new Message();
		msg.Destination = n4_flag;
		
		Node n2 = newNode(handle, n2_flag);
		Node n3 = newNode(handle, n3_flag);
		// n1,n2 connect
		Connection n1_n2 = newConnection(n2_n1_id, n2);
		n2.accept(newProxy(n1_flag), n1_n2);
		// n3,n4 connect
		Connection n3_n4 = newConnection(n3_n4_id, n3);
		n3.accept(newProxy(n4_flag), n3_n4);
		// n2 next to n3
		n2.setNext(newProxy(n3_flag, n2_flag, newConnection(n2_n3_id, n3)));
		// n2-n3
		Connection n2_n3 = newConnection(n2_n3_id, n2);
		Connection n3_n2 = newConnection(n2_n3_id, n3, n2_n3);
		n3.accept(newProxy(n2_flag), n3_n2);
		
		n1_n2.onMessage(msg);
		msg.Command = Commands.ACK;
		n3_n4.onMessage(msg);
		
		assertPath(n2_n1_id, n2_flag, n2_n3_id, n3_flag, n3_n4_id, n3_flag, n2_n3_id, n2_flag);
	}
	
	private Node newNode(MessageHandle handle, final Object flag) {
		return new Node(handle) {
			@Override
			protected void process(Object msg) {
			}
			
			@Override
			public Object flag() {
				return flag;
			}
			
			@Override
			public void onMessage(Object msg, Connection from) {
				path.add(this.flag());
				System.out.println(String.format("conn#%s -> node#%s: %s", from.id(), this.flag(), msg));
				super.onMessage(msg, from);
			}
		};
	}
	
	private NodeProxy newProxy(final Object flag) {
		return newProxy(flag, null, null);
	}
	
	private NodeProxy newProxy(final Object flag, final Object at, final Connection conn) {
		return new NodeProxy() {
			@Override
			public void send(Object msg) {
				System.out.println(String.format("node#%s -> node#%s: %s", at, this.flag(), msg));
				conn.onMessage(msg);
			}
			
			@Override
			public Object flag() {
				return flag;
			}
		};
	}
	
	private Connection newConnection(final Object id, final Node local) {
		return newConnection(id, local, null);
	}
	
	private Connection newConnection(final Object id, final Node local, final Connection remote) {
		return new Connection(local) {
			@Override
			public void send(Object msg) {
				System.out.println(String.format("node#%s -> conn#%s: %s", local.flag(), this.id(), msg));
				if (remote != null)
					remote.onMessage(msg);
			}
			
			@Override
			public Object id() {
				return id;
			}
			
			@Override
			public void onMessage(Object msg) {
				path.add(this.id());
				System.out.println(String.format("network -> conn#%s: %s", this.id(), msg));
				super.onMessage(msg);
			}
		};
	}
	
	private void assertPath(Object... args) {
		assertEquals(args.length, path.size());
		int i = 0;
		Object p;
		while ((p = path.poll()) != null) {
			assertEquals(args[i++], p);
			System.out.print(p);
			System.out.print(" <-> ");
		}
		System.out.println();
	}
}
