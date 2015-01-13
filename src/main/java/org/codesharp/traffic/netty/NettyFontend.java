package org.codesharp.traffic.netty;

import org.codesharp.traffic.Node;
import org.codesharp.traffic.drpc.DRPCMessageHandle;
import org.codesharp.traffic.drpc.Frontend;

public class NettyFontend extends Frontend {
	public NettyFontend(Node local, DRPCMessageHandle handle) {
		super(local, handle);
	}
	
	@Override
	protected void internalSend(Object msg) {
	}
	
	@Override
	public Object id() {
		return null;
	}
	
}
