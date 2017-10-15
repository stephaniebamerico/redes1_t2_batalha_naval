
public class Teste_listener {

	public static void main(String[] args) {
		//TokenRing t2 = new TokenRing(1, 2, TokenRing.getLocalIP());
		
		//t2.listener();
		String s = "oi;tudo;bem;";
		String[] list = s.split(";");
		String teste = String.join(";", list);
		System.out.println(teste);
	}

}
