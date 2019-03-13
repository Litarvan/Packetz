package fr.litarvan.packetz.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.litarvan.packetz.PacketRegistry;
import fr.litarvan.packetz.example.client.ClientExample;
import fr.litarvan.packetz.example.server.ServerExample;

public class PacketzTests
{
	public static final String INITIAL_STATE = "initial";

	public static void main(String[] args) throws IOException
	{
		PacketRegistry registry = new PacketRegistry();
		registry.register(PacketToClientExample.class);
		registry.register(PacketToServerExample.class);

		ServerSocket serverSocket = new ServerSocket(1234);
		ServerExample server = new ServerExample(registry);

		new Thread(() -> {
			try {
				server.start(serverSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		Socket clientSocket = new Socket("127.0.0.1", 1234);
		ClientExample client = new ClientExample(registry);

		client.start(clientSocket);
	}
}
