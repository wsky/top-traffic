package org.codesharp.traffic.drpc;

import org.codesharp.traffic.Connection;
import org.codesharp.traffic.Node;
import org.codesharp.traffic.NodeTrafficTest;
import org.codesharp.traffic.drpc.SimpleDRPCMessageHandle.DRPCMessage;

public class DRPCTrafficTest extends NodeTrafficTest {
	@Override
	protected DRPCMessageHandle newHandle() {
		return new SimpleDRPCMessageHandle();
	}
	
	@Override
	protected Object newMessage(Object dst) {
		DRPCMessage msg = new DRPCMessage();
		msg.Command = DRPCMessage.REQ;
		msg.ID = "req_id";
		msg.Destination = dst;
		return msg;
	}
	
	@Override
	protected Object newAck(Object msg) {
		((DRPCMessage) msg).Command = DRPCMessage.REP;
		return msg;
	}
	
	@Override
	protected Connection newConnection(Object id, Object flag, Node local, Connection remote) {
		Connection conn = super.newConnection(id, flag, local, remote);
		return !id.equals(n2_n1_id)
				&& !id.equals(n2_n4_id)
				&& !id.equals(n3_n4_id)
				? conn : new Frontend(conn, newHandle());
	}
}
