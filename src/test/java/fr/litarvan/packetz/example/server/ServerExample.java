package fr.litarvan.packetz.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.litarvan.packetz.NetworkConnection;
import fr.litarvan.packetz.PacketRegistry;
import fr.litarvan.packetz.Side;
import fr.litarvan.packetz.example.PacketToServerExample;
import fr.litarvan.packetz.example.PacketzTests;

public class ServerExample
{
	private PacketRegistry registry;

	public ServerExample(PacketRegistry registry)
	{
		this.registry = registry;

		ServerHandlerExample handler = new ServerHandlerExample(); // Manual handling registration for demonstration purpose !
		this.registry.handle(PacketToServerExample.class, handler::handle);
	}

	public void start(ServerSocket server) throws IOException
	{
		Socket socket = server.accept();
		NetworkConnection connection = new NetworkConnection(Side.SERVER, socket, registry, PacketzTests.INITIAL_STATE);

		while (true) {
			connection.processNextPacket();
		}
	}
}
