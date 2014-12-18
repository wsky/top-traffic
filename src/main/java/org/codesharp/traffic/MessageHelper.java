package org.codesharp.traffic;

public interface MessageHelper {
	byte getCommand(Object msg);
	
	<T> T get(Class<T> cmd);
	
	Object getFrom(Object msg);
	
	Object getTo(Object msg);
	
	Object newMessage(Object request, Node from);
	
	Object newAck(Object msg, Object reply);
	
	Object getId(Object msg);
	
	Object getOutId(Object msg);
	
	Object getDestination(Object msg);
	
	Object getNext(Object msg);
	
	Object getBody(Object msg);
	
	Object[] getPath(Object msg);
	
	Object append(Object msg, Object from);
}
