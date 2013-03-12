package cbox.realrec.protocol.command;

import static cbox.realrec.protocol.command.ByteBufUtils.CRLF;
import static cbox.realrec.protocol.command.ByteBufUtils.writeBulk;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

public class Command {

	public static final byte ARGS_SIZE_MARKER = '*';
	public static final byte ARG_LENGTH_MARKER = '$';
	private String[] tokens;

	public Command(String inline) {
		tokens = inline.split("\\s+");
	}

	public Command(String[] unified) {
		tokens = unified;
	}

	public String[] tokens() {
		return tokens;
	}

	public void writeTo(ByteBuf out) {
		out.writeByte(ARGS_SIZE_MARKER);
		out.writeBytes(String.valueOf(tokens.length).getBytes());
		out.writeBytes(CRLF);
		for (String token : tokens) {
			out.writeByte(ARG_LENGTH_MARKER);
			writeBulk(out, token);
		}
	}

	@Override
	public String toString() {
		return "Command [tokens=" + Arrays.toString(tokens) + "]";
	}

}
