package org.codesharp.traffic;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Node {
	private MessageHandle handle;
	
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
	
	public void accept(Connection conn) {
		this.nodes.put(conn.id(), conn);
		// FIXME connection list
		this.routes.put(conn.flag(), new Connection[] { conn });
	}
	
	public void onMessage(Object msg, Connection from) {
		byte cmd = this.handle.getCommand(msg);
		switch (cmd) {
		case Commands.MSG:
			this.internalOnMessage(this.handle.append(msg, from.id()), from);
			break;
		case Commands.ACK:
			this.internalOnAck(msg);
			break;
		default:
			Asserter.throwUnsupportedCommand(cmd);
			break;
		}
	}
	
	protected void internalOnMessage(Object msg, Connection from) {
		Object dst = this.handle.getDestination(msg);
		
		// FIXME dst match with flag, should be included in route()
		if (this.flag().equals(dst)) {
			this.process(msg);
			return;
		}
		
		Connection conn = this.route(dst, from);
		if (conn != null) {
			conn.send(msg);
			return;
		}
		
		if (this.next != null) {
			this.next.send(msg);
			return;
		}
		
		this.internalOnAck(this.handle.unknownDestination(msg));
		System.out.println("[ERROR] drop msg as unknown destination: " + dst);
	}
	
	protected void internalOnAck(Object msg) {
		Object next = this.handle.getNext(msg);
		
		if (next == null) {
			this.process(msg);
			return;
		}
		
		Connection n = this.nodes.get(next);
		if (n != null)
			n.send(msg);
		else
			System.out.println("[ERROR] drop msg as next broken: " + next);
	}
	
	protected Connection route(Object dst, Connection from) {
		Connection[] nodes = this.routes.get(dst);
		return nodes != null && nodes.length > 0 ?
				nodes[this.random.nextInt(nodes.length)] : null;
	}
	
}
