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

    public <P> void register(int id, ConnectionState state, Side bound, Class<P> packet)
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

                if (types.length < 2 || types[0] != NetworkConnection.class)
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " requires 2 parameters (NetworkConnection, ? extends Packet) to be registered as handler (@Handler annotation present)");
                }

                if (getPacketInfos(types[1]) == null)
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " second parameter (supposed to be the Packet to handle) is " + types[1].getName() + ", but it isn't registered as a packet, use PacketRegistry#register");
                }

                if (!Modifier.isPublic(method.getModifiers()))
                {
                    throw new IllegalArgumentException("Method " + handler.getClass().getName() + "#" + method.getName() + " is not public : can't be registered as handler");
                }

                handle(types[1], (packet, connection) -> {
                    try
                    {
                        method.invoke(handler, packet, connection);
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

    public Class find(int id, ConnectionState state, Side bound)
    {
        TIntObjectMap<Class> map = getPacketsFor(state, bound);

        if (map == null)
        {
            return null;
        }

        return map.get(id);
    }

    public TIntObjectMap<Class> getPacketsFor(ConnectionState state, Side bound)
    {
        return bound == Side.CLIENT ? state.getClientPackets() : state.getServerPackets();
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
