package org.codesharp.traffic.netty;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.CharsetUtil;

import org.codesharp.traffic.Commands;
import org.codesharp.traffic.drpc.DRPCMessageHandle;

import com.google.gson.Gson;

/***
 * 
 * https://github.com/wsky/top-traffic/issues/2#issuecomment-66725466
 * 
 * DRPC message protocol
 * 
 * { type:'REQ', id:0, to:'dst' }
 * { type:'REP', id:0 }
 */
public class DRPCMessageHandleImpl extends MessageHandleImpl implements DRPCMessageHandle {
	public final static String TYPE = "type";
	public final static String REQ = "REQ";
	public final static String REP = "REP";
	public final static String ID = "id";
	public final static String DST = "to";
	
	private Gson gson = new Gson();
	
	public DRPCMessageHandleImpl(ByteBufAllocator allocator) {
		super(allocator);
	}
	
	public Object resolve(Object msg) {
		return this.resolve((String) msg);
	}
	
	public Map<?, ?> resolve(String msg) {
		return (Map<?, ?>) this.gson.fromJson((String) msg, Object.class);
	}
	
	public boolean isRequest(Object msg) {
		return ((Map<?, ?>) msg).get(TYPE).equals(REQ);
	}
	
	public boolean isReply(Object msg) {
		return ((Map<?, ?>) msg).get(TYPE).equals(REP);
	}
	
	public long getId(Map<?, ?> msg) {
		return this.getLong(msg, ID);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setId(Map msg, long id) {
		msg.put(ID, id);
	}
	
	public Object getReplyId(Object reply) {
		return this.getId((Map<?, ?>) reply);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Object newMessage(Object request) {
		Map msg = (Map) request;
		long dst = this.getLong(msg, DST);
		long inId = this.getId(msg);
		long outId = inId; // FIXME generaete global outId
		
		this.setId(msg, outId);
		
		return this.newMessage(
				Commands.MSG,
				dst, 4,
				this.parseBody(msg),
				inId, outId);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Object newAck(Object reply, Object msg) {
		Map replyMsg = (Map) reply;
		ByteBuf buf = (ByteBuf) msg;
		
		this.setId(replyMsg, this.getIncomeId(buf));
		
		this.setCommand(buf, Commands.ACK);
		this.setBody(buf, this.parseBody(replyMsg));
		
		return buf;
	}
	
	public long getIncomeId(Object msg) {
		return (Long) this.getHeader(msg, 0);
	}
	
	public Long getOutcomeId(Object msg) {
		// HACK retain buffer reused for reply
		((ByteBuf) msg).retain();
		return (Long) this.getHeader(msg, 1);
	}
	
	public Object getBody(Object msg) {
		return this.getMessageBody(msg);
	}
	
	@Override
	public Object unknownDestination(Object msg) {
		super.unknownDestination(msg);
		// FIXME support errorcode at fontend
		this.setBody((ByteBuf) msg, "{error:'unknownDestination'}".getBytes(CharsetUtil.UTF_8));
		return msg;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private long getLong(Map msg, String key) {
		return msg.containsKey(key) ? ((Number) msg.get(key)).longValue() : -1L;
	}
	
	private byte[] parseBody(Object msg) {
		return this.gson.toJson(msg).getBytes(CharsetUtil.UTF_8);
	}
}
