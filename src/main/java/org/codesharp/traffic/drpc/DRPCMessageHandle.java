package org.codesharp.traffic.drpc;

import org.codesharp.traffic.MessageHandle;

public interface DRPCMessageHandle extends MessageHandle {
	Object resolve(Object msg);
	
	boolean isRequest(Object msg);
	
	boolean isReply(Object msg);
	
	Object newMessage(Object request);
	
	Object newAck(Object reply, Object msg);
	
	Object getOutcomeId(Object msg);
	
	Object getReplyId(Object reply);
	
	Object getBody(Object msg);
}
