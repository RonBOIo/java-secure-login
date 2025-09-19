package loginSystem;

//source: https://medium.com/@gaganjain9319/aes-encryption-and-decryption-using-java-700eaa0fd273
// This class is used to handle the AES encryption and decryption of user data 
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class AesEncryption {
	//here we define the algorithm need, in our case AES
	private static final String ALGORITHM = "AES";
	
	//this defines the transformation algorithm/mode/padding
	private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

	public static byte[] encrypt(String plainText, String key) throws Exception {
		//converts the key into a string variable
		Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
		//a cipher instance is created using the defined transformation
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		//used to cipher using encryptmode and key
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		//returns the ciphered text
		return cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
	}

	public static String decrypt(byte[] cipherText, String key) throws Exception {
		//converts the key into a string variable
		Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
		//a cipher instance is created using the defined transformation
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		//used to cipher using decryptmode and key
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		//return the plain text
		byte[] decryptedBytes = cipher.doFinal(cipherText);
		return new String(decryptedBytes, StandardCharsets.UTF_8);
	}
}
