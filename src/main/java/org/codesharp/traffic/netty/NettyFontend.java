package org.codesharp.traffic.netty;

import org.codesharp.traffic.Node;
import org.codesharp.traffic.drpc.Frontend;

public class NettyFontend extends Frontend {
	private DRPCMessageHandleImpl handleImpl;
	
	public NettyFontend(Node local, DRPCMessageHandleImpl handle) {
		super(local, handle);
		this.handleImpl = handle;
	}
	
	@Override
	public Object id() {
		return null;
	}
	
	@Override
	public void onMessage(Object msg) {
		super.onMessage(this.handleImpl.resolve(msg));
	}
	
	@Override
	protected void internalSend(Object msg) {
	}
}
