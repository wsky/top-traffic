package org.codesharp.traffic.netty;

import java.util.List;

import org.codesharp.traffic.Asserter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageDecoder extends ByteToMessageDecoder {
	enum State {
		LEN,
		PAYLOAD
	}
	
	private MessageHandleImpl handle;
	private int len;
	private State state = State.LEN;
	private ByteBuf buf;
	
	public MessageDecoder(MessageHandleImpl handle) {
		this.handle = handle;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		switch (this.state) {
		case LEN:
			if (!in.isReadable(MessageHandleImpl.LEN + 4))
				return;
			
			this.len = this.handle.getLen(in);
			this.buf = ctx.alloc().buffer(MessageHandleImpl.LEN + 4 + this.len);
			this.read(in, this.buf, MessageHandleImpl.LEN + 4);
			this.state = State.PAYLOAD;
			break;
		case PAYLOAD:
			if (in.readableBytes() < this.len)
				return;
			
			this.read(in, this.buf, this.len);
			this.state = State.LEN;
			out.add(this.buf);
			break;
		default:
			Asserter.throwUnsupported(null);
			break;
		}
	}
	
	private ByteBuf read(ByteBuf src, ByteBuf dst, int length) {
		boolean release = true;
		try {
			src.readBytes(dst, length);
			release = false;
			return dst;
		} finally {
			if (release) {
				dst.release();
			}
		}
	}
}
