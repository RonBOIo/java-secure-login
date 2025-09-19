package loginSystem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;

public class SecurityManager {

	// constant user_file for UserData.txt
	private static final String USER_FILE = "userData.txt";
	//our key for AES encryption/decryption *must be 16 bytes*
	private static final String key = "1234567891234567";

	// used to handle password hashing and the authentication. It will also handle
	// the brute force protection, by tracking the failed login attempts and enforce
	// account locking.
	public static String hashPassword(String password, String salt) {

		try {
			MessageDigest PasswordHash = MessageDigest.getInstance("SHA-256");

			String saltedPassword = password + salt;
			PasswordHash.update(saltedPassword.getBytes());
			byte[] hashedBytes = PasswordHash.digest();

			StringBuilder StringBuilt = new StringBuilder();
			for (byte b : hashedBytes) {
				StringBuilt.append(String.format("%02x", b));
			}
			return StringBuilt.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error please contact admin!");
			return null;
		}
	}

	/*
	 * First check account lock status hash the entered password with its stored
	 * salt compare hashed(enetredPassword) to stored hashed password track login
	 * attemps and lock account if more than 3 (brute force protection)
	 */
	public boolean authenticateUser(User user, String enteredPassword) {
		if (user.isAccountLocked()) {
			Logger.logEvent("ACCOUNT LOCKED," + user.getUsername());
			System.out.println("Error please contact Admin! ");
			return false;
		}

		String enteredHashedPassword = hashPassword(enteredPassword, user.getSalt());
		if (enteredHashedPassword != null && user.validatePassword(enteredHashedPassword)) {
			user.resetAttempts();// used to reset attempts after a successful login
			Logger.logEvent("Login Successfull," + user.getUsername());
			return true;
		} else {
			trackLoginAttempts(user);
			Logger.logEvent("Failed Attempt, " + user.getAttemptCount() + "," + user.getUsername());
			return false;
		}
	}

	// Used to lock accounts
	public void lockAccount(User user) {
		user.lockAccount();
		Logger.logEvent("Account Now Locked, " + user.getUsername());
		System.out.println("Invalid Credentials");

	}

	public void trackLoginAttempts(User user) { // limits users login Attempts to 3
		user.addAttemptCount();
	}

	// generate salt, later appended to password
	public static String generateSalt() {
		SecureRandom random = new SecureRandom();
		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);
		return Base64.getEncoder().encodeToString(saltBytes);
	}

	// saves users to userData.txt
	// each user has a username, hashedPassword. role, isLocked(accountstatus),
	// salt, attempts
	public static void saveUser(List<User> users) throws Exception {
		try {
			StringBuilder plaintext = new StringBuilder();

			for (User user : users) {
				plaintext.append(user.getUsername()).append(",").append(user.getHashedPassword()).append(",")
						.append(user.getRole()).append(",").append(user.isAccountLocked()).append(",")
						.append(user.getAttemptCount()).append(",").append(user.getSalt()).append("\n");
			}
			byte[] encryptedBytes = AesEncryption.encrypt(plaintext.toString(), key);

			try (FileOutputStream encrytion = new FileOutputStream(USER_FILE)) {
				encrytion.write(encryptedBytes);
			}

			//System.out.println("encrypted..");

		} catch (IOException e) {
			System.out.println("Error contact Admin");
			Logger.logEvent("Error encrypting user data" + e.getMessage());
		}
	}

	// Loads the users from userData.txt
	// applies account lock status and failed attempt count
	public static List<User> loadUser() {
		List<User> users = new ArrayList<>();

		try {
			byte[] encrytedData = Files.readAllBytes(Paths.get(USER_FILE));

			String decryption = null;
			try {
				decryption = AesEncryption.decrypt(encrytedData, key);
			} catch (Exception e) {
				System.out.print("Error Please contact an Admin");
				Logger.logEvent("Error Decrypting User information" + e.getMessage());
			}

			for (String line : decryption.split("\n")) {
				String[] userDetails = line.split(",");

				if (userDetails.length == 6) {
					String username = userDetails[0];
					String hashedPassword = userDetails[1];
					String role = userDetails[2];
					boolean isLocked = Boolean.parseBoolean(userDetails[3]);
					int attempts = Integer.parseInt(userDetails[4]);
					String salt = userDetails[5];

					User user = new User(username, hashedPassword, salt, role);

					if (isLocked) {
						user.lockAccount();
					}

					for (int i = 0; i < attempts; i++) {
						user.addAttemptCount();
					}

					users.add(user);

				}
			}
		} catch (IOException e) {
			System.out.println("Error, Please Contact Admin!");
			Logger.logEvent("Issue reading user datails" + e.getMessage());
		}
		return users;
	}
}
