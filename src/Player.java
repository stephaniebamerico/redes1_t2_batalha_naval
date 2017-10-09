import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Player {
	private int id;
	private Board[] boards = null;
	private Point[][] ships = null;
	private TokenRing network = null;
	
	public Player(int id, Point[][] ships) {
		/* Start Network configuration */
		// Find ip address
		InetAddress my_ip = TokenRing.getLocalIP(), next_ip = null;
		int next_id = 0;
		
		if (id == 0) {
			// Players have different ip address
			String ip = my_ip.toString().split("/")[1];
			
			try {
				// Open file with ip addresses
	            File f = new File("src/ip_addresses.txt");
	            BufferedReader buffer = new BufferedReader(new FileReader(f));
	            String line = "";
	            
	            // Find id based on ip address
	            while (((line = buffer.readLine()) != null) && !(line.split(" ")[1].equals(ip)));
	            this.id = Integer.parseInt(line.split(" ")[0]);
	            
	            // Find the next player's ip address
	            if ((line = buffer.readLine()) != null) {
	            	line = line.split(" ")[1];
	            }
	            else {
	            	buffer.close();
	            	buffer = new BufferedReader(new FileReader(f));
	            	line = buffer.readLine().split(" ")[1];
	            }
	            next_ip = InetAddress.getByName(line);
	         
	            buffer.close();
	        } catch (IOException e) {e.printStackTrace();}
		}
		else {
			// Players have the same ip address
			this.id = id;
			next_ip = my_ip;
		}
		next_id = (this.id+1)%5 + (this.id+1)/5;
		network = new TokenRing(this.id, next_ip, 10100+id, 10100+next_id);
		System.out.println("Criando player "+this.id);
		// Starts listening on the network
		network.listener();
		/* End Network configuration */
		
		/* Start Board configuration */
		// Set ships position
		this.ships = ships;
		
		// Initializes the player's board
		boards = new Board[5];
		for (int i = 0; i < 5; i++) {
			boards[i] = new Board();
		}
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				boards[0].cells[i][j] = Cell.WATER;
			}
		}
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				boards[0].cells[ships[i][j].x][ships[i][j].y] = Cell.SHIP;
			}
		}
		/* End Board configuration */
	}
	
	public void attack(int id_target, Point coordinates) {
		String msg = new String(id+";"+id_target+";1;"+coordinates.x+";"+coordinates.y+";");
		System.out.println(msg);
		network.speaker(msg);
	}
	
	public int getId() {
		return this.id;
	}
}
