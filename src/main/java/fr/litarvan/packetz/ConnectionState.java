package fr.litarvan.packetz;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class ConnectionState
{
    private int id;
    private String name;

    private TIntObjectMap<Class> clientPackets = new TIntObjectHashMap<>();
    private TIntObjectMap<Class> serverPackets = new TIntObjectHashMap<>();

    public ConnectionState(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public TIntObjectMap<Class> getClientPackets()
    {
        return clientPackets;
    }

    public TIntObjectMap<Class> getServerPackets()
    {
        return serverPackets;
    }
}
