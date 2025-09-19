package loginSystem;

/* The user class will store the login credentials, user role, accountlocked status, attemptscount.
 */
public class User {
	private String username; // used in identifing each user
	String hashedPassword; // hashed(salt+password)
	String role; // RBAC admin/user
	String salt; // randonly generated
	boolean accountLocked; // identifies if account is locked
	int attemptCount; // tracks failed login attempts after system quit

	// constructor for user class
	public User(String username, String hashedPassword, String salt, String role) {

		this.username = username;
		this.hashedPassword = hashedPassword;
		this.salt = salt;
		this.role = role;
		this.accountLocked = false;
		this.attemptCount = 0;// default values are set for both the account locked and the attempts of login

	}

	// used by admin to set and reset password
	public void setPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	// checked entered password against stored password
	public boolean validatePassword(String enteredPassword) {
		return this.hashedPassword.equals(enteredPassword); // Securicty manager uses to authenticate users
	}

	public boolean isAccountLocked() {// checks account locked value(Bool)
		return accountLocked;
	}

	public void lockAccount() {// will set the account locked value to true
		this.accountLocked = true;
	}

	public void unlockAccount() {// needed by admin to unlock account
		this.accountLocked = false;
		resetAttempts();
	}

	public int getAttemptCount() {// needed to get login attpemts
		return attemptCount;
	}

	public void addAttemptCount() {// needed to incriment the login attemps before calling lock account
		this.attemptCount++;
		if (attemptCount >= 3) {
			lockAccount();
		}

	}

	public void resetAttempts() {// resets the attemptCount to 0
		this.attemptCount = 0;
	}

	// RBAC getters
	public String getUsername() {
		return username;

	}

	public String getHashedPassword() {
		return hashedPassword;
	}

	public String getRole() {// used for logs and user identification
		return role;
	}

	public String getSalt() {
		return salt;

	}
}