package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

public class Server {

	public static final int PORTA = 0;
	public static final String EXIT_FLAG = "sair";

	// "Espera" por requisições feitas na rede
	private ServerSocket socketServidor; // Representa o socket do servidor
	private Socket socketCliente; // Representa o socket do cliente que conectar com o servidor

	// Este é o canal de mensagens do servidor para o cliente. Tudo que o servidor enviar para o cliente será enviado usando este PrintWriter
	private PrintWriter saida;

	// Este é o canal de mensagens do cliente para o servidor. Tudo que o cliente enviar para o servidor deve ser lido por aqui
	private BufferedReader entrada;

	// Mapa de ações que o servidor deve executar para cada mensagem que o cliente enviar
	private final Map<String, Consumer<String>> relacaoMensagemResposta = Map.of(
		EXIT_FLAG, this::parar
	);

	public Server() {
		conectar();
	}

	// Faz a tentativa de conexão duas vezes, se não conseguir
	public void conectar() {
		int erros = 0;
		boolean sair;
		do {
			sair = true;
			try {
				// Criando socket com uma porta qualquer,
				// alterar 0 para a porta desejada se quiser uma fixa
				socketServidor = new ServerSocket(PORTA);
				System.out.printf("Servidor inicializado na porta: %d%n", socketServidor.getLocalPort());
				System.out.println("Aguardando conexão do cliente...");

				// Interrompe a execução do programa até que alguém conecte com o servidor
				socketCliente = socketServidor.accept();
				saida = new PrintWriter(socketCliente.getOutputStream(), true);
				entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

				System.out.println("Conectado!");

			} catch (IOException e) {
				erros++;
				if (erros == 2) {
					System.out.println("Não foi possível estabelecer uma conexão. Tente novamente mais tarde");
				}
				else {
					sair = false;
					System.out.println("Ocorreu um erro ao conectar com o cliente. Tentando novamente...");
					System.out.println("Descrição do erro: ");
					e.printStackTrace();
				}
			}
		} while (erros < 2 && !sair);
	}

	// Espera que o cliente envie uma mensagem e executa a função corresponde a mensagem enviada
	public void aguardarEResponderCliente() {
		try {
			String mensagemCliente;
			while ((mensagemCliente = entrada.readLine()) != null) {
				// Executa a ação equivalente à mensagem enviada pelo cliente
				relacaoMensagemResposta.getOrDefault(mensagemCliente, this::comportamentoPadrao).accept(mensagemCliente);
			}
		} catch (IOException e) {
			parar("");
		}
	}

	// Liberar recursos do sistema ao fechar a conexão
	// O parametro String nada contém a mensagem que o cliente enviou, neste caso não está sendo utilizada
	// mas caso houvesse utilização, a mensagem está contida no parâmetro
	public void parar(String nada) {
		try {
			System.out.println("Conexão interrompida. Fechando...");
			entrada.close();
			saida.close();
			socketCliente.close();
			socketServidor.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	// Caso não tenha nenhum comando especifico a ser executado como no caso de "sair", apenas escreva a mensagem na tela
	public void comportamentoPadrao(String mensagem) {
		System.out.printf("%nCliente: %s%n", mensagem);
		System.out.print("Servidor (Você): " );
	}

	public void enviar(String mensagem) {
		saida.println(mensagem);
		if (mensagem.equals(EXIT_FLAG))
			parar(mensagem);

	}

	public boolean conectado() {
		return !socketServidor.isClosed();
	}
}
