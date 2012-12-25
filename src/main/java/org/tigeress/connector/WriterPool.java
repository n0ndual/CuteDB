package org.tigeress.connector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class WriterPool {

	private Executor executor;

	private ResponseHandler responseHandler;

	public WriterPool(Executor executor, ResponseHandler responseHandler) {
		super();
		this.executor = executor;
		this.responseHandler = responseHandler;
	}

	public void processWrite(Response response) {
		executor.execute(new Writer(response));
	}

	class Writer implements Runnable {

		private Response response;

		public Writer(Response response) {
			this.response = response;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			responseHandler.handle(response);
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

			if (response.getDataOutput() instanceof ByteBuffer) {
				ByteBuffer byteBuffer = (ByteBuffer) response.getDataOutput();
				response.getSocketChannel().write(byteBuffer);
			} else {
				ByteBuffer buffer = ByteBuffer.allocate(response
						.getDataOutputBytes().length);
				buffer.put(response.getDataOutputBytes(), 0,
						response.getDataOutputBytes().length);
				buffer.flip();
				response.getSocketChannel().write(buffer);
			}
		}
	}

}
