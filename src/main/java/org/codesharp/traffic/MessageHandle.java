package org.codesharp.traffic;

public interface MessageHandle {
	byte getCommand(Object msg);
	
	Object getDestination(Object msg);
	
	Object getNext(Object msg);
}
