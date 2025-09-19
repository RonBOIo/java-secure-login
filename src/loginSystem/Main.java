package loginSystem;

/* handles the main interaction with the system
 * manages the overall flow. 
 * first checks is admin account excists 
 * 
 */
import java.io.*;
import java.util.*;

public class Main {

	public static void main(String[] args) {

		// scanner for user input
		Scanner scanner = new Scanner(System.in);
		// used for managing sec operations like authenticating, load,save user and
		// generating salt
		SecurityManager securityManager = new SecurityManager();
		// load existing users from userData.txt
		List<User> users = SecurityManager.loadUser();

		// if no user exists create an admin, and assume first time app launch
		if (users.isEmpty()) {
			System.out.println("--Secure Login System--");
			System.out.println("Please set up admin account");

			// method used to create an admin
			setupAdminAccount(scanner, users);

			try {
				// save new admin to userData.txt
				SecurityManager.saveUser(users);
				System.out.println("New Admin Created");
				Logger.logEvent("Admin account has been created"); // logs activity to eventLog.txt

			} catch (Exception e) {
				System.out.println("Error Contact Admin");
				Logger.logEvent("Error saving new admin" + e.getMessage());// logs activity to eventLog.txt
			}
		}

		System.out.println("\nEnter Credentials");

		// if user is found , jumps here an requests credentials

		while (true) {

			System.out.print("Username: ");
			String enteredUsername = scanner.nextLine().trim();

			String enteredPassword = maskedPassword(scanner, "Password: ");
			// requests user password and attempts to mask input using maskedPassword method

			User loggedInUser = null;

			// checks if the user exists
			for (User user : users) {
				if (user.getUsername().equalsIgnoreCase(enteredUsername)) {
					loggedInUser = user;
					break;

				}
			}
			// if username is not found
			if (loggedInUser == null) {
				System.out.println("No such user, contact Admin.");
				continue;
			}

			// checks password and turns authenticated true
			boolean authenticated = securityManager.authenticateUser(loggedInUser, enteredPassword);

			// when authenicated is true
			if (authenticated) {
				System.out.println("Welcome " + loggedInUser.getUsername());

				// check role and load approprate menu admin or user
				if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
					Admin admin = new Admin(users);
					handleAdminOptions(scanner, admin);
				} else {
					handleUserOptions(scanner, loggedInUser);
				}

				try { // save and log user interaction
					SecurityManager.saveUser(users);
				} catch (Exception e) {
					System.out.println("Error , Contact Admin.");
					Logger.logEvent("Error saving User data " + e.getMessage());
				}
				break; // break loop where login successful and menu accessed

			} else {

				// handle all failed logins or account locks
				if (loggedInUser.isAccountLocked()) {
					System.out.println("Account has been locked, Contact Admin.");
					try {
						// save and log user interaction
						SecurityManager.saveUser(users);
					} catch (Exception e) {
						System.out.println("Error Contact Admin");
						Logger.logEvent("Error in locking user account" + e.getMessage());
					}
					break;// break loop if account locked
				}
				// notify user save and log outcome
				System.out.println("Password or Username incorrect, please try again.");
				try {
					// save and log user interaction
					SecurityManager.saveUser(users);
				} catch (Exception e) {
					System.out.println("Error Contact Admin");
					Logger.logEvent("Error adding new User" + e.getMessage());
				}

			}

		}

		scanner.close();
	}

// main loop ends here
	// RBAC
	// User menu , not fully implimented in MVP but hosts hotel booking system from
	// coursework one
	private static void handleUserOptions(Scanner scanner, User user) {
		System.out.println("Welcome to the hotel booking system.");
		Logger.logEvent("User Logged In " + user.getUsername());
		System.out.println("To be continued...");
	}

	// RBAC
	// Admin Menu, fully implimented with admin functions for user managment
	private static void handleAdminOptions(Scanner scanner, Admin admin) {

		while (true) {
			System.out.println("--ADMIN MENU--");
			System.out.println("1. Unlock User");
			System.out.println("2. Add User");
			System.out.println("3. Remove User");
			System.out.println("4. Update Password");
			System.out.println("5. View Logs");
			System.out.println("6. Quit");
			System.out.println("Choose from 1-6: ");
			String choice = scanner.nextLine();

			switch (choice) {
			case "1" -> {// unlock user
				System.out.print("Enter username to unlock: ");
				admin.unlockAccount(scanner.nextLine());
			}
			case "2" -> {// add new user
				System.out.print("Enter new username: ");
				String username = scanner.nextLine();
				String password = maskedPassword(scanner, "Enter a New Password: ");
				// mask password enter
				while (!isPasswordValid(password, username)) {
					password = maskedPassword(scanner, "Enter a new password: ");

				}
				String role = "user";// apply role of user to new user
				admin.addUser(username, password, role);

			}
			case "3" -> {// delete user
				System.out.print("Enter username you would like to delete: ");
				String username = scanner.nextLine();
				admin.removeUser(username);

			}
			case "4" -> {// change password
				System.out.print("Enter Username to change password: ");
				String username = scanner.nextLine();

				String newPassword = maskedPassword(scanner, "Enter new Password: ");

				// mask password enter
				while (!isPasswordValid(newPassword, username)) {
					newPassword = maskedPassword(scanner, "Enter new Password:");
				}
				admin.updatePassword(username, newPassword);

			}
			case "5" -> {// view logs
				System.out.println("--Event Log--");
				List<String> logs = Logger.getLogs();
				for (String log : logs) {
					System.out.println(log);
				}

			}
			case "6" -> {// log out
				Logger.logEvent("Admin has logged out");
				return;
			}
			default -> System.out.println("Error, please try again!");
			}
		}

	}

	// hide user input of password(only effective in Command prompt)
	private static String maskedPassword(Scanner scanner, String masked) {
		Console console = System.console();

		if (console != null) {
			// console method used to hide user input
			char[] passwordChars = console.readPassword(masked);
			return new String(passwordChars).trim();
		} else {
			// fallback needed for IDE
			System.out.print(masked);
			return scanner.nextLine().trim();

		}
	}

	// method for creating a new admin account called on first launch
	private static void setupAdminAccount(Scanner scanner, List<User> users) {
		System.out.print("Enter Admin Username :");
		String username = scanner.nextLine();

		String password;

		do {
			password = maskedPassword(scanner, "Password: ");

		} while (!isPasswordValid(password, username));

		String salt = SecurityManager.generateSalt();
		String hashedPassword = SecurityManager.hashPassword(password, salt);

		users.add(new User(username, hashedPassword, salt, "admin"));
	}

	// enforces our password policy
	private static boolean isPasswordValid(String enteredPassword, String Username) {
		try {
			File myObj = new File("commonPasswords.txt");// source
															// (https://www.ncsc.gov.uk/static-assets/documents/PwnedPasswordsTop100k.txt)
			Scanner read = new Scanner(myObj);
			while (read.hasNextLine()) {
				String commonPassword = read.nextLine();
				// checks entered password against commonPassword.txt and if it contains thier
				// username
				if (enteredPassword.equalsIgnoreCase(commonPassword)
						|| enteredPassword.toLowerCase().contains(Username.toLowerCase())) {
					System.out.println("Password too common or may contains your username, please try another!");
					read.close();
					return false;
				}
			}
			read.close();
			// enforces lenght and checks for space

			if (enteredPassword.length() < 8 || enteredPassword.contains(" ")) {
				System.out.println("Password must contain at least 8 characters and not contain a space");
				return false;
			}

			// bool flags for other rules
			boolean hasLower = false;
			boolean hasUpper = false;
			boolean hasNum = false;
			boolean hasCharacter = false;

			// bool flag checks
			for (char Char : enteredPassword.toCharArray()) {
				if (Character.isLowerCase(Char))
					hasLower = true;
				else if (Character.isUpperCase(Char))
					hasUpper = true;
				else if (Character.isDigit(Char))
					hasNum = true;

				else if ("!@#?];".indexOf(Char) >= 0)
					hasCharacter = true;
			}

			// feedback for entered password
			if (!(hasLower && hasUpper && hasNum && hasCharacter)) {
				System.out.println(
						"Password must contain one upper, one lower, a number and a specials character (!@#?];)");
				return false;
			}
			return true;

		} catch (FileNotFoundException e) {
			System.out.println("Error, Contact an admin!");
			return false;
		}
	}
}
