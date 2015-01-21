package org.codesharp.traffic.drpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codesharp.traffic.Asserter;
import org.codesharp.traffic.Commands;
import org.codesharp.traffic.Connection;

public class Frontend extends Connection {
	private Connection conn;
	private DRPCMessageHandle handle;
	private ConcurrentMap<Object, Object> outgoings = new ConcurrentHashMap<Object, Object>();
	
	public Frontend(Connection conn, DRPCMessageHandle handle) {
		super(null);
		this.conn = conn;
		this.handle = handle;
	}
	
	@Override
	public Object id() {
		return this.conn.id();
	}
	
	@Override
	public Object flag() {
		return this.conn.flag();
	}
	
	@Override
	public void onMessage(Object msg) {
		msg = this.handle.resolve(msg);
		
		if (this.handle.isRequest(msg))
			msg = this.handle.newMessage(msg);
		else if (this.handle.isReply(msg))
			msg = this.handle.newAck(msg, this.getOutgoing(msg));
		else
			Asserter.throwUnsupported(msg);
		
		this.conn.onMessage(msg);
	}
	
	@Override
	public void send(Object msg) {
		// FIXME check msg status
		this.tryPutOutgoing(msg);
		this.conn.send(this.handle.getBody(msg));
	}
	
	private void tryPutOutgoing(Object msg) {
		if (this.handle.getCommand(msg) == Commands.MSG)
			this.outgoings.put(this.handle.getOutcomeId(msg), msg);
	}
	
	private Object getOutgoing(Object reply) {
		Object id = this.handle.getReplyId(reply);
		return Asserter.throwIfOutgoingNotExists(this.outgoings.remove(id), id);
	}
}
