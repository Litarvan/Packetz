package fr.litarvan.packetz;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class StatePackets
{
	private TIntObjectMap<Class> clientPackets;
	private TIntObjectMap<Class> serverPackets;

	public StatePackets()
	{
		this.clientPackets = new TIntObjectHashMap<>();
		this.serverPackets = new TIntObjectHashMap<>();
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
