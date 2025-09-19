package loginSystem;

/*Used to provaide admins RBAC 
 * When user has admin privalage they can access the following
 * addUser(), removeUser(), updatePassword(), unlockAccount(), viewLogs()
 * adminActions records all admin performed actions 
 * 
 */
import java.util.*;

public class Admin {

	private List<User> users; // loads user list 

	public Admin(List<User> users) {
		this.users = users;
	}

	// adds user new with hashed password and salt
	// stores to userData.txt and logs admin action
	public void addUser(String username, String password, String role) {

		try {
			String salt = SecurityManager.generateSalt();
			String hashedPassword = SecurityManager.hashPassword(password, salt);

			User newUser = new User(username, hashedPassword, salt, role);
			users.add(newUser);

			SecurityManager.saveUser(users);
			System.out.println("New User added");
			Logger.logEvent("New user added " + username);

		} catch (Exception e) {
			System.out.println("Error please contact admin");
			Logger.logEvent("Error adding user" + username);

		}
	}

	// deletes the selected user from userData.txt
	// then logs admin action
	public void removeUser(String username) {
		Iterator<User> iterator = users.iterator();
		boolean found = false;

		while (iterator.hasNext()) {
			User user = iterator.next();
			if (user.getUsername().equals(username)) {
				iterator.remove();
				found = true;
				break;
			}
		}

		if (found) {
			try {
				SecurityManager.saveUser(users);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Logger.logEvent("User has been removed " + username);
		} else {
			System.out.println("User Not Found");
		}

	}

	// enables admin to update user password in case account is locked or password
	// has been forgotten.
	// then logs admin action
	// need to hash the password and add new salt again.
	public void updatePassword(String username, String newPassword) {
		boolean updated = false;

		for (User user : users) {
			if (user.getUsername().equals(username)) {
				String newSalt = SecurityManager.generateSalt();
				String newHashedPassword = SecurityManager.hashPassword(newPassword, newSalt);
				user.setPassword(newHashedPassword);
				user.salt = newSalt;
				try {
					SecurityManager.saveUser(users);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Logger.logEvent("Password has been updated for " + username);
				updated = true;
				break;
			}
		}
		if (!updated) {
			System.out.println("No User Found");
		}
	}

	// resets the isAccountLocked status
	// also resets the attempts
	public void unlockAccount(String username) {
		boolean unlocked = false;//

		for (User user : users) {
			if (user.getUsername().equals(username)) {
				user.unlockAccount();
				try {
					SecurityManager.saveUser(users);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Logger.logEvent("Account unlocked for " + username);
				unlocked = true;
				break;

			}
		}
		if (!unlocked) {
			System.out.println("No user Found");
		}
	}

	// allows admin to view logs
	public void viewLogs() {
		System.out.print("--System Events--");
		Logger.getLogs();
	}
}
