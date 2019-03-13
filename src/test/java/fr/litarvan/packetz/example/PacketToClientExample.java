package fr.litarvan.packetz.example;

import fr.litarvan.packetz.Packet;
import fr.litarvan.packetz.serializing.AutoSerializing;

import static fr.litarvan.packetz.Side.*;
import static fr.litarvan.packetz.example.PacketzTests.*;

@AutoSerializing({"name"})
@Packet(id = 0x01, state = INITIAL_STATE, bound = CLIENT)
public class PacketToClientExample
{
	// A data we went to save
	private String name;

	// Always have an empty constructor for initialization when receiving packet
	public PacketToClientExample()
	{
	}

	public PacketToClientExample(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
