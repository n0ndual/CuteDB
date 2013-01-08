package org.tigeress;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.tigeress.SwitchCommandExecutor.Wrapper;
import org.tigeress.connector.Request;
import org.tigeress.server.RequestProcessor;

import redis.bytebuffer.DynamicByteBuffer;
import redis.bytebuffer.Reply;
import redis.server.netty.RedisException;
import redis.server.netty.RedisServer;
import redis.util.BytesKey;

public class ReflectiveCommandExecutor implements RequestProcessor {
	private static final Map<BytesKey, Wrapper> methods = new HashMap<BytesKey, Wrapper>();
	private RedisServer redis;

	interface Wrapper {
		Reply execute(Command command) throws RedisException;
	}

	public ReflectiveCommandExecutor(final RedisServer rs) {
		this.redis = rs;
		Class<? extends RedisServer> aClass = rs.getClass();
		for (final Method method : aClass.getMethods()) {
			final Class<?>[] types = method.getParameterTypes();
			methods.put(new BytesKey(method.getName().getBytes()),
					new Wrapper() {
						@Override
						public Reply execute(Command command)
								throws RedisException {
							Object[] objects = new Object[types.length];
							try {
								command.toArguments(objects, types);
								return (Reply) method.invoke(rs, objects);
							} catch (IllegalAccessException e) {
								throw new RedisException(
										"Invalid server implementation");
							} catch (InvocationTargetException e) {
								Throwable te = e.getTargetException();
								if (!(te instanceof RedisException)) {
									te.printStackTrace();
								}
								return null;
							}
						}
					});
		}
	}

	private static final byte LOWER_DIFF = 'a' - 'A';

	@Override
	public void onEvent(Request request, long sequence, boolean endOfBatch)
			throws Exception {
		reflectiveCall(request);
	}

	public void reflectiveCall(Request request) throws Exception {
		Command command = (Command) request.getInput();
		byte[] name = command.getName();
		System.out.println(new String(name));
		for (int i = 0; i < name.length; i++) {
			byte b = name[i];
			if (b >= 'A' && b <= 'Z') {
				name[i] = (byte) (b + LOWER_DIFF);
			}
		}
		Wrapper wrapper = methods.get(new BytesKey(name));
		Reply reply;
		// long start = System.nanoTime();
		reply = wrapper.execute(command);
		// long end = System.nanoTime();
		// System.out.println("reflective: "+(end-start));
		
		DynamicByteBuffer dynamic = new DynamicByteBuffer();
		reply.write(dynamic);
		request.getResponse().setOutput(dynamic.asByteBuffer());
		request.done();
	}
}
