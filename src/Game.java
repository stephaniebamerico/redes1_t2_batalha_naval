import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

// TODO: checar se está atacando jogador válido
// TODO: melhorar mensagens do jogo e acrescentar opção de ver tabuleiro dos jogadores
// TODO: comentarios em ingles
public class Game {
	private static TokenRing network;
	private static Player player;
	private static boolean gameOver, myTurn;
	private static ArrayList<String> bufferMessages;
	public static Scanner input;
	
	private static void init_game() {
		bufferMessages = new ArrayList<String>();
		/* Start Network configuration */
		// Find ip address
		InetAddress my_ip = TokenRing.getLocalIP(), next_ip = null;
		int next_id = 0, player_id = 0;
		String ip = my_ip.toString().split("/")[1];
			
		try {
			// Open file with ip addresses
	        File f = new File("src/ip_addresses.txt");
	        BufferedReader buffer = new BufferedReader(new FileReader(f));
	        String line = "";
	            
	    	// Find id based on ip address
	        while (((line = buffer.readLine()) != null) && !(line.split(" ")[0].equals("*")));
	        if(line == null || (line != null && !line.split(" ")[2].equals(ip))) {
	        	System.err.println("[init_game] Erro ao encontrar id do jogador");
	        	System.exit(0);
	        }
	        
	        player_id = Integer.parseInt(line.split(" ")[1]);
	        System.out.println("Seu Id: "+player_id);
	        
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
		
		next_id = (player_id+1)%5 + (player_id+1)/5;
		network = new TokenRing(player_id, next_id, next_ip);
		/* End Network configuration */
	}
	
	public static void main(String[] args) {
		int id_target;
		Point coordinates = new Point();
		input = new Scanner(System.in);
		
		init_game();
		
		Point[][] navios = new Point[2][3];
		System.out.println("Entre com as 3 coordenadas (adjacentes) de dois navios (1 - 5): ");
		System.out.println("Exemplo de entrada: 1 5 2 5 3 5 2 1 2 2 2 3");
		for (int i = 0; i < 2; i++) {	
			for (int j = 0; j < 3; j++) {
				coordinates.x = input.nextInt()-1;
				coordinates.y = input.nextInt()-1;
				navios[i][j] = new Point(coordinates.x, coordinates.y);
			}
			System.out.println();
		}
		
		player = new Player(TokenRing.getMyId()-1, navios);
				
		if(TokenRing.getMyId() == 1)
			myTurn = true;
		else {
			myTurn = false;
			System.out.println("Aguarde...");
		}
		// Starts listening on the network
		network.listener();
		
		gameOver = false;
		while(!gameOver) {
			try {
				while(bufferMessages.size() > 0) {
					treatMessage(bufferMessages.remove(0));
				}
				if(myTurn) {
					System.out.println("\n[Iniciando turno de ataque]");
					imprimeTabuleiros();
					
					System.out.println("\nEntre com o Id do Jogador alvo: ");
					id_target = input.nextInt();
					
					System.out.println("\nEntre com as coordenadas da célula alvo (1 - 5): ");
					System.out.println("Exemplo de entrada: 1 3");
					coordinates.x = input.nextInt()-1;
					coordinates.y = input.nextInt()-1;
					
					attack(id_target, coordinates);
					System.out.println("\n[Turno de ataque encerrado]");
					myTurn = false;
				}
				
				Thread.sleep(100);
			}
			catch (InterruptedException e) {e.printStackTrace();}
		}
		
		input.close();
	}
	
	public static boolean getGameStatus(){
		return gameOver;
	}
	
	public static void gameOver(){
		System.out.println("\n\nGAME OVER");
		gameOver = true;
	}
	
	public static void imprimeTabuleiros() {
		System.out.println("Seu tabuleiro (Id: "+TokenRing.getMyId()+"):");
		Player.printBoard(TokenRing.getMyId()-1);
		for (int i = 0; i < 4; i++) {
			if(i != (TokenRing.getMyId()-1) && Player.isActive(i)) {
				System.out.println("\nTabuleiro do Jogador "+(i+1)+":");
				Player.printBoard(i);
			}
		}
	}
	
	public static void nextTurn() {
		int next = Player.nextAvaible();
		if(next == TokenRing.getMyId()) {
			System.out.println("\n\nVocê venceu!!!");
			gameOver();
		}
		else {
			String msg = new String(TokenRing.getMyId()+";"+next+";0;0;");
			network.speaker(true, msg);
		}
	}
	
	public static void attack(int id_target, Point coordinates) {
		if((id_target <= 4 && id_target >= 1) && 
				(coordinates.x < 5 && coordinates.x >= 0 && coordinates.y < 5 && coordinates.y >= 0) &&
				Player.getCell(id_target-1, coordinates) == Cell.UNKNOWN && Player.isActive(id_target-1)
				&& id_target != TokenRing.getMyId()) {
			String msg = new String(TokenRing.getMyId()+";"+id_target+";0;1;"
					+coordinates.x+";"+coordinates.y+";0;0,0,0,0,0,0,0;");
			network.speaker(true, msg);
		}
		else {
			nextTurn();
			System.out.println("\nJogada invalida, perdeu a vez.\nAguarde...");
		}
	}
	
	public static void updateBoard(int target, Point coordinates, String result, String sank) {
		String msg = null;
		for (int player = 1; player <= 4; player++) {
			if(player != TokenRing.getMyId() && player != target && Player.isActive(player-1)) {
				msg = TokenRing.getMyId()+";"+player+";0;2;"+coordinates.x+";"+coordinates.y+";"
						+result+";"+sank+";"+target+";";
				network.speaker(true, msg);
			}
		}
	}
	
	public static void addBufferMessage(String msg) {
		bufferMessages.add(msg);
	}
	
	public static void treatMessage(String msg) {
		String[] mensagem = msg.split(";");
		if(mensagem.length < 4) {
			System.err.println("[treatMessage] Mensagem recebida inválida: tamanho inválido.");
			System.exit(0);
		}
		if(Integer.parseInt(mensagem[0]) == TokenRing.getMyId() || mensagem[3].equals("2")) {
			// Takes the result of an attack or update board
			Cell result = null;
			switch (mensagem[6]) {
			case "SHIP":
				result = Cell.SHIP;
				break;
			case "WATER":
				result = Cell.WATER;
				break;
			default:
				System.err.println("[treatMessage] "+msg+": mensagem inválida");
				System.exit(0);
				break;
			}
			int target = 0;
			if(Integer.parseInt(mensagem[0]) == TokenRing.getMyId())
				target = Integer.parseInt(mensagem[1]);
			else
				target = Integer.parseInt(mensagem[8]);
			Point coordinates = new Point(Integer.parseInt(mensagem[4]),
					Integer.parseInt(mensagem[5]));
			player.setBoardCell(target-1, coordinates, result);
			
			boolean origin_me = false;
			if(Integer.parseInt(mensagem[0]) == TokenRing.getMyId()) {
				updateBoard(target, coordinates, result.toString(), mensagem[7]);	
				origin_me = true;
			}
			else {
				mensagem[2] = "1"; // ACK flag
				network.speaker(false, String.join(";", mensagem)+";");
			}
			mensagem = mensagem[7].split(","); // Afundou, X_1, Y_1, X_2, Y_2, X_3, Y_3;
			if(mensagem[0].equals("1")) {
				Point[] t = new Point[3];
				for (int i = 0, x = 1; i < 3; i++, x+=2) {
					t[i] = new Point(Integer.parseInt(mensagem[x]), Integer.parseInt(mensagem[x+1]));					
				}
				player.shipSank(target-1, t);
			}
			
			if(origin_me) {
				// Now that you've updated everyone on the outcome of the attack, pass the token
				nextTurn();
			}
		}			
		else {
			switch(mensagem[3]) {
			case "0": // Received token: Origin; Destino; ACK; 0;
				mensagem[2] = "1"; // ACK flag
				msg = String.join(";", mensagem)+";";
				network.speaker(false, msg);
				
				myTurn = true;
				break;
			case "1": // Origem; Destino; ACK; 1; X; Y; Resultado, Afundou, X_1, Y_1, X_2, Y_2, X_3, Y_3;
				mensagem[2] = "1"; // ACK flag
				Point coordinates = new Point(Integer.parseInt(mensagem[4]),
						Integer.parseInt(mensagem[5]));
				
				mensagem[6] = player.getResult(coordinates); // Result flag
				if(mensagem[6].equals(Cell.SHIP.toString())) {
					// Hit the ship
					mensagem[7] = player.getShipStatus(coordinates);
					System.out.println("\nVocê sofreu um ataque do Jogador "+(Integer.parseInt(mensagem[0]))+
					" e o navio na coordenada ("+(coordinates.x+1)+","+(coordinates.y+1)+") foi atingido.");
				}
				else
					System.out.println("\nVocê foi atacado pelo Jogador "+(Integer.parseInt(mensagem[0]))+
							" na coordenada ("+(coordinates.x+1)+", "+(coordinates.y+1)+"), mas nenhum navio foi atingido.");
				msg = String.join(";", mensagem);
				network.speaker(false, msg);
				break;
			default:
				System.err.println("[treatMessage] "+msg+": operação inválida");
				System.exit(0);
				break;
			}
		}
		System.out.println("\nAguarde...");
	}
}
