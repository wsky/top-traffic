package org.codesharp.traffic.netty;

import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

public class DRPCMessageHandleImplTest {
	@Test
	public void json_test() {
		Gson gson = new Gson();
		Map<?, ?> obj = (Map<?, ?>) gson.fromJson("{'k':'v', 'm':{'k':'v'}}", Object.class);
		System.out.println(obj);
		System.out.println(obj.getClass());
	}
}
