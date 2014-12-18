package org.codesharp.traffic;

import static org.easymock.classextension.EasyMock.*;

import org.junit.Test;

public class NodeTrafficTest {
	private Object req = "req";
	private Object rep = "rep";
	private Object msg = "msg";
	private Object ack = "ack";
	private Object req_body = "req_body";
	private Object rep_body = "rep_body";
	private Object outId = 1;
	private Object dst = "C2";
	private Object c1_id = 1;
	private Object c2_id = 2;
	private Object r1_id = 3;
	private Object r2_id = 4;
	
	@Test
	public void c1_r_c2_traffic_test() {
		MessageHelper helper = createStrictMock(MessageHelper.class);
		Router r = newRouter(helper, r1_id, "R", null);
		Connection c1 = newConnection(helper, c1_id, "C1", r);
		Connection c2 = newConnection(helper, c2_id, dst, r);
		r.register(c1);
		r.register(c2);
		
		// c2-r1->c3
		expect(helper.getCommand(req)).andReturn(Commands.REQ);
		expect(helper.newMessage(req, c1)).andReturn(msg);
		expect(helper.getCommand(msg)).andReturn(Commands.MSG);
		expect(helper.getDestination(msg)).andReturn(dst);
		expect(helper.getCommand(msg)).andReturn(Commands.MSG);
		expect(helper.getOutId(msg)).andReturn(outId);
		expect(helper.getBody(msg)).andReturn(req_body);
		// c3->r1
		expect(helper.getCommand(rep)).andReturn(Commands.REP);
		expect(helper.getOutId(rep)).andReturn(outId);
		expect(helper.newAck(msg, rep)).andReturn(ack);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getNext(ack)).andReturn(r1_id);
		// r1->c2
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getNext(ack)).andReturn(c1_id);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getBody(ack)).andReturn(rep_body);
		replay(helper);
		
		sendMessage(c1, req);
		sendMessage(c2, rep);
		
		verify(helper);
	}
	
	@Test
	public void c1_r1_r2_c2_traffic_test() {
		MessageHelper helper = createStrictMock(MessageHelper.class);
		
		Router r1 = newRouter(helper, r1_id, "R1", null);
		Router r2 = newRouter(helper, r2_id, "R2", null);
		Connection c1 = newConnection(helper, c1_id, "C1", r1);
		Connection c2 = newConnection(helper, c2_id, dst, r2);
		Node r1_proxy = newProxy(r1.id(), r1.flag(), r1);
		Node c2_proxy = newProxy(c2.id(), c2.flag(), r2);
		r1.register(c1);
		r1.register(c2_proxy);
		r2.register(c2);
		r2.register(r1_proxy);
		
		expect(helper.getCommand(req)).andReturn(Commands.REQ);
		expect(helper.newMessage(req, c1)).andReturn(msg);
		expect(helper.getCommand(msg)).andReturn(Commands.MSG);
		expect(helper.getDestination(msg)).andReturn(dst);
		expect(helper.getCommand(msg)).andReturn(Commands.MSG);
		expect(helper.getDestination(msg)).andReturn(dst);
		expect(helper.getCommand(msg)).andReturn(Commands.MSG);
		expect(helper.getOutId(msg)).andReturn(outId);
		expect(helper.getBody(msg)).andReturn(req_body);
		
		expect(helper.getCommand(rep)).andReturn(Commands.REP);
		expect(helper.getOutId(rep)).andReturn(outId);
		expect(helper.newAck(msg, rep)).andReturn(ack);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getNext(ack)).andReturn(r1_id);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getNext(ack)).andReturn(c1_id);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getBody(ack)).andReturn(rep_body);
		replay(helper);
		
		sendMessage(c1, req);
		sendMessage(c2, rep);
		
		verify(helper);
	}
	
	private Router newRouter(MessageHelper helper, final Object id, final Object flag, final Node next) {
		return new Router(helper) {
			@Override
			public Object id() {
				return id;
			}
			
			@Override
			public Object flag() {
				return flag;
			}
			
			@Override
			public void send(Object msg) {
				this.onMessage(msg);
			}
			
			@Override
			public Node next() {
				return next;
			}
			
			@Override
			public void onMessage(Object msg) {
				message(this, msg);
				super.onMessage(msg);
			}
		};
	}
	
	private Connection newConnection(MessageHelper helper, final Object id, final Object flag, final Node next) {
		return new Connection(helper) {
			@Override
			public Object id() {
				return id;
			}
			
			@Override
			public Object flag() {
				return flag;
			}
			
			@Override
			protected void flush(Object msg) {
				System.out.println(String.format("%s -> %s#%s", msg, this.flag(), this.id()));
			}
			
			@Override
			public Node next() {
				return next;
			}
			
			@Override
			public void onMessage(Object msg) {
				message(this, msg);
				super.onMessage(msg);
			}
		};
	}
	
	private Node newProxy(final Object id, final Object flag, final Node real) {
		return new Node() {
			@Override
			public Object id() {
				return id;
			}
			
			@Override
			public Object flag() {
				return flag;
			}
			
			@Override
			public void send(Object msg) {
				message(this, msg, "proxy");
				real.onMessage(msg);
			}
			
			@Override
			public void onMessage(Object msg) {
				Asserter.throwUnsupported("onMessage in proxy");
			}
			
			@Override
			public Node next() {
				return null;
			}
		};
	}
	
	private void sendMessage(Node n, Object msg) {
		System.out.println(String.format("%s#%s -> %s", n.flag(), n.id(), msg));
		n.onMessage(msg);
	}
	
	private void message(Node n, Object msg, Object... extra) {
		System.out.println(String.format("%s -> %s#%s#%s#%s", msg, n.flag(), n.id(), n.hashCode(), extra.length > 0 ? extra[0] : ""));
	}
}
