package fr.litarvan.packetz.example.server;

import fr.litarvan.packetz.NetworkConnection;
import fr.litarvan.packetz.example.PacketToClientExample;
import fr.litarvan.packetz.example.PacketToServerExample;

public class ServerHandlerExample
{
	public void handle(PacketToServerExample example, NetworkConnection connection) // NetworkConnection is optional
	{
		System.out.println("Received client connection version : '" + example.getVersion() + "'");
		connection.sendPacket(new PacketToClientExample("The example server"));
	}
}
