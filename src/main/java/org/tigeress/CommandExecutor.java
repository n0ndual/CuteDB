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

public class CommandExecutor implements EventHandler<Request> {
	private static final Map<BytesKey, Wrapper> methods = new HashMap<BytesKey, Wrapper>();
	private RedisServer redis;

	interface Wrapper {
		Reply execute(Command command) throws RedisException;
	}

	public CommandExecutor(final RedisServer rs) {
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

	public void switchCall(Request request) throws Exception {
		// 试一试简单的方法调用比反射调用快多少
		Reply reply = null;
		Command cmd = (Command) request.getAttchment();
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
		request.getResponse().setDataOutput(dynamic.asByteBuffer());
		request.done();
	}

	public void reflectiveCall(Request request) throws Exception {
		Command command = (Command) request.getAttchment();
		byte[] name = command.getName();
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
		request.getResponse().setDataOutput(dynamic.asByteBuffer());
		request.done();
	}
}
