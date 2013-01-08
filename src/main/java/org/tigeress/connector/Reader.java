package org.tigeress.connector;

import java.nio.channels.SelectionKey;

public interface Reader {
	
	public void read(SelectionKey selectionKey);
}
