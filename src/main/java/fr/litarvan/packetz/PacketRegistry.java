package fr.litarvan.packetz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import fr.litarvan.packetz.serializing.AutoSerializer;
import fr.litarvan.packetz.serializing.AutoSerializing;
import fr.litarvan.packetz.serializing.ReflectionSerializer;
import gnu.trove.map.TIntObjectMap;

public class PacketRegistry
{
    // Packet handlers and serializers
    private Map<Class, PacketContainer> packetContainers = new HashMap<>();

    // Packet annotations, cached to not have to use reflection at each packet sending
    private Map<Class, Packet> packetInfos = new HashMap<>();

    // Packets, by state
    private Map<String, StatePackets> packets = new HashMap<>();

    public <P> void register(Class<P> packetClass)
    {
        if (!packetClass.isAnnotationPresent(Packet.class))
        {
            throw new IllegalArgumentException("Cannot register class " + packetClass.getName() + " as packet : missing annotation @Packet");
        }

        Packet packet = packetClass.getAnnotation(Packet.class);
        packetInfos.put(packetClass, packet);

        register(packet.id(), packet.state(), packet.bound(), packetClass);
    }

    public <P> void register(int id, String state, Side bound, Class<P> packet)
    {
        TIntObjectMap<Class> map = getPacketsFor(state, bound);

        if (map == null)
        {
            return;
        }

        getPacketContainer(packet).setSerializer(packet.isAnnotationPresent(AutoSerializing.class) ?
                                                 new AutoSerializer<>(packet) : new ReflectionSerializer<>(packet));

        map.put(id, packet);
    }

    public <P> void handle(Class<P> packetClass, PacketHandler<P> handler)
    {
        getPacketContainer(packetClass).setHandler(handler);
    }

    public void addHandler(Object handler) throws IllegalArgumentException
    {
        Method[] methods = handler.getClass().getDeclaredMethods();
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(Handle.class))
            {
                Class<?>[] types = method.getParameterTypes();

                if (types.length == 0 || types.length > 2)
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " requires 1 or 2 parameters (? extends Packet, NetworkConnection (optional)) to be registered as handler (@Handler annotation present)");
                }

                if (getPacketInfos(types[0]) == null)
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " first parameter (supposed to be the Packet to handle) is " + types[1].getName() + ", but it isn't registered as a packet, use PacketRegistry#register");
                }

                if (types.length == 2 && types[1] != NetworkConnection.class)
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " requires second parameter to be NetworkConnection, or not to have a second parameter, to be registered as handler (@Handler annotation present)");
                }

                if (!Modifier.isPublic(method.getModifiers()))
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " is not public : can't be registered as handler");
                }

                handle(types[0], (packet, connection) -> {
                    try
                    {
                        if (types.length == 1)
                        {
                            method.invoke(handler, packet);
                        }
                        else
                        {
                            method.invoke(handler, packet, connection);
                        }
                    }
                    catch (IllegalAccessException ignored)
                    {
                        // Can't happen
                    }
                    catch (InvocationTargetException e)
                    {
                        if (e.getTargetException() instanceof RuntimeException)
                        {
                            throw (RuntimeException) e.getTargetException();
                        }

                        throw new RuntimeException("Unhandled handler exception", e);
                    }
                });
            }
        }
    }

    public Class find(int id, String state, Side bound)
    {
        TIntObjectMap<Class> map = getPacketsFor(state, bound);

        if (map == null)
        {
            return null;
        }

        return map.get(id);
    }

    public TIntObjectMap<Class> getPacketsFor(String state, Side bound)
    {
        StatePackets list = packets.get(state);
        if (list == null) {
            list = new StatePackets();
            packets.put(state, list);
        }

        return bound == Side.CLIENT ? list.getClientPackets() : list.getServerPackets();
    }

    public <P> PacketContainer<P> getPacketContainer(Class<P> packetClass)
    {
        PacketContainer<P> container = packetContainers.get(packetClass);

        if (container == null)
        {
            container = new PacketContainer<>();
            packetContainers.put(packetClass, container);
        }

        return container;
    }

    public <P> Packet getPacketInfos(Class<P> packetClass)
    {
        return packetInfos.get(packetClass);
    }
}
