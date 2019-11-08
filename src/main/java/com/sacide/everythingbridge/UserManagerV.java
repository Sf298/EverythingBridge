/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sacide.everythingbridge;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

/**
 * Performs all the passwords checking and token handling for users.
 * @author saud
 */
public class UserManagerV {
    
	public final HashMap<Integer, String> tokens = new HashMap<>();
	private final Random r = new Random();
	private final int HASH_LEN = Encryptor.hashSHA256("a", "a").length();
	//private final String encryptionKey = Encryptor.genKey("npauvfnpfjlksmnvnpfd");
	private Thread fileLoaderThread;
	
	private final Properties users;
	public final String hashSalt;
	private String fileEncryptorKey;
	
	/**
	 * Creates a new user manager instance.
	 * @param scanUserFile Whether to scan for changes in the users file every 10 sec.
	 * @param hashSalt The salt to use when hashing the passwords.
	 * @param fileEncryptorKey The key to use when encrypting the save file. If
	 * null, the file is not encrypted.
	 */
	public UserManagerV(boolean scanUserFile, String hashSalt, String fileEncryptorKey) {
		this(new File("./users.prop"), scanUserFile, hashSalt, fileEncryptorKey);
	}
	
	/**
	 * Creates a new user manager instance.
	 * @param usersFile The file path to store the usernames and password hashes.
	 * @param scanUserFile Whether to scan for changes in the users file every 10 sec.
	 * @param hashSalt The salt to use when hashing the passwords.
	 * @param fileEncryptorKey The key to use when encrypting the save file. If
	 * null, the file is not encrypted.
	 */
	public UserManagerV(File usersFile, boolean scanUserFile, String hashSalt, String fileEncryptorKey) {
		this.users = new Properties(usersFile, " = ");
		this.hashSalt = hashSalt;
		this.fileEncryptorKey = fileEncryptorKey;
		if(!users.fileExists()) {
			System.out.println("File 'users.prop' not found. Creating...");
			users.save();
		}
		if(scanUserFile) {
			fileLoaderThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					loadUserData();
					try {
						Thread.sleep(10*1000);
					} catch (InterruptedException ex) {}
				}
			}
		});
		fileLoaderThread.start();
		}
		/*
		Zf6j0V2HKgkk9tLarewYG

		*/
	}
	
	/**
	 * Loads data from the users file.
	 */
	public void loadUserData() {
		users.load(fileEncryptorKey);
		
		HashSet<String> toHash = new HashSet<>(); // hash unhashed passwords
		for(Map.Entry<String, String> entry : users.getMap().entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if(value.length() != HASH_LEN || value.matches("[^0-9a-f]")) { // check is hash
				toHash.add(key);
			}
		}
		for(String key : toHash) {
			users.put(key, Encryptor.hashSHA256(users.get(key),"",hashSalt));
		}
		if(!toHash.isEmpty()) {
			users.save(fileEncryptorKey);
		}
	}
	
	/**
	 * Checks if the provided password matches the password stored.
	 * @param uname The unique username for the user.
	 * @param unsaltedHash The password, unsalted and pre-hashed with SHA-256.
	 * @return Whether or not the password matches the stored password.
	 */
	public boolean checkPasswordHash(String uname, String unsaltedHash) {
		uname = uname.toLowerCase();
		return users.hasKey(uname) && users.get(uname).equals(Encryptor.hashSHA256(unsaltedHash,hashSalt));
	}
	
	/**
	 * Add a new user to the manager.
	 * @param uname The username.
	 * @param unsaltedHash The password, unsalted and pre-hashed with SHA-256.
	 */
	public void addUser(String uname, String unsaltedHash) {
		users.put(uname, Encryptor.hashSHA256(unsaltedHash,hashSalt));
		resetBatchSaveThread();
	}
	
	/**
	 * Remove a user from the manager.
	 * @param uname The username.
	 */
	public void removeUser(String uname) {
		users.remove(uname);
		resetBatchSaveThread();
	}
	private Thread batchSaveThread;
	private void resetBatchSaveThread() {
		if(batchSaveThread != null) batchSaveThread.interrupt();
		batchSaveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					return;
				}
				users.save(fileEncryptorKey);
			}
		});
		batchSaveThread.start();
	}
	
	
	/**
	 * Get a new token for the given username.
	 * @param uname A username that exists in the manager.
	 * @return returns the new token, or -1 if the user does not exist.
	 */
	public int newToken(String uname) {
		if(!users.hasKey(uname)) return -1;
		
		int t;
		do {
			t = r.nextInt(Integer.MAX_VALUE);
		} while(tokens.keySet().contains(t));

		tokens.put(t, uname);
		return t;
	}
	
	/**
	 * Checks if a token has been issued.
	 * @param token
	 * @return 
	 */
	public boolean checkToken(int token) {
		if(token == -1) return false;
		return tokens.containsKey(token);
	}
	
	/**
	 * Removes all stored tokens.
	 */
	public void clearTokens() {
		tokens.clear();
	}
	
	/**
	 * Removes the selected token.
	 * @param token The token to remove.
	 */
	public void logout(int token) {
		tokens.remove(token);
	}
	
	/**
	 * Logs out all tokens issued to a given user.
	 * @param token Any token issued to the user.
	 */
	public void logoutUser(int token) {
		String uname = tokens.get(token);
		logoutUser(uname);
    }
	
	/**
	 * Logs out all tokens issued to a given user.
	 * @param uname The username.
	 */
	public void logoutUser(String uname) {
		HashSet<Integer> toRemove = new HashSet<>();
		for(Map.Entry<Integer, String> entry : tokens.entrySet()) {
			Integer key = entry.getKey();
			String value = entry.getValue();
			if(uname.equals(value))
			toRemove.add(key);
		}
		for(Integer integer : toRemove) {
			tokens.remove(integer);
		}
    }
    
}
