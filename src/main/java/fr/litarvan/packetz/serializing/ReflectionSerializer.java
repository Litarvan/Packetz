package fr.litarvan.packetz.serializing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fr.litarvan.packetz.io.StreamReader;
import fr.litarvan.packetz.io.StreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionSerializer<P> implements PacketSerializer<P>
{
    private static final Logger log = LoggerFactory.getLogger("ReflectionSerializer");
    private static final String errorMessage = "Malformed packet class '%s', %s; This is either a development error, either a module that doesn't support the current Stick version";

    private Method deserialize;
    private Method serialize;

    public ReflectionSerializer(Class<P> packetClass)
    {
        for (Method method : packetClass.getDeclaredMethods())
        {
            if (method.isAnnotationPresent(Deserializer.class))
            {
                deserialize = method;

                if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != StreamReader.class)
                {
                    throw new RuntimeException(String.format(errorMessage, packetClass.getName(), "@Deserializer annotated method parameters should only be a StreamReader"));
                }
            }
            else if (method.isAnnotationPresent(Serializer.class))
            {
                serialize = method;

                if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != StreamWriter.class)
                {
                    throw new RuntimeException(String.format(errorMessage, packetClass.getName(), "@Serializer annotated method parameters should only be a StreamWriter"));
                }
            }
        }

        if (deserialize == null && serialize == null)
        {
            throw new RuntimeException("Malformed packet class '" + packetClass.getName() + "', @AutoSerializing isn't enabled, and @Deserializer and @Serializer methods are both missing; One of these is required");
        }
    }

    @Override
    public void read(P packet, StreamReader in) throws IOException
    {
        if (deserialize == null)
        {
            log.error("Tried to read a packet that hasn't a @Deserialize method (neither @AutoSerializing)");
            log.error("This is probably a bound-error ({} is probably bound to the wrong side)", packet.getClass().getName());

            return;
        }

        invoke(deserialize, packet, in);
    }

    @Override
    public void write(P packet, StreamWriter out) throws IOException
    {
        if (serialize == null)
        {
            log.error("Tried to write a packet that hasn't a @Serialize method (neither @AutoSerializing)");
            log.error("This is probably a bound-error ({} is probably bound to the wrong side)", packet.getClass().getName());

            return;
        }

        invoke(serialize, packet, out);
    }

    protected void invoke(Method method, P packet, Object arg) throws IOException
    {
        try
        {
            method.invoke(packet, arg);
        }
        catch (IllegalAccessException e)
        {
            log.error("Tried to invoke serialization/deserialization method '" + method.getName() + "' (from class " + packet.getClass().getName() + ") but it isn't public");
            log.error("Nothing will be done, packet will be ignored; this is a serious error", e);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof IOException)
            {
                throw (IOException) e.getTargetException();
            }

            if (e.getTargetException() instanceof RuntimeException)
            {
                throw (RuntimeException) e.getTargetException();
            }

            throw new RuntimeException(packet.getClass().getName() + " serializer/deserializer method threw an exception", e);
        }
    }
}
