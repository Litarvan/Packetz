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

            for (FieldType f : FieldType.values())
            {
                if (f.type == type && (fieldType == null || (f.annotation != null && field.isAnnotationPresent(f.annotation))))
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
                switch (fieldType)
                {
                    case BOOLEAN:
                        field.set(packet, in.readBoolean());
                        break;
                    case BYTE:
                        field.set(packet, in.readByte());
                        break;
                    case UNSIGNED_BYTE:
                        field.set(packet, in.readUnsignedByte());
                        break;
                    case SHORT:
                        field.set(packet, in.readShort());
                        break;
                    case UNSIGNED_SHORT:
                        field.set(packet, in.readUnsignedShort());
                        break;
                    case INT:
                        field.set(packet, in.readInt());
                        break;
                    case LONG:
                        field.set(packet, in.readLong());
                        break;
                    case FLOAT:
                        field.set(packet, in.readFloat());
                        break;
                    case DOUBLE:
                        field.set(packet, in.readDouble());
                        break;
                    case STRING:
                        field.set(packet, in.readString());
                        break;
                    case VAR_INT:
                        field.set(packet, in.readVarInt());
                        break;
                    case VAR_LONG:
                        field.set(packet, in.readVarLong());
                        break;
                }
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
                switch (fieldType)
                {
                    case BOOLEAN:
                        out.writeBoolean((boolean) field.get(packet));
                        break;
                    case BYTE:
                        out.writeByte((byte) field.get(packet));
                        break;
                    case UNSIGNED_BYTE:
                        out.writeUnsignedByte((int) field.get(packet));
                        break;
                    case SHORT:
                        out.writeShort((short) field.get(packet));
                        break;
                    case UNSIGNED_SHORT:
                        out.writeUnsignedShort((int) field.get(packet));
                        break;
                    case INT:
                        out.writeInt((int) field.get(packet));
                        break;
                    case LONG:
                        out.writeLong((long) field.get(packet));
                        break;
                    case FLOAT:
                        out.writeFloat((float) field.get(packet));
                        break;
                    case DOUBLE:
                        out.writeDouble((double) field.get(packet));
                        break;
                    case STRING:
                        out.writeString((String) field.get(packet));
                        break;
                    case VAR_INT:
                        out.writeVarInt((int) field.get(packet));
                        break;
                    case VAR_LONG:
                        out.writeVarLong((long) field.get(packet));
                        break;
                }
            }
            catch (IllegalAccessException e)
            {
                log.error("Error during auto-serialization, couldn't access the field '" + field + "'", e);
            }

            field.setAccessible(false);
        }
    }

    protected enum FieldType
    {
        BOOLEAN(boolean.class),
        BYTE(byte.class),
        UNSIGNED_BYTE(int.class, Unsigned.class),
        SHORT(short.class),
        UNSIGNED_SHORT(int.class, Unsigned.class),
        INT(int.class),
        LONG(long.class),
        FLOAT(float.class),
        DOUBLE(double.class),
        STRING(String.class),
        VAR_INT(int.class, VarNum.class),
        VAR_LONG(long.class, VarNum.class);

        private Class type;
        private Class<? extends Annotation> annotation;

        FieldType(Class type)
        {
            this(type, null);
        }

        FieldType(Class type, Class<? extends Annotation> annotation)
        {
            this.type = type;
            this.annotation = annotation;
        }
    }
}
