package org.codesharp.traffic.drpc;

import org.codesharp.traffic.Commands;
import org.codesharp.traffic.SimpleMessageHandle;

public class SimpleDRPCMessageHandle extends SimpleMessageHandle implements DRPCMessageHandle {
	public boolean isRequest(Object msg) {
		return DRPCMessage.REQ.equals(((DRPCMessage) msg).Command);
	}
	
	public boolean isReply(Object msg) {
		return DRPCMessage.REP.equals(((DRPCMessage) msg).Command);
	}
	
	public Object newMessage(Object request) {
		DRPCMessage req = (DRPCMessage) request;
		req.OutId = "out_id";
		Message msg = new Message();
		msg.Command = Commands.MSG;
		msg.Destination = req.Destination;
		msg.Body = req;
		msg.Headers.put("in_id", req.ID);
		msg.Headers.put("out_id", req.OutId);
		return msg;
	}
	
	public Object newAck(Object reply, Object msg) {
		Message ack = (Message) msg;
		ack.Command = Commands.ACK;
		ack.Body = reply;
		DRPCMessage rep = (DRPCMessage) reply;
		rep.ID = ack.Headers.get("in_id");
		return ack;
	}
	
	public Object getOutcomeId(Object msg) {
		return ((Message) msg).Headers.get("out_id");
	}
	
	public Object getReplyId(Object reply) {
		return ((DRPCMessage) reply).OutId;
	}
	
	public Object getBody(Object msg) {
		return ((Message) msg).Body;
	}
	
	public static class DRPCMessage {
		public final static String REQ = "REQ";
		public final static String REP = "REP";
		public Object Command;
		public Object Destination;
		public Object ID;
		public Object OutId;
		public Object Body;
		
		@Override
		public String toString() {
			return String.format("%s|%s|%s|%s|%s",
					this.Command,
					this.Destination,
					this.ID,
					this.OutId,
					this.Body);
		}
	}
}
