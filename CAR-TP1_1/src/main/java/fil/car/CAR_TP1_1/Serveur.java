package fil.car.CAR_TP1_1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import DirectoryManager.HomeDir;
import bdCheck.Session;
import bdCheck.User;

public class Serveur {

	public static void main(String[] args) {
		Session.users.add(new User("rama", "rama", new HomeDir("rama", "/etu")));
		try {
			ServerSocket s = new ServerSocket(8080);
			System.out.println(s.getInetAddress().getHostAddress());
			try {
				while (true) {
					Socket c = s.accept();
					System.out.println("client : " + c.getInetAddress().getHostAddress());
					FtpRequest fr = new FtpRequest(c, s.getLocalPort());
					fr.start();
				}
			} catch (Exception e) {
				s.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("fin du main");

	}

}
