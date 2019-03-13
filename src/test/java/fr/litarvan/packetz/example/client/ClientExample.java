package fr.litarvan.packetz.example.client;

import java.io.IOException;
import java.net.Socket;

import fr.litarvan.packetz.NetworkConnection;
import fr.litarvan.packetz.PacketRegistry;
import fr.litarvan.packetz.Side;
import fr.litarvan.packetz.example.PacketToServerExample;
import fr.litarvan.packetz.example.PacketzTests;

public class ClientExample extends Thread
{
	private PacketRegistry registry;

	public ClientExample(PacketRegistry registry)
	{
		this.registry = registry;
		this.registry.addHandler(new ClientHandlerExample());
	}

	public void start(Socket socket) throws IOException
	{
		NetworkConnection connection = new NetworkConnection(Side.CLIENT, socket, registry, PacketzTests.INITIAL_STATE);
		connection.sendPacket(new PacketToServerExample("vTestExample"));

		while (true) {
			connection.processNextPacket();
		}
	}
}
