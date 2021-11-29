package server;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Server servidor = new Server();

		Thread lerMensagens = new Thread(() -> {
			while (servidor.conectado()) {
				servidor.enviar(scanner.nextLine());
			}
		});

		lerMensagens.start();
		servidor.aguardarEResponderCliente();
	}
}
