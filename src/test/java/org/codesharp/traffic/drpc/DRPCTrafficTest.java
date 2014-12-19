package org.codesharp.traffic.drpc;

import org.codesharp.traffic.Connection;
import org.codesharp.traffic.MessageHandle;
import org.codesharp.traffic.Node;
import org.codesharp.traffic.NodeTrafficTest;
import org.codesharp.traffic.drpc.SimpleDRPCMessageHandle.DRPCMessage;

public class DRPCTrafficTest extends NodeTrafficTest {
	private DRPCMessageHandle handle = new SimpleDRPCMessageHandle();
	
	@Override
	protected MessageHandle newHandle() {
		return handle;
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
	protected Connection newConnection(final Object id, final Node local, final Connection remote) {
		if (!id.equals(n2_n1_id) && !id.equals(n2_n4_id) && !id.equals(n3_n4_id))
			return super.newConnection(id, local, remote);
		
		return new Frontend(local, handle) {
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
