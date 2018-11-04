package fr.litarvan.packetz.serializing;

import java.io.IOException;

import fr.litarvan.packetz.io.StreamReader;
import fr.litarvan.packetz.io.StreamWriter;

public interface PacketSerializer<P>
{
    void read(P packet, StreamReader in) throws IOException;
    void write(P packet, StreamWriter out) throws IOException;
}
