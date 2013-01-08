package org.tigeress.connector;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * <p>
 * Title: 客户端请求信息类
 * </p>
 * 
 * @author starboy
 * @version 1.0
 */

public class Request {
	private SelectionKey selectionKey;
	private byte[] inputBytes = null;;
	private Object input;
	private Response response;

	public byte[] getInputBytes() {
		return inputBytes;
	}

	public void setInputBytes(byte[] inputBytes) {
		this.inputBytes = inputBytes;
	}

	public Object getInput() {
		return input;
	}

	public void setInput(Object input) {
		this.input = input;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public void doneByWritable() {
		this.response.setSelectionKey(this.selectionKey);
		Connector.responses.add(response);
		if (selectionKey.isValid()) {
			selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
		}
	}
	
	public void done(){
		DefaultWriter.directWrite(response);
	}

	public Request(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
		this.response = new Response(selectionKey);
	}

	public Request() {
	}

}
