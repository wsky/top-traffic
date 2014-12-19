package org.codesharp.traffic;

public abstract class Connection {
	private Node local;
	
	protected MessageHandle handle;
	
	public Connection(Node local) {
		this.local = local;
	}
	
	public abstract Object id();
	
	public abstract void send(Object msg);
	
	public Node local() {
		return this.local;
	}
	
	public void onMessage(Object msg) {
		this.local().onMessage(msg, this);
	}
}
