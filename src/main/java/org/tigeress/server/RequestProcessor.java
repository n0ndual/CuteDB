package org.tigeress.server;

import org.tigeress.connector.Request;

import com.lmax.disruptor.EventHandler;

public interface RequestProcessor extends EventHandler<Request>{

}
