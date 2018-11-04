package fr.litarvan.packetz;

@FunctionalInterface
public interface PacketHandler<T>
{
    void handle(T packet, NetworkConnection connection);
}
