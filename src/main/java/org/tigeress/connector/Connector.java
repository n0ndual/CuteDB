package org.tigeress.connector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.tigeress.connector.Request;

public class Connector implements Runnable {
	private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime()
			.availableProcessors() * 2;
	private static final int DEFAULT_PORT = 6380;
	private Executor workerExecutor;
	private int port;
	private RequestHandler requestHandler;
	private ResponseHandler responseHandler;

	private static Selector selector;
	private ServerSocketChannel sschannel;
	private InetSocketAddress address;

	private ReaderPool readerPool;
	private WriterPool writerPool;

	public Connector(int port, RequestHandler requestHandler,
			ResponseHandler responseHandler) {
		super();
		this.port = port;
		this.workerExecutor = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
		this.readerPool = new ReaderPool(workerExecutor, requestHandler);
		this.writerPool = new WriterPool(workerExecutor, responseHandler
				);
	
	}

	@Override
	public void run() {
		// 创建无阻塞网络套接
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
			// 监听
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
							// 处理IO事件
							if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
								// Accept the new connection
								ServerSocketChannel ssc = (ServerSocketChannel) key
										.channel();
							

								SocketChannel sc = ssc.accept();
								sc.configureBlocking(false);

								// 触发接受连接事件
								Request request = new Request(sc, this);
							

								// 注册读操作,以进行下一步的读操作
								sc.register(selector, SelectionKey.OP_READ,
										request);
							} else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
								// 提交读服务线程读取客户端数据
								readerPool.processRead(key);
								//如果还没关闭socketChannel，那么注册继续读
							//	if (key.isValid()) {
						//			key.interestOps(SelectionKey.OP_READ);
							//	}
							} else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
								// 提交写服务线程向客户端发送回应数据
								//不使用这种麻烦的方法，写方法直接调用socketChannel.write();
							}

						}
					} else {
						// addRegister(); // 在Selector中注册新的写通道
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
		writerPool.processWrite(response);
	}

	public static void main(String... strings) throws Exception, IOException {
		Connector connector = new Connector(6380, new RequestHandler() {

			@Override
			public void handle(Request request) {
				request.getResponse()
						.setDataOutputBytes(request.getDataInput());
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
