package bdCheck;

import java.util.ArrayList;
import java.util.List;

public class Session {

	public static List<User> users = new ArrayList<>();

	public static boolean containsU(String username) {
		System.out.println("wesh?!!");
		for (User u : users) {
			if (u.getUsername().contains(username) || username.contains(u.getUsername())){
				System.out.println(u.getUsername() + " = "+  username);
				return true;
			}
		}
		System.out.println("wesh?!!");
		return false;
	}

	public static User connexion(String username, String password) {
		for (User u : users) {
			if (username.contains(u.getUsername()))
				if (u.checkPass(password))
					return u;
				return null;
		}
		return null;

	}

}
