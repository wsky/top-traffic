package org.codesharp.traffic;

public abstract class Connection {
	private Node local;
	
	public Connection(Node local) {
		this.local = local;
	}
	
	public abstract Object id();
	
	public abstract Object flag();
	
	public abstract void send(Object msg);
	
	public void onMessage(Object msg) {
		this.local.onMessage(msg, this);
	}
}
