package DirectoryManager;

public class HomeDir {
	private String currentDir;
	private final String homeDir;

	public HomeDir(String username, String chemin){
		
		homeDir = chemin + "/" + username;
		currentDir = homeDir;
	}
	
	public void cdCommand(String chemin){
		currentDir += "/" + chemin;
	}
	
	public void cdUpCommand(){
		currentDir = "";
	}
	
	public void reset(){
		currentDir = homeDir;
	}
	
	public String getCurrentDir(){
		return currentDir;
	}
}
