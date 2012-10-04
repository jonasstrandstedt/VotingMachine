
import java.util.*;

public class CommandParser {

	private String command;
	private Vector<String> arguments = new Vector<String>();
	private int argument = 0;

	public CommandParser(String cmd_string) {
		StringTokenizer tokenizer = new StringTokenizer(cmd_string, " ");
		this.command = tokenizer.nextToken();

		Boolean informationrich = false;
		String theToken = "";
		while(tokenizer.hasMoreTokens()) {
			String current = tokenizer.nextToken();
			if ( ! informationrich && current.startsWith("\"")) {
				informationrich = true;
				current = current.substring(1);
				theToken = current;
			} else if ( informationrich) {
				if(current.endsWith("\"")) {
					informationrich = false;
					current = current.substring(0, current.length() - 1);
				}
				theToken += " " + current;
			} else {
				theToken = current;
			}

			if( ! informationrich || ! tokenizer.hasMoreTokens()) {
				arguments.add(theToken);
				theToken = "";
			}
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
			String current = this.arguments.get(argument);
			argument++;
			return current;
		}
		return "";
	}

	public Vector<String> get_arguments() {
		return this.arguments;
	}

	public String get_command() {
		return command;
	}

	public String toString() {
		return command;
	}
} 