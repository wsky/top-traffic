package org.codesharp.traffic;

import static org.easymock.classextension.EasyMock.*;

import org.junit.Test;

public class NodeTrafficTest {
	@Test
	public void c1_r_c2_traffic_test() {
		Object req = "req";
		Object rep = "rep";
		Object msg = "msg";
		Object ack = "ack";
		Object req_body = "req_body";
		Object rep_body = "rep_body";
		Object outId = 1;
		Object dst = "C2";
		Object r_id = 0;
		Object c1_id = 1;
		Object c2_id = 2;
		
		MessageHelper helper = createStrictMock(MessageHelper.class);
		Router r = newRouter(helper, r_id, "R", null);
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
		expect(helper.getNext(ack)).andReturn(r_id);
		// r1->c2
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getNext(ack)).andReturn(c1_id);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getBody(ack)).andReturn(rep_body);
		replay(helper);
		
		System.out.println(String.format("%s#%s -> %s", c1.flag(), c1.id(), req));
		c1.onMessage(req);
		System.out.println(String.format("%s#%s -> %s", c2.flag(), c2.id(), rep));
		c2.onMessage(rep);
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
		};
	}
}
