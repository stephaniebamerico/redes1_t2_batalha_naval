import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TokenRing {
	private int id;
	private DatagramSocket socketListener, socketSpeaker;
	private InetAddress next_ip;
	private int my_port, next_port;
	
	public TokenRing(int id, InetAddress next_ip, int my_port, int next_port) {
		this.id = id;
		this.next_ip = next_ip;
		this.my_port = my_port;
		this.next_port = next_port;
	}
	
	public void listener() {
		byte[] receive_msg = new byte[1024]; // buffer for messages
		
		// Creates the socket
		try {socketListener = new DatagramSocket(my_port);} 
		catch (SocketException e) {e.printStackTrace();}
		
		System.out.println("passou com porta my: "+my_port+" next: "+next_port);
		
		// Starts listening to messages
		new Thread() {
			@Override
			public void run() {
				while(true) {
					DatagramPacket packet = new DatagramPacket(receive_msg, receive_msg.length);
					// Receive the message
					try {socketListener.receive(packet);}
					catch (IOException e) {e.printStackTrace();}
					String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
					
					// Treats the message
					if(Integer.parseInt(msg.split(";")[1]) == id) {
						// The message is for me
						System.out.println("A mensagem é para mim!!");
						Game.treatMessage(msg);
					}
					else {
						System.out.println("A mensagem não é para mim, passando adiante...");
						speaker(msg);
					}
				}
			}
		}.start();
	}
	
	public void speaker(String msg) {
		byte[] send_msg = new byte[1024]; // buffer for messages
		
		try {
			socketSpeaker = new DatagramSocket();
		
			// Send the message
			send_msg = msg.getBytes();
			DatagramPacket p2 = new DatagramPacket(send_msg, send_msg.length, next_ip, next_port);
			socketSpeaker.send(p2);
		} 
		catch (IOException e) {e.printStackTrace();}
		
		socketSpeaker.close();
	}
	
	public static InetAddress getLocalIP() {
		try {return InetAddress.getByName("localhost");}
		catch (UnknownHostException e) {e.printStackTrace();}
		return null;
	}
}
