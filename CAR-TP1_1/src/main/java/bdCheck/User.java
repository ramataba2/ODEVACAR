package bdCheck;

import DirectoryManager.HomeDir;

public class User {
	
	private String username;
	private String password;
	private HomeDir dir;
	
	
	private boolean read, write;
	public User (String username, String password, HomeDir dir){
		this.password = password;
		this.username = username;
		this.dir = dir;
		
		read = true;
		write = true;
	}
	
	
	public boolean isRead() {
		return read;
	}


	public void setRead(boolean read) {
		this.read = read;
	}


	public boolean isWrite() {
		return write;
	}


	public void setWrite(boolean write) {
		this.write = write;
	}


	public String getUsername() {
		return username;
	}


	public String getPassword() {
		return password;
	}


	public boolean checkPass(String password){
		return this.password.equals(password);
	}
	

}
