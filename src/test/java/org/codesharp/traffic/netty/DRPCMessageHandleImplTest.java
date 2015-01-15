package org.codesharp.traffic.netty;

import static org.junit.Assert.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Map;

import org.codesharp.traffic.Commands;
import org.junit.Test;

import com.google.gson.Gson;

public class DRPCMessageHandleImplTest {
	DRPCMessageHandleImpl handle = new DRPCMessageHandleImpl(ByteBufAllocator.DEFAULT);
	String request = "{ type:'REQ', id:1024, to:1234 }";
	String request_out = "{\"type\":\"REQ\",\"id\":1024,\"to\":1234.0}";
	String reply = "{ type:'REP', id:1024 }";
	String reply_out = "{\"type\":\"REP\",\"id\":1024}";
	
	@Test
	public void json_test() {
		Gson gson = new Gson();
		Map<?, ?> obj = (Map<?, ?>) gson.fromJson("{'id':1024, to:'', 'body':{'k':'v'}}", Object.class);
		System.out.println(obj);
		System.out.println(obj.getClass());
	}
	
	@Test
	public void resolve_test() {
		Map<?, ?> msg = handle.resolve(request);
		assertTrue(handle.isRequest(msg));
		assertFalse(handle.isReply(msg));
		assertEquals(1024L, handle.getId(msg));
		
		msg = handle.resolve(reply);
		assertFalse(handle.isRequest(msg));
		assertTrue(handle.isReply(msg));
		assertEquals(1024L, handle.getReplyId(msg));
	}
	
	@Test
	public void internal_message_test() {
		Object msg = handle.newMessage(handle.resolve(request));
		assertEquals(Commands.MSG, handle.getCommand(msg));
		assertEquals(1234L, handle.getDestination(msg));
		assertEquals(1024L, handle.getIncomeId(msg));
		assertEquals(1024L, handle.getOutcomeId(msg).longValue());
		assertBody(msg, request_out);
		
		msg = handle.newAck(handle.resolve(reply), msg);
		assertEquals(Commands.ACK, handle.getCommand(msg));
		assertEquals(1234L, handle.getDestination(msg));
		assertEquals(1024L, handle.getIncomeId(msg));
		assertEquals(1024L, handle.getOutcomeId(msg).longValue());
		assertBody(msg, reply_out);
	}
	
	private void assertBody(Object msg, String expectedString) {
		ByteBuf body = handle.getMessageBody(msg);
		byte[] bytes = new byte[body.readableBytes()];
		body.readBytes(bytes);
		System.out.println(new String(bytes));
		assertEquals(expectedString, new String(bytes));
	}
}
