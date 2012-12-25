package org.tigeress.connector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

public class ReaderPool {
	private Executor executor;
	private RequestHandler requestHandler;
	

	public ReaderPool(Executor executor, RequestHandler requestHandler) {
		super();
		this.executor = executor;
		this.requestHandler = requestHandler;
	
	}

	public void processRead(SelectionKey key) {
		// executor.execute(new Reader(key, notifier));
		new Reader(key).run();
	}

	class Reader implements Runnable {

		private SelectionKey key;
		

		public Reader(SelectionKey key) {
			this.key = key;
			
		}

		@Override
		public void run() {
			try {
				// 读取客户端数据
				SocketChannel sc = (SocketChannel) key.channel();
				byte[] clientData = readRequest(key,sc);
				if (clientData == null) {
					return;
				}
				Request request = (Request) key.attachment();
				request.setDataInput(clientData);

			

				// dispatch给应用逻辑来处理request
				requestHandler.handle(request);
			} catch (Exception e) {
				e.printStackTrace();
			
			}

		}

		private static final int BUFFER_SIZE = 1024;

		public byte[] readRequest(SelectionKey sk,SocketChannel sc) throws IOException {
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			int off = 0;
			int r = 0;
			byte[] data = new byte[BUFFER_SIZE * 10];
			while (true) {
				buffer.clear();
				r = sc.read(buffer);
				//如果客户端关闭了socket，那么就关闭channel，取消事件
				if (r == -1) {
					sk.cancel();
					sc.close();
					return null;
				}
				//读到0说明这次发送的消息已经读完
				if (r == 0) {
					break;
				}
				if ((off + r) > data.length) {
					data = grow(data, BUFFER_SIZE * 10);
				}
				byte[] buf = buffer.array();
				System.arraycopy(buf, 0, data, off, r);
				off += r;
			}
			byte[] req = new byte[off];
			System.arraycopy(data, 0, req, 0, off);
			return req;
		}

		/**
		 * 数组扩容
		 * 
		 * @param src
		 *            byte[] 源数组数据
		 * @param size
		 *            int 扩容的增加量
		 * @return byte[] 扩容后的数组
		 */
		public byte[] grow(byte[] src, int size) {
			byte[] tmp = new byte[src.length + size];
			System.arraycopy(src, 0, tmp, 0, src.length);
			return tmp;
		}

	}
}
