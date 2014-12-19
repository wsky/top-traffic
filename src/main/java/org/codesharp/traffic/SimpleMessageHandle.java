package org.codesharp.traffic;

import java.util.Arrays;
import java.util.Stack;

public class SimpleMessageHandle implements MessageHandle {
	public byte getCommand(Object msg) {
		return ((Message) msg).Command;
	}
	
	public Object getDestination(Object msg) {
		return ((Message) msg).Destination;
	}
	
	public Object getNext(Object msg) {
		Stack<Object> path = ((Message) msg).Path;
		return path.isEmpty() ? null : path.pop();
	}
	
	public Object append(Object msg, Object from) {
		((Message) msg).Path.add(from);
		return msg;
	}
	
	public static class Message {
		public byte Command;
		public byte Type;
		public Object Destination;
		public Stack<Object> Path = new Stack<Object>();
		public Object Body;
		
		@Override
		public String toString() {
			return String.format("%s|%s|%s|%s|%s",
					this.Command,
					this.Type,
					this.Destination,
					this.Body,
					Arrays.toString(this.Path.toArray()));
		}
	}
}
