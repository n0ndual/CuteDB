package org.tigeress;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.tigeress.connector.Connector;
import org.tigeress.connector.Request;
import org.tigeress.connector.RequestHandler;

import redis.bytebuffer.DynamicByteBuffer;
import redis.bytebuffer.Reply;
import redis.server.netty.RedisServer;
import redis.server.netty.SimpleRedisServer;

public class CuteDBServerWithThreadPool implements Runnable{

	@Override
	public void run() {
		Connector connector = new Connector(6380, new PooledRequestHandler(), new DispatchResponseHandler());

		try {
			new Thread(connector).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String...strings ){
		new Thread(new CuteDBServerWithThreadPool()).start();
	}
	
	public class PooledRequestHandler implements RequestHandler{

		private RedisServer redis;
		private CommandDecoder decoder = new CommandDecoder();
		private Executor executor = Executors.newSingleThreadScheduledExecutor();
		public PooledRequestHandler() {
			this.redis = new SimpleRedisServer();
		}

		public void handleRequest(Request request) throws Exception {
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

		@Override
		public void handle(final Request request) {
			try {
				//handleRequest(request);
				executor.execute(new Runnable(){

					@Override
					public void run() {
						try {
							handleRequest(request);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
