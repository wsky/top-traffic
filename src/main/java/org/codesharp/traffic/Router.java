package org.codesharp.traffic;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class Router implements Node {
	private MessageHelper helper;
	private Map<Object, Node> nodes = new HashMap<Object, Node>();
	private Map<Object, Node[]> routes = new HashMap<Object, Node[]>();
	private Random random = new Random();
	
	public Router(MessageHelper helper) {
		this.helper = helper;
		this.register(this);
	}
	
	public void register(Node node) {
		this.nodes.put(node.id(), node);
		this.routes.put(node.flag(), new Node[] { node });
	}
	
	public void onMessage(Object msg) {
		byte cmd = this.helper.getCommand(msg);
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
		Object dst = this.helper.getDestination(msg);
		Node[] nodes = this.routes.get(dst);
		
		if (nodes != null && nodes.length > 0)
			nodes[this.random.nextInt(nodes.length)].send(msg);
		else if (this.next() != null)
			this.next().send(msg);
		else
			// drop msg as unknown destination
			// FIXME tell to client
			System.err.println("drop unknown msg");
	}
	
	private void forward(Object msg) {
		Object next = this.helper.getNext(msg);
		
		if (next == null) {
			this.process(msg);
			return;
		}
		
		Node n = this.nodes.get(next);
		if (n != null)
			n.send(msg);
		else
			// drop msg as path broken
			System.err.println("drop msg as next broken: " + next);
	}
	
	private void process(Object msg) {
		
	}
}
