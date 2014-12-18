package org.codesharp.traffic;

import static org.easymock.classextension.EasyMock.*;

import org.junit.Test;

public class NodeTrafficTest {
	@Test
	public void traffic_test() {
		Object req = "req";
		Object rep = "rep";
		Object msg = "msg";
		Object ack = "ack";
		Object req_body = "req_body";
		Object rep_body = "rep_body";
		Object outId = 1;
		Object dst = "C3";
		Object n_id_1 = 1;
		Object n_id_2 = 2;
		Object n_id_3 = 3;
		
		MessageHelper helper = createStrictMock(MessageHelper.class);
		Router r1 = newRouter(helper, n_id_1, "R2", null);
		Connection c2 = newConnection(helper, n_id_2, "C2", r1);
		Connection c3 = newConnection(helper, n_id_3, dst, r1);
		r1.register(c2);
		r1.register(c3);
		
		// c2-r1->c3
		expect(helper.getCommand(req)).andReturn(Commands.REQ);
		expect(helper.newMessage(req, c2)).andReturn(msg);
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
		expect(helper.getNext(ack)).andReturn(n_id_1);
		// r1->c2
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getNext(ack)).andReturn(n_id_2);
		expect(helper.getCommand(ack)).andReturn(Commands.ACK);
		expect(helper.getBody(ack)).andReturn(rep_body);
		replay(helper);
		
		System.out.println(String.format("%s#%s -> %s", c2.flag(), c2.id(), req));
		c2.onMessage(req);
		System.out.println(String.format("%s#%s -> %s", c3.flag(), c3.id(), rep));
		c3.onMessage(rep);
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
