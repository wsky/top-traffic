package org.codesharp.traffic;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Node {
	protected MessageHandle handle;
	
	private NodeProxy next;
	private Map<Object, Connection> nodes = new HashMap<Object, Connection>();
	private Map<Object, Connection[]> routes = new HashMap<Object, Connection[]>();
	private Random random = new Random();
	
	public Node(MessageHandle handle) {
		this.handle = handle;
	}
	
	public abstract Object flag();
	
	protected abstract void process(Object msg);
	
	public void setNext(NodeProxy next) {
		this.next = next;
	}
	
	public void accept(NodeProxy remote, Connection conn) {
		this.nodes.put(conn.id(), conn);
		this.routes.put(remote.flag(), new Connection[] { conn });
	}
	
	public void onMessage(Object msg, Connection from) {
		byte cmd = this.handle.getCommand(msg);
		switch (cmd) {
		case Commands.MSG:
			this.route(msg);
			break;
		case Commands.ACK:
			this.forward(msg);
			break;
		default:
			Asserter.throwUnsupportedCommand(cmd);
			break;
		}
	}
	
	private void route(Object msg) {
		Object dst = this.handle.getDestination(msg);
		Connection[] nodes = this.routes.get(dst);
		
		if (nodes != null && nodes.length > 0)
			nodes[this.random.nextInt(nodes.length)].send(msg);
		else if (this.next != null)
			this.next.send(msg);
		else
			// drop msg as unknown destination
			System.err.println("drop unknown msg");
	}
	
	private void forward(Object msg) {
		Object next = this.handle.getNext(msg);
		
		if (next == null) {
			this.process(msg);
			return;
		}
		
		Connection n = this.nodes.get(next);
		if (n != null)
			n.send(msg);
		else
			// drop msg as path broken
			System.err.println("drop msg as next broken: " + next);
	}
}
