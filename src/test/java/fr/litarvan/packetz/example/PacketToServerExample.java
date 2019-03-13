package fr.litarvan.packetz.example;

import java.io.IOException;

import fr.litarvan.packetz.Packet;
import fr.litarvan.packetz.io.StreamReader;
import fr.litarvan.packetz.io.StreamWriter;
import fr.litarvan.packetz.serializing.Deserializer;
import fr.litarvan.packetz.serializing.Serializer;

import static fr.litarvan.packetz.Side.*;
import static fr.litarvan.packetz.example.PacketzTests.*;

@Packet(id = 0x01, state = INITIAL_STATE, bound = SERVER)
public class PacketToServerExample
{
	// A data we want to send
	private String version;

	// Always have an empty constructor for initialization when receiving packet
	public PacketToServerExample()
	{
	}

	public PacketToServerExample(String version)
	{
		this.version = version;
	}

	// This one has manual serializing for demonstration purpose
	@Serializer
	public void serialize(StreamWriter writer) throws IOException
	{
		writer.writeString(version);
	}

	@Deserializer
	public void deserialize(StreamReader reader) throws IOException
	{
		this.version = reader.readString();
	}

	public String getVersion()
	{
		return version;
	}
}
