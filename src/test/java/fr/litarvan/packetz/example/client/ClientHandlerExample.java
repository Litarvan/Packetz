package fr.litarvan.packetz.example.client;

import fr.litarvan.packetz.Handle;
import fr.litarvan.packetz.example.PacketToClientExample;

public class ClientHandlerExample
{
	@Handle
	public void handleClientExample(PacketToClientExample example)
	{
		System.out.println("Server answered, hello '" + example.getName() + "' !");
	}
}
