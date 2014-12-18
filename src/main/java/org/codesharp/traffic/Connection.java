package org.codesharp.traffic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Gateway connection from fontend, always forward income message to next
 */
public abstract class Connection implements Node {
	private MessageHelper helper;
	
	private ConcurrentMap<Object, Object> outgoings = new ConcurrentHashMap<Object, Object>();
	
	public Connection(MessageHelper helper) {
		this.helper = helper;
	}
	
	public void send(Object msg) {
		if (this.helper.getCommand(msg) == Commands.MSG)
			this.outgoings.put(this.helper.getOutId(msg), msg);
		
		this.flush(this.helper.getBody(msg));
	}
	
	public void onMessage(Object msg) {
		byte cmd = this.helper.getCommand(msg);
		switch (cmd) {
		case Commands.REQ:
			this.forwardRequest(msg);
			break;
		case Commands.REP:
			this.forwardReply(msg);
			break;
		default:
			Asserter.throwUnsupportedCommand(cmd);
			break;
		}
	}
	
	private void forwardRequest(Object request) {
		this.next().send(this.helper.newMessage(request, this));
	}
	
	private void forwardReply(Object reply) {
		Object id = this.helper.getOutId(reply);
		this.next().send(
				this.helper.newAck(
						Asserter.throwIfOutgoingNotExists(this.outgoings.remove(id), id),
						reply));
	}
	
	protected abstract void flush(Object msg);
}
