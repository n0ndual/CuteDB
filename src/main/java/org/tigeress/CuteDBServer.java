package org.tigeress;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;

import org.tigeress.connector.Connector;
import org.tigeress.connector.Request;
import org.tigeress.server.CuteServer;

import redis.server.netty.SimpleRedisServer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

public class CuteDBServer {

	public static void main(String... strings) throws Exception {
		CuteServer server = new CuteServer();
		server.addProcessor(new CommandDecoder());
		server.addProcessor(new ReflectiveCommandExecutor(new SimpleRedisServer()));
		new Thread(server).start();
	}
}
