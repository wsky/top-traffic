package org.codesharp.traffic.netty;

import static org.junit.Assert.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.junit.Test;

public class MessageHandleImplTest {
	MessageHandleImpl handle = new MessageHandleImpl(ByteBufAllocator.DEFAULT);
	
	@Test
	public void new_msg_test() {
		ByteBuf msg = handle.newMessage(
				(byte) 1, 10, 4,
				"hello".getBytes(),
				(byte) 0,
				(short) 1,
				(int) 2,
				(long) 3);
		
		System.out.println(msg.writerIndex());
		System.out.println(msg.getInt(1));
		assertEquals(msg.writerIndex(), msg.getInt(1) + 5);
		assertEquals(1, handle.getCommand(msg));
		assertEquals(10L, handle.getDestination(msg));
		assertEquals((byte) 0, handle.getHeader(msg, 0));
		assertEquals((short) 1, handle.getHeader(msg, 1));
		assertEquals((int) 2, handle.getHeader(msg, 2));
		assertEquals((long) 3, handle.getHeader(msg, 3));
		assertNull(handle.getNext(msg));
		assertBody(msg, 5, "hello");
		
		msg = handle.newMessage((byte) 1, 10, 0, "hello".getBytes());
		System.out.println(msg.writerIndex());
		System.out.println(msg.getInt(1));
	}
	
	@Test
	public void header_test() {
		ByteBuf msg = handle.newMessage((byte) 1, 10, 4, "hello".getBytes(), (byte) 0);
		assertEquals((byte) 0, handle.getHeader(msg, 0));
		
		msg = handle.newMessage((byte) 1, 10, 4, "hello".getBytes(), (byte) 0, (short) 1);
		assertEquals((byte) 0, handle.getHeader(msg, 0));
		assertEquals((short) 1, handle.getHeader(msg, 1));
		
		msg = handle.newMessage((byte) 1, 10, 4, "hello".getBytes(), (byte) 0, (byte) 1);
		assertEquals((byte) 0, handle.getHeader(msg, 0));
		assertEquals((byte) 1, handle.getHeader(msg, 1));
		
		msg = handle.newMessage((byte) 1, 10, 4, "hello".getBytes(), (byte) 0);
	}
	
	@Test(expected = RuntimeException.class)
	public void header_overflow_test() {
		ByteBuf msg = handle.newMessage((byte) 1, 10, 4, "hello".getBytes(), (byte) 0);
		assertEquals((short) 1, handle.getHeader(msg, 1));
	}
	
	@Test
	public void path_test() {
		ByteBuf msg = handle.newMessage((byte) 1, 10, 0, "hello".getBytes());
		assertNull(handle.getNext(msg));
		
		msg = handle.newMessage((byte) 1, 10, 2, "hello".getBytes());
		handle.append(msg, 1L);
		assertEquals(1L, handle.getNext(msg));
		assertNull(handle.getNext(msg));
		
		path_test(1);
		path_test(2);
		path_test(5);
		path_test(10);
	}
	
	@Test(expected = RuntimeException.class)
	public void path_overflow_test() {
		ByteBuf msg = handle.newMessage((byte) 1, 10, 1, "hello".getBytes());
		handle.append(msg, 1L);
		handle.append(msg, 1L);
	}
	
	@Test
	public void set_command_test() {
		ByteBuf msg = handle.newMessage((byte) 1, 10, 1, "hello".getBytes());
		assertEquals(1, handle.getCommand(msg));
		handle.setCommand(msg, (byte) 2);
		assertEquals(2, handle.getCommand(msg));
	}
	
	@Test
	public void set_body_test() {
		ByteBuf msg = handle.newMessage((byte) 1, 10, 1, "1".getBytes());
		System.out.println(msg.writerIndex());
		assertBody(msg, 1, "1");
		
		int len = handle.getLen(msg);
		
		handle.setBody(msg, "12".getBytes());
		System.out.println(msg.writerIndex());
		assertEquals(len + 1, handle.getLen(msg));
		assertBody(msg, 2, "12");
	}
	
	private void path_test(int count) {
		ByteBuf msg = handle.newMessage((byte) 1, 10, count, "hello".getBytes());
		for (int i = 0; i < count; i++)
			handle.append(msg, new Long(i));
		
		while (count-- > 0)
			assertEquals(new Long(count), handle.getNext(msg));
	}
	
	private void assertBody(Object msg, int expectedLen, String expectedString) {
		ByteBuf body = handle.getMessageBody(msg);
		assertEquals(expectedLen, body.readableBytes());
		byte[] bytes = new byte[body.readableBytes()];
		body.readBytes(bytes);
		assertEquals(expectedString, new String(bytes));
	}
}
