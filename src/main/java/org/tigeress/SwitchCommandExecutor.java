package org.tigeress;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import redis.bytebuffer.DynamicByteBuffer;
import redis.bytebuffer.Reply;
import redis.server.netty.RedisException;
import redis.server.netty.RedisServer;
import redis.util.BytesKey;

import com.google.common.base.Charsets;
import com.lmax.disruptor.EventHandler;
import org.tigeress.connector.Request;
import org.tigeress.server.RequestProcessor;

public class SwitchCommandExecutor implements RequestProcessor {
	private static final Map<BytesKey, Wrapper> methods = new HashMap<BytesKey, Wrapper>();
	private RedisServer redis;

	interface Wrapper {
		Reply execute(Command command) throws RedisException;
	}

	public SwitchCommandExecutor(final RedisServer rs) {
		this.redis = rs;
	}

	private static final byte LOWER_DIFF = 'a' - 'A';

	@Override
	public void onEvent(Request request, long sequence, boolean endOfBatch)
			throws Exception {
		switchCall(request);
	}

	public void switchCall(Request request) throws Exception {
		// 试一试简单的方法调用比反射调用快多少
		Reply reply = null;
		Command cmd = (Command) request.getInput();
		byte[] name = cmd.getName();
		switch (new String(name)) {
		case "DEL":
			Object[] delV = new Object[1];
			Class<?>[] delT = { byte[][].class };
			cmd.toArguments(delV, delT);
			reply = redis.del((byte[][]) delV[0]);
			break;
		case "PING":
			reply = redis.ping();
			break;
		case "INFO":
			reply = redis.info();
			break;
		case "GET":
			Object[] arguments = new Object[1];
			Class<?>[] types = { byte[].class };
			cmd.toArguments(arguments, types);
			reply = redis.get((byte[]) arguments[0]);
			break;
		case "SET":
			Object[] arguments0 = new Object[2];
			Class<?>[] types0 = { byte[].class, byte[].class };
			cmd.toArguments(arguments0, types0);
			reply = redis.set((byte[]) arguments0[0], (byte[]) arguments0[1]);
			break;
		}

		DynamicByteBuffer dynamic = new DynamicByteBuffer();
		reply.write(dynamic);
		request.getResponse().setOutput(dynamic.asByteBuffer());
		request.done();
	}

	
}
