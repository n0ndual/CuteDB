package org.tigeress;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Command serialization. We special case when there are few 4 or fewer
 * parameters since most commands fall into that category. Passing bytes,
 * channelbuffers and strings / objects are all allowed. All strings are assumed
 * to be UTF-8.
 */

public class Command {
	public static final byte[] ARGS_PREFIX = "*".getBytes();
	public static final byte[] CRLF = "\r\n".getBytes();
	public static final byte[] BYTES_PREFIX = "$".getBytes();
	public static final byte[] EMPTY_BYTES = new byte[0];

	private final Object name;
	private final Object[] objects;
	private final Object object1;
	private final Object object2;
	private final Object object3;
	private final boolean inline;
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	public Command(Object[] objects) {
		this(null, null, null, null, objects, false);
	}

	public Command(Object[] objects, boolean inline) {
		this(null, null, null, null, objects, inline);
	}

	public Command(Object name) {
		this(name, null, null, null, null, false);
	}

	public Command(Object name, Object[] objects) {
		this(name, null, null, null, objects, false);
	}

	public Command(Object name, Object object1) {
		this(name, object1, null, null, null, false);
	}

	public Command(Object name, Object object1, Object object2) {
		this(name, object1, object2, null, null, false);
	}

	public Command(Object name, Object object1, Object object2, Object object3) {
		this(name, object1, object2, object3, null, false);
	}

	private Command(Object name, Object object1, Object object2,
			Object object3, Object[] objects, boolean inline) {
		this.name = name;
		this.object1 = object1;
		this.object2 = object2;
		this.object3 = object3;
		this.objects = objects;
		this.inline = inline;
	}

	public byte[] getName() {
		// It is either the name or the first objects in the objects array
		if (name != null)
			return getBytes(name);
		return getBytes(objects[0]);
	}

	public boolean isInline() {
		return inline;
	}

	private byte[] getBytes(Object object) {
		byte[] argument;
		if (object == null) {
			argument = EMPTY_BYTES;
		} else if (object instanceof byte[]) {
			argument = (byte[]) object;

		} else if (object instanceof String) {
			argument = ((String) object).getBytes(UTF_8);
		} else {
			argument = object.toString().getBytes(UTF_8);
		}
		return argument;
	}

	public void toArguments(Object[] arguments, Class<?>[] types) {
		int position = 0;
		for (Class<?> type : types) {
			if (type == byte[].class) {
				if (position >= arguments.length) {
					throw new IllegalArgumentException(
							"wrong number of arguments for '"
									+ new String(getName()) + "' command");
				}
				if (objects.length - 1 > position) {
					arguments[position] = objects[1 + position];
				}
			} else {
				int left = objects.length - position - 1;
				byte[][] lastArgument = new byte[left][];
				for (int i = 0; i < left; i++) {
					lastArgument[i] = (byte[]) objects[i + position + 1];
				}
				arguments[position] = lastArgument;
			}
			position++;
		}
	}
}
