package org.tigeress.connector;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DefaultWriter implements Writer {

	private ResponseHandler responseHandler;
	private Executor executor;

	static ByteBuffer byteBuffer;

	public DefaultWriter(ResponseHandler responseHandler) {
		super();
		this.responseHandler = responseHandler;
		executor = Executors.newFixedThreadPool(8);
	}

	public void write(final Response response) {
		// executor.execute(new Writer(response));
		// TODO Auto-generated method stub
		responseHandler.handle(response);
		/**
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					doWrite(response);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		**/
		try {
			doWrite(response);
			// notifier.fireOnWrite(response.getRequest(), response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void doWrite(Response response) throws Exception {
		/**
		 * 向客户端写数据
		 * 
		 * @param data
		 *            byte[]　待回应数据
		 */

		if (response.getOutput() instanceof ByteBuffer) {
			byteBuffer = (ByteBuffer) response.getOutput();
			response.getSocketChannel().setOption(
					StandardSocketOptions.TCP_NODELAY, false);
			response.getSocketChannel().write(byteBuffer);
		} else {
			byteBuffer = ByteBuffer
					.allocate(response.getOutputBytes().length);
			byteBuffer.put(response.getOutputBytes(), 0,
					response.getOutputBytes().length);
			byteBuffer.flip();

			response.getSocketChannel().setOption(
					StandardSocketOptions.TCP_NODELAY, false);
			response.getSocketChannel().write(byteBuffer);
		}
	}
	public static void directWrite(final Response response) {
		// executor.execute(new Writer(response));
		// TODO Auto-generated method stub
	
		try {
			if (response.getOutput() instanceof ByteBuffer) {
				byteBuffer = (ByteBuffer) response.getOutput();
				response.getSocketChannel().write(byteBuffer);
			} else {
				byteBuffer = ByteBuffer
						.allocate(response.getOutputBytes().length);
				byteBuffer.put(response.getOutputBytes(), 0,
						response.getOutputBytes().length);
				byteBuffer.flip();

				response.getSocketChannel().setOption(
						StandardSocketOptions.TCP_NODELAY, false);
				response.getSocketChannel().write(byteBuffer);
			}
			// notifier.fireOnWrite(response.getRequest(), response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
