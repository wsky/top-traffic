package org.codesharp.traffic.drpc;

import org.codesharp.traffic.Connection;
import org.codesharp.traffic.MessageHandle;
import org.codesharp.traffic.Node;

public abstract class Gateway extends Node {
	public Gateway(MessageHandle handle) {
		super(handle);
	}
	
	@Override
	public Object flag() {
		return "Gateway";
	}
	
	@Override
	protected Connection route(Object msg, Connection from) {
		return from instanceof Frontend ? null : super.route(msg, from);
	}
}
