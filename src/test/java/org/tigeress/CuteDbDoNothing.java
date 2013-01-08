package org.tigeress;

import org.tigeress.connector.Connector;

public class CuteDbDoNothing  implements Runnable{
	@Override
	public void run() {
		Connector connector = new Connector(6380, new DoNothingRequestHandler(), new DispatchResponseHandler());

		try {
			new Thread(connector).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String...strings ){
		new Thread(new CuteDbDoNothing()).start();
	}
}

