package org.tigeress;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;

import org.tigeress.connector.Connector;
import org.tigeress.connector.Request;

import redis.server.netty.SimpleRedisServer;


import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.dsl.Disruptor;

public class CuteServer implements Runnable {

	@Override
	public void run() {

		// setup the disruptor
		Disruptor<Request> disruptor = new Disruptor<Request>(
				new EventFactory<Request>() {

					@Override
					public Request newInstance() {

						return new Request();
					}

				}, Executors.newFixedThreadPool(2),
				new MultiThreadedClaimStrategy(65536),
				new BlockingWaitStrategy());
		disruptor.handleEventsWith(new CommandDecoder()).then(
				new CommandExecutor(new SimpleRedisServer()));

		// start the disruptor
		disruptor.start();

		// start the connector
		Connector connector = new Connector(6380, new DispatchRequestHandler(
				disruptor), new DispatchResponseHandler());

		try {
			new Thread(connector).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String... strings) throws Exception {
		new Thread(new CuteServer()).run();

	}
}
