package org.codesharp.traffic.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.drpc.DRPCMessageHandle;

public class DRPCMessageHandleImpl extends MessageHandleImpl implements DRPCMessageHandle {
	public DRPCMessageHandleImpl(ByteBufAllocator allocator) {
		super(allocator);
	}
	
	public boolean isRequest(Object msg) {
		return false;
	}
	
	public boolean isReply(Object msg) {
		return false;
	}
	
	public Object getReplyId(Object reply) {
		return null;
	}
	
	public Object newMessage(Object request) {
		ByteBuf buf = this.allocator.buffer();
		// FIXME write new message to buffer
		return buf;
	}
	
	public Object newAck(Object reply, Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		// FIXME rewrite ack message to buffer
		return buf;
	}
	
	public Object getOutId(Object msg) {
		return this.getHeader(msg, 0);
	}
	
	public Object getBody(Object msg) {
		return this.getMessageBody(msg);
	}
}
