package org.tigeress.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.tigeress.connector.Request;

public class Connector implements Runnable {
	public static ArrayBlockingQueue<Response> responses = new ArrayBlockingQueue<Response>(
			128);
	private static final int DEFAULT_PORT = 6380;
	private int port;
	private RequestHandler requestHandler;
	private ResponseHandler responseHandler;

	private static Selector selector;
	private ServerSocketChannel sschannel;
	private InetSocketAddress address;

	private Reader reader;
	private Writer writer;

	public Connector(int port, RequestHandler requestHandler,
			ResponseHandler responseHandler) {
		super();
		this.port = port;
		BlockingQueue list;
		// this.workerExecutor = Executors.newFixedThreadPool(nThreads);
		this.reader = new DefaultReader(requestHandler);
		this.writer = new DefaultWriter(responseHandler);
	}

	@Override
	public void run() {
		// ���������������׽�
		try {
			selector = Selector.open();

			sschannel = ServerSocketChannel.open();

			sschannel.configureBlocking(false);
			address = new InetSocketAddress(port);
			ServerSocket ss = sschannel.socket();
			ss.bind(address);
			sschannel.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("Server started ...");
			System.out.println("Server listening on port: " + port);
			// ����
			while (true) {
				try {
					int num = 0;
					num = selector.select();

					if (num > 0) {
						Set<SelectionKey> selectedKeys = selector
								.selectedKeys();
						Iterator<SelectionKey> it = selectedKeys.iterator();
						while (it.hasNext()) {
							SelectionKey key = (SelectionKey) it.next();
							it.remove();
							// ����IO�¼�
							if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
								// Accept the new connection
								ServerSocketChannel ssc = (ServerSocketChannel) key
										.channel();

								SocketChannel sc = ssc.accept();
								sc.configureBlocking(false);
								sc.setOption(StandardSocketOptions.TCP_NODELAY,
										false);
								// �������������¼�
								// ע�������,�Խ�����һ���Ķ�����
								sc.register(selector, SelectionKey.OP_READ);
							} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
								// �ύ�������̶߳�ȡ�ͻ�������
								SocketChannel sc = (SocketChannel) key
										.channel();
								reader.read(key);
								
								// �����û�ر�socketChannel����ôע�������
								// if (key.isValid()) {
								// key.interestOps(SelectionKey.OP_READ);
								// }
							} else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
								// �ύд�����߳���ͻ��˷��ͻ�Ӧ����
								// ��ʹ�������鷳�ķ�����д����ֱ�ӵ���socketChannel.write();
							//	System.out.println(responses.size() == 0);
								while (responses.size() != 0) {
									writer.write(responses.take());
								}
								key.interestOps(key.interestOps()
										& ~SelectionKey.OP_WRITE);
							}
						}
					} else {
						// addRegister(); // ��Selector��ע���µ�дͨ��
					}
				} catch (Exception e) {
					e.printStackTrace();

					continue;
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void write(Response response) {
		writer.write(response);
	}

	public static void main(String... strings) throws Exception, IOException {
		Connector connector = new Connector(6380, new RequestHandler() {

			@Override
			public void handle(Request request) {
				request.getResponse().setOutputBytes(request.getInputBytes());
				request.done();
			}

		}, new ResponseHandler() {

			@Override
			public void handle(Response response) {
				// TODO Auto-generated method stub
			}

		});
		try {
			new Thread(connector).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Thread.sleep(1000);
		Socket socket = new Socket("127.0.0.1", 6380);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.println("hello");
		byte[] result = new byte[1024];
		socket.getInputStream().read(result);
		System.out.println("reslut: " + new String(result));
		out.println("world");
		result = new byte[1024];
		socket.getInputStream().read(result);
		System.out.println("reslut: " + new String(result));

	}

}
