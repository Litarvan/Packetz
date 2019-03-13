package fr.litarvan.packetz.serializing;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.litarvan.packetz.io.StreamReader;
import fr.litarvan.packetz.io.StreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoSerializer<P> implements PacketSerializer<P>
{
    private static final Logger log = LoggerFactory.getLogger("AutoSerializer");

    // Cache, so reflection processing is moved to initialization instead of runtime
    private Map<Field, FieldType> fields;

    public AutoSerializer(Class<P> packetClass)
    {
        this.fields = new LinkedHashMap<>();

        String[] order = packetClass.getAnnotation(AutoSerializing.class).value();

        for (String fieldName : order)
        {
            Field field;

            try
            {
                field = packetClass.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                log.error("Development error : Can't find field '{}' on class {}, as requested in @AutoSerializing annotation", fieldName, packetClass.getName());
                log.error("This can also be caused by obfuscating packet classes, you should then ignore org.stick.library.network.packets (with an 's')");
                log.error("Packet will be ignored");

                return;
            }

            Class type = field.getType();
            FieldType fieldType = null;

            for (FieldType f : FieldType.getTypes())
            {
                if (f.getType() == type && (fieldType == null || (f.getAnnotation() != null && field.isAnnotationPresent(f.getAnnotation()))))
                {
                    fieldType = f;
                }
            }

            if (fieldType == null)
            {
                log.error("Field type {} isn't supported by AutoSerialization; field will not be serialized");
                log.error("This is either a development error, or a module that doesn't support the current Stick version");
            }
            else
            {
                fields.put(field, fieldType);
            }
        }
    }

    @Override
    public void read(P packet, StreamReader in) throws IOException
    {
        for (Entry<Field, FieldType> entries : fields.entrySet())
        {
            Field field = entries.getKey();
            FieldType fieldType = entries.getValue();

            field.setAccessible(true);

            try
            {
                field.set(packet, fieldType.deserialize(in));
            }
            catch (IllegalAccessException e)
            {
                log.error("Error during auto-deserialization, couldn't access the field '" + field + "'", e);
            }

            field.setAccessible(false);
        }
    }

    @Override
    public void write(P packet, StreamWriter out) throws IOException
    {
        for (Entry<Field, FieldType> entries : fields.entrySet())
        {
            Field field = entries.getKey();
            FieldType fieldType = entries.getValue();

            field.setAccessible(true);

            try
            {
                fieldType.serialize(out, field.get(packet));
            }
            catch (IllegalAccessException e)
            {
                log.error("Error during auto-serialization, couldn't access the field '" + field + "'", e);
            }

            field.setAccessible(false);
        }
    }


}
