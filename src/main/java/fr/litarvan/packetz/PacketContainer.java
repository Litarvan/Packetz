package fr.litarvan.packetz;

import fr.litarvan.packetz.serializing.PacketSerializer;

public class PacketContainer<P>
{
    private PacketHandler<P> handler;
    private PacketSerializer<P> serializer;

    public PacketHandler<P> getHandler()
    {
        return handler;
    }

    public void setHandler(PacketHandler<P> handler)
    {
        this.handler = handler;
    }

    public PacketSerializer<P> getSerializer()
    {
        return serializer;
    }

    public void setSerializer(PacketSerializer<P> serializer)
    {
        this.serializer = serializer;
    }
}
