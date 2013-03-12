package cbox.realrec.protocol.command;

import java.util.Arrays;

public class Command {

	String[] cmd;

	public Command(String inline) {
		cmd = inline.split("\\s+");
	}

	public Command(String[] unified) {
		cmd = unified;
	}

	public String[] cmd() {
		return cmd;
	}

	@Override
	public String toString() {
		return "Command [cmd=" + Arrays.toString(cmd) + "]";
	}

}
