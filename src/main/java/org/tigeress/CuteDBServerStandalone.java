package org.tigeress;

import org.tigeress.connector.Connector;

public class CuteDBServerStandalone implements Runnable{
	@Override
	public void run() {
		Connector connector = new Connector(6380, new SimpleRequestHandler(), new DispatchResponseHandler());

		try {
			new Thread(connector).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String...strings ){
		new Thread(new CuteDBServerStandalone()).start();
	}
}
