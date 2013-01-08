package org.tigeress.server;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import org.tigeress.CommandDecoder;
import org.tigeress.CommandDecoderAndExecutor;
import org.tigeress.SwitchCommandExecutor;
import org.tigeress.CuteDBServer;
import org.tigeress.DispatchRequestHandler;
import org.tigeress.DispatchResponseHandler;
import org.tigeress.ReflectiveCommandExecutor;
import org.tigeress.connector.Connector;
import org.tigeress.connector.Request;

import redis.server.netty.SimpleRedisServer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;

public class CuteServer implements Runnable {

	private List<RequestProcessor> processors = new LinkedList<RequestProcessor>();

	public void addProcessor(RequestProcessor requestProcessor) {
		processors.add(requestProcessor);
	}

	@Override
	public void run() {
		System.out.println(processors.size());
		// setup the disruptor
		Disruptor<Request> disruptor = new Disruptor<Request>(
				new EventFactory<Request>() {

					@Override
					public Request newInstance() {
						return new Request();
					}

				}, Executors.newFixedThreadPool(processors.size()),
				new SingleThreadedClaimStrategy(1024),
				new BusySpinWaitStrategy());

		EventHandlerGroup<Request> group = disruptor
				.handleEventsWith(processors.get(0));
		if (processors.size() > 1) {
			for (int i = 1; i < processors.size(); i++) {
				group.then(processors.get(i));
			}
		}
		// disruptor.handleEventsWith(new CommandDecoderAndExecutor());
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
		// new Thread(new CuteServer()).run();
		CuteServer server = new CuteServer();
		//server.addProcessor(new CommandDecoder());
		//server.addProcessor(new ReflectiveCommandExecutor(
			//	new SimpleRedisServer()));
		server.addProcessor(new CommandDecoderAndExecutor());
		new Thread(server).start();
	}
}
