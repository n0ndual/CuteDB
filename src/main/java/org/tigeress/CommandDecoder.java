package org.tigeress;

import java.io.IOException;

import org.tigeress.connector.Request;
import org.tigeress.server.RequestProcessor;

import com.lmax.disruptor.EventHandler;

public class CommandDecoder implements RequestProcessor {

	private static final char CR = '\r';
	private static final char LF = '\n';
	private static final byte[] CRLF = { CR, LF };
	private static final char ZERO = '0';
	private byte[][] bytes = null;
	private int arguments = 0;

	@Override
	public void onEvent(Request request, long sequence, boolean endOfBatch)
			throws Exception {
		Command command = decode(request.getInputBytes(), new Cursor(0));
		request.setInput(command);
	}

	public Command decode(byte[] input, Cursor cursor) throws Exception {

		if (bytes != null) {
			int numArgs = bytes.length;
			for (int i = arguments; i < numArgs; i++) {
				if (input[cursor.getValue()] == '$') {
					cursor.increase(1);
					int size = readInt(input, cursor);
					if (size > Integer.MAX_VALUE) {
						throw new IllegalArgumentException(
								"Java only supports arrays up to "
										+ Integer.MAX_VALUE + " in size");
					}
					bytes[i] = new byte[size];
					copyBytes(input, bytes[i], cursor);
					if (bytesBefore(input, CRLF, cursor) != null) {
						throw new Exception("Argument doesn't end in CRLF");
					}
					cursor.increase(2);
					arguments++;
				} else {
					throw new IOException("Unexpected character");
				}
			}
			try {
				return new Command(bytes);
			} finally {
				bytes = null;
				arguments = 0;
			}
		}

		// the first step, figure out the number of arguments, and init the
		// bytes;
		if (input[cursor.getValue()] == '*') {

			int numArgs = readInt(input, cursor.increase(1));
			if (numArgs > Integer.MAX_VALUE) {
				throw new IllegalArgumentException(
						"Java only supports arrays up to " + Integer.MAX_VALUE
								+ " in size");
			}
			if (numArgs < 0) {
				throw new Exception("Invalid size: " + numArgs);
			}
			bytes = new byte[numArgs][];
			return decode(input, cursor);
		} else {

			// Read command -- can't be interupted
			byte[][] b = new byte[1][];
			b[0] = bytesBefore(input, CRLF, cursor);
			return new Command(b, true);
		}

	}

	public int readInt(byte[] input, Cursor start) {
		int value = 0;
		int read;
		for (int i = start.getValue(); i < input.length - 1; i++) {
			if (input[i] == CR && input[i + 1] == LF) {
				start.increase(2);
				break;
			}
			read = input[i] - ZERO;
			value = value * 10 + read;
			start.increase(1);
		}

		return value;

	}

	public static byte[] bytesBefore(byte[] input, byte[] target, Cursor cursor) {
		int length = 0;
		int start = cursor.getValue();
		for (int i = cursor.getValue(); i < input.length - 1; i++) {

			if (input[i] == target[0]) {
				boolean b = true;
				for (int j = 0; j < target.length - 1; j++) {
					if (input[i + 1 + j] != target[j + 1]) {
						b = false;
						break;
					}
				}
				if (b == true) {
					break;
				}
			}
			cursor.increase(1);
			length++;
		}
		if (length == 0) {
			return null;
		} else {
			byte[] result = new byte[length];
			System.arraycopy(input, start, result, 0, length);
			return result;
		}

	}

	public void copyBytes(byte[] input, byte[] target, Cursor cursor) {
		System.arraycopy(input, cursor.getValue(), target, 0, target.length);
		cursor.increase(target.length);
	}

	static class Cursor {
		int value = 0;

		public Cursor(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public Cursor increase(int incre) {
			this.value += incre;
			return this;
		}

	}

	public static void main(String... args) {
		byte[] input = { 0, 1, 2, 3, 4, 5 };
		byte[] target = { 4, 5 };
		byte[] result = bytesBefore(input, target, new Cursor(0));
		System.out.println(result[0]);
	}
}
