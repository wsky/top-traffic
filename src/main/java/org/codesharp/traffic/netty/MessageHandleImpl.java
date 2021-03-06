package org.codesharp.traffic.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.codesharp.traffic.Asserter;
import org.codesharp.traffic.Commands;
import org.codesharp.traffic.MessageHandle;
import org.codesharp.traffic.Status;

/***
 * 
 * https://github.com/wsky/top-traffic/issues/2#issuecomment-66725466
 * 
 * Internal message protocol
 * 
 * +-----+--------+-----+-----+------------+--------------+----------+-------------+----------+------+
 * | CMD | Status | Len | Dst | Header-Len |   Headers    | Path-Len |    Path     | Body-Len | Body |
 * |     |        |     |     |            |--------------|          |-------------|          |      |
 * |     |        |     |     |            | Type | Value |          | Flag | Path |          |      |
 * +-----+--------+-----+-----+------------+------+-------+----------+------+------+----------+------+
 * |  1  |    1   |  4  |  8  |     1      |   1  |   N   |    1     |   1  |   8  |     4    |  N   |
 * +-----+--------+-----+-----+------------+------+-------+----------+------+------+----------+------+
 * | 0/1 |  byte  | int | long|    byte    | 1/2/ |       |   byte   |  0/1 | long |    int   |      |
 * +-----+--------+-----+-----+------------+------+-------+----------+------+------+----------+------+
 */
public class MessageHandleImpl implements MessageHandle {
	public final static int LEN = 1 + 1;
	public final static int DST = LEN + 4;
	public final static int HEADER_LEN = DST + 8;
	public final static int PATH_SIZE = 1 + 8;
	public final static byte PATH_UNSET = 0;
	public final static byte PATH_SET = 1;
	
	protected ByteBufAllocator allocator;
	
	public MessageHandleImpl(ByteBufAllocator allocator) {
		this.allocator = allocator;
	}
	
	public String toString(Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		ByteBuf body = this.getMessageBody(msg);
		byte[] bytes = new byte[body.readableBytes()];
		body.readBytes(bytes);
		return String.format(
				"cmd=%s, status=%s, len=%s, dst=%s, header-len=%s, body=%s",
				this.getCommand(buf),
				this.getStatus(buf),
				this.getLen(buf),
				this.getDestination(buf),
				this.getHeaderLen(buf),
				new String(bytes));
	}
	
	public ByteBuf newMessage(byte cmd, byte status, long dst, int pathCount, byte[] body, Object... headers) {
		ByteBuf buf = this.allocator.buffer();
		buf.resetWriterIndex();
		buf.writeByte(cmd);
		buf.writeByte(status);
		
		int len = 0;
		buf.writeInt(len);
		
		buf.writeLong(dst);
		len += 8;
		
		byte headerLen = 0;
		buf.writeByte(headerLen);
		len += 1;
		
		for (Object o : headers) {
			if (o instanceof Byte) {
				buf.writeByte(1).writeByte((Byte) o);
				headerLen += (1 + 1);
			} else if (o instanceof Short) {
				buf.writeByte(2).writeShort((Short) o);
				headerLen += (1 + 2);
			} else if (o instanceof Integer) {
				buf.writeByte(3).writeInt((Integer) o);
				headerLen += (1 + 4);
			} else if (o instanceof Long) {
				buf.writeByte(4).writeLong((Long) o);
				headerLen += (1 + 8);
			} else
				Asserter.throwUnsupportedHeader(o != null ? o.getClass() : o);
		}
		buf.setByte(HEADER_LEN, headerLen);
		len += headerLen;
		
		byte pathLen = (byte) (pathCount * PATH_SIZE);
		buf.writeByte(pathLen);
		len += (1 + pathLen);
		
		for (int i = 0; i < pathCount; i++) {
			buf.writeByte(PATH_UNSET);
			buf.writeLong(0);
		}
		
		buf.writeInt(body.length);
		buf.writeBytes(body);
		len += (4 + body.length);
		
		this.setLen(buf, len);
		
		return buf;
	}
	
	public byte getCommand(Object msg) {
		return ((ByteBuf) msg).getByte(0);
	}
	
	public byte getStatus(Object msg) {
		return ((ByteBuf) msg).getByte(1);
	}
	
	public Object getDestination(Object msg) {
		return ((ByteBuf) msg).getLong(DST);
	}
	
	public Object getHeader(Object msg, int index) {
		ByteBuf buf = (ByteBuf) msg;
		
		int headerLen = this.getHeaderLen(buf);
		int read = HEADER_LEN + 1;
		int end = HEADER_LEN + headerLen;
		
		int i = 0;
		while (read < end) {
			byte t = buf.getByte(read++);
			Object v = null;
			
			if (t == 1) {
				v = buf.getByte(read);
				read += 1;
			} else if (t == 2) {
				v = buf.getShort(read);
				read += 2;
			} else if (t == 3) {
				v = buf.getInt(read);
				read += 4;
			} else if (t == 4) {
				v = buf.getLong(read);
				read += 8;
			} else
				Asserter.throwUnsupportedHeader(t);
			
			if (i++ == index)
				return v;
		}
		
		throw Asserter.overflow("header", index);
	}
	
	public Object getNext(Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		
		int headerLen = this.getHeaderLen(buf);
		int pathLen = buf.getByte(HEADER_LEN + 1 + headerLen);
		int pathBegin = HEADER_LEN + 1 + headerLen + 1;
		int pathCount = pathLen / PATH_SIZE;
		
		while (pathCount-- > 0) {
			int pathFlag = pathBegin + pathCount * PATH_SIZE;
			if (buf.getByte(pathFlag) == PATH_UNSET)
				continue;
			buf.setByte(pathFlag, PATH_UNSET);
			return buf.getLong(pathFlag + 1);
		}
		return null;
	}
	
	public Object append(Object msg, Object from) {
		ByteBuf buf = (ByteBuf) msg;
		
		int headerLen = this.getHeaderLen(buf);
		int pathLen = buf.getByte(HEADER_LEN + 1 + headerLen);
		int pathBegin = HEADER_LEN + 1 + headerLen + 1;
		int pathCount = pathLen / PATH_SIZE;
		
		for (int i = 0; i < pathCount; i++) {
			int pathFlag = pathBegin + i * PATH_SIZE;
			if (buf.getByte(pathFlag) == PATH_SET)
				continue;
			buf.setByte(pathFlag, PATH_SET);
			buf.setLong(pathFlag + 1, (Long) from);
			return msg;
		}
		
		throw Asserter.overflow("path", pathCount);
	}
	
	public ByteBuf getMessageBody(Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		
		int headerLen = this.getHeaderLen(buf);
		int pathLen = buf.getByte(HEADER_LEN + 1 + headerLen);
		int bodyLen = buf.getInt(HEADER_LEN + 1 + headerLen + 1 + pathLen);
		int bodyBegin = HEADER_LEN + 1 + headerLen + 1 + pathLen + 4;
		
		buf.setIndex(bodyBegin, bodyBegin + bodyLen);
		return buf;
	}
	
	public Object unknownDestination(Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		this.setCommand(buf, Commands.ACK);
		this.setStatus(buf, Status.UNKNOWN_DESTINATION);
		return msg;
	}
	
	protected void setCommand(ByteBuf buf, byte cmd) {
		buf.setByte(0, cmd);
	}
	
	protected void setStatus(ByteBuf buf, byte status) {
		buf.setByte(1, status);
	}
	
	protected void setBody(ByteBuf buf, byte[] body) {
		int len = this.getLen(buf);
		int headerLen = this.getHeaderLen(buf);
		int pathLen = buf.getByte(HEADER_LEN + 1 + headerLen);
		int bodyLen = buf.getInt(HEADER_LEN + 1 + headerLen + 1 + pathLen);
		int bodyBegin = HEADER_LEN + 1 + headerLen + 1 + pathLen + 4;
		
		// reset len
		this.setLen(buf, len + (body.length - bodyLen));
		// reset body-len
		buf.setInt(HEADER_LEN + 1 + headerLen + 1 + pathLen, body.length);
		buf.setIndex(0, bodyBegin);
		buf.writeBytes(body);
	}
	
	protected int getLen(ByteBuf buf) {
		return buf.getInt(LEN);
	}
	
	protected void setLen(ByteBuf buf, int len) {
		buf.setInt(LEN, len);
	}
	
	protected int getHeaderLen(ByteBuf buf) {
		return buf.getByte(HEADER_LEN);
	}
}
