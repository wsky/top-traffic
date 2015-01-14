package org.codesharp.traffic.netty;

import java.nio.charset.Charset;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.Commands;
import org.codesharp.traffic.drpc.DRPCMessageHandle;

import com.google.gson.Gson;

public class DRPCMessageHandleImpl extends MessageHandleImpl implements DRPCMessageHandle {
	public final static String REQ = "REQ";
	public final static String REP = "REP";
	public final static String DST = "to";
	
	private final static Charset UTF8 = Charset.forName("UTF-8");
	
	private Gson gson = new Gson();
	
	public DRPCMessageHandleImpl(ByteBufAllocator allocator) {
		super(allocator);
	}
	
	public Object resolve(Object msg) {
		return this.gson.fromJson((String) msg, Object.class);
	}
	
	public boolean isRequest(Object msg) {
		return ((Map<?, ?>) msg).get("command").equals(REQ);
	}
	
	public boolean isReply(Object msg) {
		return ((Map<?, ?>) msg).get("command").equals(REP);
	}
	
	public Object getReplyId(Object reply) {
		return this.getId((Map<?, ?>) reply);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Object newMessage(Object request) {
		Map msg = (Map) request;
		long dst = (Long) msg.get(DST);
		long inId = this.getId(msg);
		long outId = inId; // FIXME generaete global outId
		
		this.setId(msg, outId);
		
		return this.newMessage(
				Commands.MSG,
				dst, 4,
				this.gson.toJson(msg).getBytes(UTF8),
				inId, outId);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Object newAck(Object reply, Object msg) {
		Map replyMsg = (Map) reply;
		ByteBuf buf = (ByteBuf) msg;
		
		this.setId(replyMsg, this.getHeader(msg, 0));
		
		this.setCommand(buf, Commands.ACK);
		this.setBody(buf, this.gson.toJson(msg).getBytes(UTF8));
		
		return buf;
	}
	
	public Object getOutId(Object msg) {
		return this.getHeader(msg, 1);
	}
	
	public Object getBody(Object msg) {
		return this.getMessageBody(msg);
	}
	
	private long getId(Map<?, ?> msg) {
		return (Long) msg.get("id");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setId(Map msg, Object id) {
		msg.put("id", id);
	}
}
