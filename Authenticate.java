
import java.util.*;

class User {
	private String name;
	private String password;
	private int token;
	
	public User(String n, String p, int t) {
		this.name = n;
		this.password = this.hash(p);
		this.token = t;
	}
	
	public Boolean isEqual(String n, String p) {
		return this.name.equals(n) && this.password.equals(p);
	}
	
	public int get_token() {
		return this.token;
	}
	
	private String hash(String in) {
		return in;
	}
	
}

public class Authenticate {
	
	private Vector<User> users;
	
	public Authenticate() {
		users = new Vector<User>();
		users.add(new User("jonas", "password", 1234));
		users.add(new User("kristina", "password", 4321));
		users.add(new User("v1", "password", 8765));
		users.add(new User("v2", "password", 9876));
		users.add(new User("v3", "password", 7654));
		users.add(new User("v4", "password", 2345));
		users.add(new User("v5", "password", 3456));
		users.add(new User("v6", "password", 4567));
	}
	// returns a token (4 digits) if correct input, -1 if user not authenticated
	public int get_token(String n, String p) {
		for(int i = 0; i < users.size(); i++) {
			if(users.elementAt(i).isEqual(n,p)) {
				return users.elementAt(i).get_token();
			}
		}
		return -1;
	}
	
	public Vector<Integer> get_all_tokens() {
		Vector<Integer> tokens = new Vector<Integer>();
		for(int i = 0; i < users.size(); i++) {
			tokens.add(users.elementAt(i).get_token());
		}
		return tokens;
	}
	
} 