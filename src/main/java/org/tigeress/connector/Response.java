package org.tigeress.connector;

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;

/**
 * <p>Title: 回应器</p>
 * <p>Description: 用于向客户端发送数据</p>
 * @author starboy
 * @version 1.0
 */

public class Response {
    private SocketChannel socketChannel;
    private Request request;
    private Object dataOutput;
    private byte[] dataOutputBytes;
    
    public Response(SocketChannel socketChannel, Request request) {
		super();
		this.socketChannel = socketChannel;
		this.request = request;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public Object getDataOutput() {
		return dataOutput;
	}

	public void setDataOutput(Object dataOutput) {
		this.dataOutput = dataOutput;
	}

	public byte[] getDataOutputBytes() {
		return dataOutputBytes;
	}

	public void setDataOutputBytes(byte[] dataOutputBytes) {
		this.dataOutputBytes = dataOutputBytes;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Response(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

   
}
