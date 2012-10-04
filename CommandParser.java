
import java.util.*;

public class CommandParser {

	private String command;
	private Vector<String> arguments = new Vector<String>();
	private int argument = 0;

	public CommandParser(String cmd_string) {
		StringTokenizer tokenizer = new StringTokenizer(cmd_string, " ");
		this.command = tokenizer.nextToken();

		while(tokenizer.hasMoreTokens()) {
			arguments.add(tokenizer.nextToken());
		}
	}

	public int count_arguments() {
		return this.arguments.size();
	}

	public Boolean isEqual(String ... cmds) {
		for (int i = 0; i < cmds.length; i++) {
			if (command.equalsIgnoreCase(cmds [i])) {
				return true;
			}
		}
		return false;
	}

	public String next_argument() {
		if (this.argument < this.arguments.size()) {
			return this.arguments.get(argument);
		}
		return "";
	}

	public Vector<String> get_arguments() {
		return this.arguments;
	}

	public String get_command() {
		return command;
	}
} 