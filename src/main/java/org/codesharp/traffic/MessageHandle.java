package org.codesharp.traffic;

public interface MessageHandle {
	byte getCommand(Object msg);
	
	byte getStatus(Object msg);
	
	Object getDestination(Object msg);
	
	Object getNext(Object msg);
	
	Object append(Object msg, Object from);
	
	Object unknownDestination(Object msg);
}
