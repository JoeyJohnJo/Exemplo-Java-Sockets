package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

public class Client {
	public static final String EXIT_FLAG = "sair";

	private Socket socketCliente; // Representa o socket do cliente que conectar com o servidor

	// Este é o canal de mensagens do cliente para o servidor. Tudo que o cliente enviar para o servidor será enviado usando este PrintWriter
	private PrintWriter saida;

	// Este é o canal de mensagens do servidor para o cliente. Tudo que o servidor enviar para o cliente deve ser lido por aqui
	private BufferedReader entrada;

	// Mapa de ações que o cliente deve executar para cada mensagem que o servidor enviar
	private final Map<String, Consumer<String>> relacaoMensagemResposta = Map.of(
		EXIT_FLAG, this::parar
	);

	public Client(String ip, int porta) {
		conectar(ip, porta);
	}

	public void conectar(String ip, int porta) {
		int erros = 0;
		boolean sair;
		do {
			sair = true;
			try {
				System.out.println("Conectando...");
				socketCliente = new Socket(ip, porta); // Estabelece a conexão com o servidor
				saida = new PrintWriter(socketCliente.getOutputStream(), true);
				entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
				System.out.println("Conectando!");
			} catch (IOException e) {
				erros++;
				if (erros == 2) {
					System.out.println("Não foi possível estabelecer uma conexão. Tente novamente mais tarde");
				}
				else {
					sair = false;
					System.out.println("Ocorreu um erro ao conectar com o servidor. Tentando novamente...");
					System.out.println("Descrição do erro: ");
					e.printStackTrace();
				}
			}
		} while (erros < 2 && !sair);
	}

	public void aguardarMensagemDoServidor() {
		try {
			String mensagemServidor;
			while ((mensagemServidor = entrada.readLine()) != null) {
				// Executa a ação equivalente à mensagem enviada pelo cliente
				relacaoMensagemResposta.getOrDefault(mensagemServidor, this::comportamentoPadrao).accept(mensagemServidor);
			}
		} catch (IOException e) {
			parar("");
		}
	}

	// Caso não tenha nenhum comando especifico a ser executado como no caso de "sair", apenas escreva a mensagem na tela
	public void comportamentoPadrao(String mensagem) {
		System.out.printf("%nServidor: %s%n", mensagem);
		System.out.print("Cliente (Você): " );
	}

	public void enviar(String mensagem) {
		saida.println(mensagem);
		if (mensagem.equals(EXIT_FLAG))
			parar(mensagem);
	}

	// Liberar recursos do sistema ao fechar a conexão
	// O parâmetro String nada contém a mensagem que o servidor enviou, neste caso não está sendo utilizada,
	// mas caso houvesse utilização, a mensagem está contida no parâmetro
	public void parar(String nada) {
		try {
			System.out.println("Conexão interrompida. Fechando...");
			entrada.close();
			saida.close();
			socketCliente.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public boolean conectado() {
		return !socketCliente.isClosed();
	}
}
