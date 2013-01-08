package org.tigeress.connector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.Executor;

public class DefaultReader implements Reader {

	byte[] receivedData;

	private RequestHandler requestHandler;

	public DefaultReader(RequestHandler requestHandler){
		this.requestHandler=requestHandler;
	}
	
	@Override
	public void read(SelectionKey selectionKey) {
		try {
			// 读取客户端数据
			SocketChannel sc = (SocketChannel) selectionKey.channel();
			receivedData = readRequest(selectionKey, sc);
			if (receivedData == null || receivedData.length == 0) {
				return;
			}
			Request request = new Request(selectionKey);
			request.setInputBytes(receivedData);
			// dispatch给应用逻辑来处理request
			requestHandler.handle(request);
		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	private static final int BUFFER_SIZE = 128;
	private int off;
	private int r;
	private byte[] data;
	private byte[] buf;
	private byte[] received;

	public byte[] readRequest(SelectionKey sk, SocketChannel sc)
			throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		off = 0;
		r = 0;
		data = new byte[BUFFER_SIZE];
		while (true) {
			buffer.clear();
			r = sc.read(buffer);
			// 如果客户端关闭了socket，那么就关闭channel，取消事件
			if (r == -1) {
				sk.cancel();
				sc.close();
				return null;
			}
			// 这里有问题，应当取消这一次的读取，并重新注册读事件
			// 读到0说明这次发送的消息已经读完
			if (r == 0) {
				break;
			}
			if ((off + r) > data.length) {
				data = grow(data, BUFFER_SIZE * 10);
			}
			buf = buffer.array();
			System.arraycopy(buf, 0, data, off, r);
			off += r;
		}
		received = new byte[off];
		System.arraycopy(data, 0, received, 0, off);
		return received;
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

	public static void main(String... args) {
		byte[] req = new byte[0];
		System.out.println("req == null?: " + req == null);

	}
}
