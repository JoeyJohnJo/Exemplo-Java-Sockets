package client;

import java.util.Scanner;

public class Main {
	// Computador local. Mudar para o IP do computador rodando a aplicação do servidor
	public static final String IP = "127.0.0.1";
	public static final int PORTA = 54159;
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Client cliente = new Client(IP, PORTA);

		Thread lerMensagens = new Thread(() -> {
			while (cliente.conectado()) {
				cliente.enviar(scanner.nextLine());
			}
		});

		lerMensagens.start();
		cliente.aguardarMensagemDoServidor();
	}
}
