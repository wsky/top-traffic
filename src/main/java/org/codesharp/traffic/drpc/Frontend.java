package org.codesharp.traffic.drpc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codesharp.traffic.Asserter;
import org.codesharp.traffic.Commands;
import org.codesharp.traffic.Connection;
import org.codesharp.traffic.Node;

public abstract class Frontend extends Connection {
	private DRPCMessageHandle handle;
	private ConcurrentMap<Object, Object> outgoings = new ConcurrentHashMap<Object, Object>();
	
	public Frontend(Node local, DRPCMessageHandle handle) {
		super(local);
		this.handle = handle;
	}
	
	protected abstract void internalSend(Object msg);
	
	@Override
	public void onMessage(Object msg) {
		if (this.handle.isRequest(msg))
			msg = this.handle.newMessage(msg);
		else if (this.handle.isReply(msg))
			msg = this.handle.newAck(msg, this.getOutgoing(msg));
		else
			Asserter.throwUnsupported(msg);
		
		super.onMessage(msg);
	}
	
	@Override
	public void send(Object msg) {
		this.tryPutOutgoing(msg);
		this.internalSend(this.handle.getBody(msg));
	}
	
	private void tryPutOutgoing(Object msg) {
		if (this.handle.getCommand(msg) == Commands.MSG)
			this.outgoings.put(this.handle.getOutId(msg), msg);
	}
	
	private Object getOutgoing(Object reply) {
		Object id = this.handle.getReplyId(reply);
		return Asserter.throwIfOutgoingNotExists(this.outgoings.remove(id), id);
	}
}
