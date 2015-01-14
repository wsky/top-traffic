package org.codesharp.traffic.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.Commands;
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
		long dst = 1L;
		long inId = 0L;
		// FIXME generaete global outId
		long outId = 0L;
		return this.newMessage(Commands.MSG, dst, 4, "".getBytes(), inId, outId);
	}
	
	public Object newAck(Object reply, Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		// FIXME rewrite ack message.id with inId
		this.setCommand(buf, Commands.ACK);
		this.setBody(buf, "".getBytes());
		return buf;
	}
	
	public Object getOutId(Object msg) {
		return this.getHeader(msg, 0);
	}
	
	public Object getBody(Object msg) {
		return this.getMessageBody(msg);
	}
}
