package org.tigeress.connector;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;

/**
 * <p>
 * Title: 回应器
 * </p>
 * <p>
 * Description: 用于向客户端发送数据
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class Response {
	private SelectionKey selectionKey;
	private Object output;
	private byte[] outputBytes;

	public Response(SelectionKey selectionKey) {
		super();
		this.selectionKey = selectionKey;
	}

	public SocketChannel getSocketChannel() {
		return (SocketChannel) selectionKey.channel();
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}

	public byte[] getOutputBytes() {
		return outputBytes;
	}

	public void setOutputBytes(byte[] outputBytes) {
		this.outputBytes = outputBytes;
	}



}
