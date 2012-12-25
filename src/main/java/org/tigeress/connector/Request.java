package org.tigeress.connector;

import java.nio.channels.SocketChannel;
/**
 * <p>Title: 客户端请求信息类</p>
 * @author starboy
 * @version 1.0
 */

public class Request {
    private SocketChannel sc;
    private byte[] dataInput = null;;
    Object attchment;
    private Response response;
    private Connector connector;
    
    public SocketChannel getSc() {
		return sc;
	}
	public void setSc(SocketChannel sc) {
		this.sc = sc;
	}
	public Object getAttchment() {
		return attchment;
	}
	public void setAttchment(Object attchment) {
		this.attchment = attchment;
	}
	public Connector getConnector() {
		return connector;
	}
	public void setConnector(Connector connector) {
		this.connector = connector;
	}
	public Response getResponse() {
		return response;
	}
	public void setResponse(Response response) {
		this.response = response;
	}
	public void done(){
		this.connector.write(response);
	}
	
	public Request(){
		
	}
	public Request(SocketChannel sc,Connector connector) {
        this.sc = sc;
        this.connector=connector;
        this.response = new Response(sc,this);
    }
	public Request(SocketChannel sc){
		this.sc = sc;
	}
    public java.net.InetAddress getAddress() {
        return sc.socket().getInetAddress();
    }
    public int getPort() {
        return sc.socket().getPort();
    }
    public boolean isConnected() {
        return sc.isConnected();
    }
    public boolean isBlocking() {
        return sc.isBlocking();
    }
    public boolean isConnectionPending() {
        return sc.isConnectionPending();
    }
    public boolean getKeepAlive() throws java.net.SocketException {
        return sc.socket().getKeepAlive();
    }
    public int getSoTimeout() throws java.net.SocketException {
        return sc.socket().getSoTimeout();
    }
    public boolean getTcpNoDelay() throws java.net.SocketException {
        return sc.socket().getTcpNoDelay();
    }
    public boolean isClosed() {
        return sc.socket().isClosed();
    }
    public void attach(Object obj) {
        this.attchment = obj;
    }
    public Object attachment() {
        return attchment;
    }
    public byte[] getDataInput() {
        return dataInput;
    }
    public void setDataInput(byte[] dataInput) {
        this.dataInput = dataInput;
    }
    

}
