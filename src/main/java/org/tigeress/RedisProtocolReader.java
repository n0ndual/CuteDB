package org.tigeress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import org.tigeress.connector.Reader;

/**
 * this is A Very Special Reader, as redis protocol is different from normal
 * protocols. for performance consideration, redis implements a protocol which
 * can be read and processed at the same time. the protocol always present the
 * length of any data structure before the content of them. so we can read and
 * process, instead of reading them all and then docoding.
 * 
 * this class not only do the nornal work of Reader, but also docode the data
 * received.
 * 
 * I'm actually not sure about if it's a proper way. 
 * 
 * @author clive
 * 
 */
public class RedisProtocolReader implements Reader {

	@Override
	public void read(SelectionKey selectionKey) {
		
		
	}
}
