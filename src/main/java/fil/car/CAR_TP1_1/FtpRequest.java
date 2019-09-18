package fil.car.CAR_TP1_1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

import bdCheck.Session;
import bdCheck.User;

public class FtpRequest extends Thread {
	private Socket client;
	private Socket data;

	private int port;
	private String adress;
	private InetAddress ip;

	private InputStreamReader inr;
	private DataOutputStream dou;
	private BufferedReader b;
	private User utilisateur;
	private String username;
	private boolean passive;
	private String racine;
	private String homeDir;
	private DataInputStream dataR;
	private DataOutputStream dataW;

	private ServerSocket serverpsv;

	public FtpRequest(Socket client, int port) {
		this.client = client;
		this.homeDir = System.getProperty("user.home");
		this.racine = homeDir;
		adress = client.getInetAddress().getHostAddress();
		this.port = port;
		ip = client.getInetAddress();
	}

	@Override
	public void run() {
		try {
			inr = new InputStreamReader(client.getInputStream());
			dou = new DataOutputStream(client.getOutputStream());

			b = new BufferedReader(inr);
			processRequest();
			// BuffereWriter wb = new BufferedWriter(dou);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String processRequest() throws IOException {
		reply(220, "Le serveur est prêt");
		String cmd = b.readLine();
		String res = "";

		while (cmd != "QUIT") {
			// cmd = b.readLine();
			// if (cmd != null) {
			System.out.println(cmd);
			if (cmd == null) return "";
			String[] ligne = cmd.split("\\s+");
			switch (ligne[0]) {
			case "USER":
				processUSER(ligne[1].replaceAll("\\s", ""));
				break;
			case "PASS":
				processPASS(ligne[1]);
				break;
			case "LIST":
				processLIST();
				break;
			case "RETR":
				processRETR(ligne[1]);
				break;
			case "STOR":
				processSTOR(ligne[1]);
				break;
			case "QUIT":
				processQUIT();
				break;
			case "PWD":
				processPWD();
				break;
			case "CWD":
				processCWD(ligne[1]);
				break;

			case "CDUP":
				processCDUP();
				break;
			case "PASV":
				processPASV();
				 break;
			case "MKD":
				processMKD(ligne[1]);
				break;
			case "RMD":
				processRMD(ligne[1]);
				break;
			case "PORT":
				processPORT(ligne[1]);
				break;
			case "EPSV":
				processEPSV();
				break;
			case "EPRT":
				processEPRT(ligne[1]);
				break;
			/*case "TYPE":
				processTYPE(ligne[1]);
				break;*/
			case "SYST":
				processSYST();
				break;
			case "FEAT":
				reply(200, "FEAT ok");
			default:
				reply(502, "commande non implémentée");
				// break;
			}
			cmd = b.readLine();
			// }
			// cmd = "QUIT";
		}
		return res;
	}
	
	public void processSYST(){
		reply(200, "SYST OK");
	}

	public void processTYPE(String type) {
		
		reply(200, "donnée en " + type);
		
	}

	public String processUSER(String username) {
		String s = "";
		if (Session.containsU(username) == true) {
			reply(331, "Bonjour " + username + ", veuillez saisir votre mot de passe");
			this.username = username;
		} else {
			reply(530, "nom d'utilisateur incorrect");
		}
		return s;
	}

	public String processPASS(String password) {
		utilisateur = Session.connexion(username, password);
		if (utilisateur != null)
			reply(230, "Session Ouverte");
		else
			reply(530, "mot de passe incorrect");
		return "";
	}

	public String processRETR(String filename) {
		if (utilisateur.isRead()) {

			// on utilise 150 car c'est nous qui ouvrons le canal de donnée
			reply(150, "transfert en cours de chargement, ouverture du canal de données");

			try {
				if (passive)
					data = serverpsv.accept();
				else
					data = (new Socket(InetAddress.getByName(adress), port));
				FileInputStream fichier = new FileInputStream((homeDir.charAt(homeDir.length() - 1) == '/') ? homeDir + filename : homeDir + "/" + filename);
				dataW = new DataOutputStream(data.getOutputStream());
				int i = fichier.read();
				while (i != -1) {
					dataW.writeByte(i);
					i = fichier.read();
				}
				fichier.close();

				data.close();
				reply(226, "Fichier recuperé, fermeture du canal de données");

			} catch (Exception e) {
				// TODO Auto-generated catch block

				reply(425, "Fichier non envoyé");
				e.printStackTrace();
			}
		} else {

			reply(550, "Acces non autorisé");
		}

		return "";

	}

	public String processSTOR(String filename) {
		if (utilisateur.isWrite()) {

			// on utilise 150 car c'est nous qui ouvrons le canal de donnée
			reply(150, "transfert en cours de chargement, ouverture du canal de donnée");

			try {
				if (passive)
					data = serverpsv.accept();
				else
					data = (new Socket(InetAddress.getByName(adress), port));
				FileOutputStream fichier = new FileOutputStream((homeDir.charAt(homeDir.length() - 1) == '/') ? homeDir + filename : homeDir + "/" + filename);
				dataR = new DataInputStream(data.getInputStream());
				int i = dataR.read();
				while (i != -1) {
					fichier.write(i);
					i = dataR.read();
				}
				fichier.close();

				data.close();
				reply(226, "Fichier recuperé, fermeture du canal de données");

			} catch (IOException e) {
				// TODO Auto-generated catch block

				reply(425, "Fichier non envoyé");
				e.printStackTrace();
			}
		} else {

			reply(550, "Acces non autorisé");
		}
		return "";
	}

	public String processCDUP() {

		String cdup = "";
		if (utilisateur.isRead()) {
			Path path = Paths.get(homeDir);

			for (int i = 0; i < path.getNameCount() - 1; i++) {
				cdup = cdup + "/" + path.getName(i);
			}
			homeDir = cdup;
			reply(250, cdup);
		} else {
			reply(550, "Non autorisé");
		}
		return "";
	}

	public String processCWD(String pathdest) {

		String[] dest = pathdest.split("\\/");

		if (utilisateur.isRead()) {
			// le cas de l'adresse absolue
			if (pathdest.startsWith("/"))
				homeDir = pathdest;

			else {
				if (homeDir.charAt(homeDir.length() - 1) == ('/'))
					homeDir += pathdest;
				else
					homeDir += "/" + pathdest;

			}
			// on vérifie ensuite l'existence du dossier et que l'acces est
			// autorisé

			if (homeDir.startsWith(racine)) {
				if ((new File(homeDir)).exists())
					reply(250, "Directory successfully changed");
				else {
					// retour a la racine
					homeDir = racine;
					reply(550, "Failed to change directory");
				}
			} else {
				homeDir = racine;
				reply(550, "Accès non autorisé");
			}
		} else {

			// traiter le cas de non autorisation d'accès
			reply(550, "No read access");
		}
		return "";
	}

	public String processPWD() {
		reply(257, homeDir);
		return "";
	}

	public String processPASV() throws IOException {
		int p1, p2;
		System.out.println("on essaye pasv");
		serverpsv = new ServerSocket(0);
		port = serverpsv.getLocalPort();

		p1 = port / 256;
		p2 = port % 256;

		String ipClient[] = client.getLocalAddress().getHostAddress().split("\\.");

		reply(227, "Passage au mode passif : " + ipClient[0] + "," + ipClient[1] + "," + ipClient[2] + "," + ipClient[3]
				+ "," + p1 + "," + p2);

		passive = true;
		//data = serverpsv.accept();
		return "";
	}
	public String processEPSV() throws IOException {
		int p1, p2;
		System.out.println("on essaye pasv");
		serverpsv = new ServerSocket(0);
		port = serverpsv.getLocalPort();

		p1 = port / 256;
		p2 = port % 256;

		String ipClient[] = client.getLocalAddress().getHostAddress().split("\\.");

		reply(229, "Passage au mode passif étendu : " + ipClient[0] + "," + ipClient[1] + "," + ipClient[2] + "," + ipClient[3]
				+ "," + p1 + "," + p2);

		passive = true;
		//data = serverpsv.accept();
		return "";
	}

	public void processPORT(String newport) throws UnknownHostException, IOException {
		System.out.print("newport : " + newport);
		String p[] = newport.split(",");

		if (p.length == 6) {
			System.out.println(p.length);
			
			//adress = (p[0] + "." + p[1] + "." + p[2] + "." + p[3]);
			System.out.println(adress);
			int nport = Integer.parseInt(p[4]) * 256 + Integer.parseInt(p[5]);
			if (nport < 1024 || nport > 65535)
				reply(500, "generation impossible changez de port");
			else {
				port = nport;
				//data = new Socket(adress, port);
				reply(200, "Canal généré");
				passive = false;
			}
		}else{
			reply(500, "generation impossible");
		}
	}
	
	public void processEPRT(String newport) throws IOException{
		
		String p[] = newport.split("[|]");
		
		int port = Integer.parseInt(p[3]);
		reply(200, "passage effectué");
		passive = false;
		//data = new Socket(adress, port);
	}

	public void processQUIT() {
		reply(221, "Canal de controle fermé");
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void processLIST() {
		if (utilisateur.isRead()) {

			// on stock la liste dans une liste de fichier

			File listfic[] = (new File(homeDir).listFiles());
			System.out.println(listfic.length);
			String fichier = "";

			reply(150, "Liste des repertoires en cours de chargement");
			System.out.println(adress);
			if (!passive && adress == null)
				reply(500, "ouvrez une canal de communication");
			else {
				try {
					if (passive)
						data = serverpsv.accept();
					else{
						System.out.println(ip.getHostAddress() + ip.getHostName() + port);
						data = (new Socket(InetAddress.getByName(adress), port));
					}
					dataR = new DataInputStream(data.getInputStream());
					dataW = new DataOutputStream(data.getOutputStream());

					// une fois les sockets de transfert de donnees ouvert on
					// peut
					// les envoyer

					for (int i = 0; i < listfic.length; i++) {
						if (listfic[i].isFile()) {
							// pour que fichier soit considéré comme Bytes ASCII
							// on
							// ajoute \015\012
							fichier = listfic[i].getName();
						}
						// on gèrera differemment l'affichage des repertoires
						if (listfic[i].isDirectory()) {
							fichier =  listfic[i].getName();
						}
						dataW.writeBytes(fichier);
						dataW.flush();
					}
					
					//data.close();
					reply(226, "Liste envoyé");

				} catch (IOException e) {
					// TODO Auto-generated catch block

					reply(425, "La liste n'a pas été envoyée");
					e.printStackTrace();
				}
			}
		} else {

			reply(550, "Acces non autorisé");
		}
	}

	public void processMKD(String directory) {
		if (utilisateur.isWrite()) {

			File dir = new File(
					(homeDir.charAt(homeDir.length() - 1) == '/') ? homeDir + directory : homeDir + "/" + directory);
			if (dir.mkdir()) {
				reply(250, "Repertoire ajouté");
			} else {
				reply(550, "Repertoire non crée");
			}
		} else {
			reply(550, "Acces non autorisé");
		}
	}

	public void processRMD(String directory) {
		if (utilisateur.isWrite()) {

			// on recupere le chemin vers le repertoire

			// on traite le cas ou tout le chemin est donné ou si c'est juste le
			// nom du dossier
			File repository;
			repository = new File(homeDir);
			String dir = (directory.startsWith(homeDir)) ? homeDir : homeDir + "/" + directory;

			repository = new File(dir);
			System.out.println(dir);
			if (repository.exists() && repository.isDirectory()) {

				if (repository.delete()) {
					reply(250, "Repertoire supprimé");

				} else {
					reply(550, "Suppression non effectuée");
				}
			} else {
				reply(550, "Ce n'est pas un répertoire, Suppression non effectuée");
			}
		} else {
			reply(550, "Acces non autorisé");
		}
	}

	int reply(int code, String message) {
		try {
			dou.writeBytes(code + " " + message + "\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return code;
	}

	public Socket getData() {
		return data;
	}

	public void setData(Socket data) {
		this.data = data;
	}

	public boolean isPassive() {
		return passive;
	}

	public void setPassive(boolean passive) {
		this.passive = passive;
	}

	public String getRacine() {
		return racine;
	}

	public void setRacine(String racine) {
		this.racine = racine;
	}

	public String getHomeDir() {
		return homeDir;
	}

	public void setHomeDir(String homeDir) {
		this.homeDir = homeDir;
	}

	public DataInputStream getDataR() {
		return dataR;
	}

	public void setDataR(DataInputStream dataR) {
		this.dataR = dataR;
	}

	public DataOutputStream getDataW() {
		return dataW;
	}

	public void setDataW(DataOutputStream dataW) {
		this.dataW = dataW;
	}

}
