package cbox.realrec.protocol.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufIndexFinder;

import java.nio.charset.Charset;

public final class ByteBufUtils {

	public static final byte[] CRLF = new byte[] { '\r', '\n' };
	public static final byte[] NEGCRLF = new byte[] { '-', '1', '\r', '\n' };
	public static final Charset UTF8 = Charset.forName("UTF-8");;

	private ByteBufUtils() {
	}

	public static String readLine(ByteBuf in) {
		ByteBuf buf = in.readBytes(in.bytesBefore(ByteBufIndexFinder.CRLF));
		in.skipBytes(2);
		return buf.toString(UTF8);
	}

	public static String readLine(ByteBuf in, int length) {
		ByteBuf buf = in.readBytes(length);
		if (in.bytesBefore(ByteBufIndexFinder.CRLF) != 0)
			throw new IllegalArgumentException("unexpected character");
		in.skipBytes(2);
		return buf.toString(UTF8);
	}

	public static long readLong(ByteBuf in) {
		return Long.parseLong(readLine(in));
	}

	public static int readInt(ByteBuf in) {
		return Integer.parseInt(readLine(in));
	}

	public static String readBulk(ByteBuf in) {
		int length = readInt(in);
		if (length < 0)
			return null;
		return readLine(in, length);
	}

}
