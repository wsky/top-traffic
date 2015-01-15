package org.codesharp.traffic.netty;

import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.drpc.DRPCTrafficTest;

public class TrafficTest extends DRPCTrafficTest {
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
	protected DRPCMessageHandleImpl newHandle() {
		return new DRPCMessageHandleImpl(ByteBufAllocator.DEFAULT);
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
}
