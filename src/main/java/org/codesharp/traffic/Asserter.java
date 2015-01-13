package org.codesharp.traffic;

public class Asserter {
	public static RuntimeException exception(String msg) {
		return new RuntimeException(msg);
	}
	
	public static RuntimeException unsupported(Object msg) {
		return exception(String.format("unsupported: %s", msg));
	}
	
	public static void throwUnsupported(Object msg) {
		throw unsupported(msg);
	}
	
	public static void throwUnsupportedCommand(byte cmd) {
		throwUnsupported(String.format("command=%s", cmd));
	}
	
	public static Object throwIfOutgoingNotExists(Object msg, Object id) {
		if (msg == null)
			throw exception(String.format("outgoing not exists: %s", id));
		return msg;
	}
	
	public static void throwUnsupportedHeader(Object o) {
		throwUnsupported(String.format("header type %s", o));
	}
	
	public static RuntimeException overflow(String who, int index) {
		return exception(String.format("%s ovreflow at: %s", who, index));
	}
}
