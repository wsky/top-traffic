package org.codesharp.traffic;

public interface Node {
	Object id();
	
	Object flag();
	
	void send(Object msg);
	
	void onMessage(Object msg);
	
	Node next();
}
