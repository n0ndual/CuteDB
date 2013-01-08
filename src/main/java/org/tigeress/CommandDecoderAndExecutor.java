package org.tigeress;

import org.tigeress.connector.Request;
import org.tigeress.server.RequestProcessor;

import redis.bytebuffer.DynamicByteBuffer;
import redis.bytebuffer.Reply;
import redis.server.netty.RedisServer;
import redis.server.netty.SimpleRedisServer;

import com.lmax.disruptor.EventHandler;

public class CommandDecoderAndExecutor implements RequestProcessor {
	private RedisServer redis= new SimpleRedisServer();
	private CommandDecoder decoder = new CommandDecoder();

	@Override
	public void onEvent(Request request, long sequence, boolean endOfBatch)
			throws Exception {
		// TODO Auto-generated method stub
		decoder.onEvent(request, 0, false);
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
