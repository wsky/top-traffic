package org.codesharp.traffic.netty;

import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.Connection;
import org.codesharp.traffic.MessageHandle;
import org.codesharp.traffic.Node;
import org.codesharp.traffic.drpc.DRPCTrafficTest;
import org.codesharp.traffic.drpc.Frontend;

public class TrafficTest extends DRPCTrafficTest {
	private DRPCMessageHandleImpl handle = new DRPCMessageHandleImpl(ByteBufAllocator.DEFAULT);
	
	public TrafficTest() {
		n1_flag = 1L;
		n2_flag = 2L;
		n3_flag = 3L;
		n4_flag = 4L;
		n2_n1_id = 201L;
		n2_n3_id = 203L;
		n2_n4_id = 204L;
		n3_n4_id = 304L;
	}
	
	@Override
	protected MessageHandle newHandle() {
		return handle;
	}
	
	@Override
	protected Object newMessage(Object dst) {
		return String.format("{%s:'%s',%s:1,%s:%s}",
				DRPCMessageHandleImpl.TYPE,
				DRPCMessageHandleImpl.REQ,
				DRPCMessageHandleImpl.ID,
				DRPCMessageHandleImpl.DST,
				dst);
	}
	
	@Override
	protected Object newAck(Object msg) {
		return String.format("{%s:'%s',%s:1}",
				DRPCMessageHandleImpl.TYPE,
				DRPCMessageHandleImpl.REP,
				DRPCMessageHandleImpl.ID);
	}
	
	@Override
	protected Frontend newFrontend(final Object id, final Node local, final Connection remote) {
		return new NettyFontend(local, handle) {
			@Override
			public Object id() {
				return id;
			}
			
			@Override
			protected void internalSend(Object msg) {
				System.out.println(String.format("node#%s -> conn#%s: %s", local.flag(), this.id(), msg));
				if (remote != null)
					remote.onMessage(msg);
			}
			
			@Override
			public void onMessage(Object msg) {
				path.add(this.id());
				System.out.println(String.format("network -> conn#%s: %s", this.id(), msg));
				super.onMessage(msg);
			}
		};
	}
}
