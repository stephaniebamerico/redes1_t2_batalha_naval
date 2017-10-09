
public class Teste_listener {

	public static void main(String[] args) {
		TokenRing t2 = new TokenRing(1, TokenRing.getLocalIP(), 10100, 10101);
		
		t2.listener();
	}

}
