package fr.litarvan.packetz.serializing;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import fr.litarvan.packetz.io.StreamReader;
import fr.litarvan.packetz.io.StreamWriter;

public abstract class FieldType
{
	private static final List<FieldType> types = new ArrayList<>();

	static {
		Stream.of(PrimitiveTypes.values())
			  .map(PrimitiveFieldType::new)
			  .forEach(FieldType::register);
	}

	private Class type;
	private Class<? extends Annotation> annotation;

	public FieldType(Class type, Class<? extends Annotation> annotation)
	{
		this.type = type;
		this.annotation = annotation;
	}

	public abstract void serialize(StreamWriter out, Object value) throws IOException;
	public abstract Object deserialize(StreamReader in) throws IOException;

	public Class getType()
	{
		return type;
	}

	public Class<? extends Annotation> getAnnotation()
	{
		return annotation;
	}

	public static void register(FieldType type)
	{
		types.add(type);
	}

	public static FieldType[] getTypes()
	{
		return types.toArray(new FieldType[0]);
	}

	public static class PrimitiveFieldType extends FieldType
	{
		private PrimitiveTypes primitive;

		public PrimitiveFieldType(PrimitiveTypes primitive)
		{
			super(primitive.type, primitive.annotation);
			this.primitive = primitive;
		}

		@Override
		public void serialize(StreamWriter out, Object value) throws IOException
		{
			switch (primitive)
			{
				case BOOLEAN:
					out.writeBoolean((boolean) value);
					break;
				case BYTE:
					out.writeByte((byte) value);
					break;
				case UNSIGNED_BYTE:
					out.writeUnsignedByte((int) value);
					break;
				case SHORT:
					out.writeShort((short) value);
					break;
				case UNSIGNED_SHORT:
					out.writeUnsignedShort((int) value);
					break;
				case INT:
					out.writeInt((int) value);
					break;
				case LONG:
					out.writeLong((long) value);
					break;
				case FLOAT:
					out.writeFloat((float) value);
					break;
				case DOUBLE:
					out.writeDouble((double) value);
					break;
				case STRING:
					out.writeString((String) value);
					break;
				case VAR_INT:
					out.writeVarInt((int) value);
					break;
				case VAR_LONG:
					out.writeVarLong((long) value);
					break;
			}
		}

		@Override
		public Object deserialize(StreamReader in) throws IOException
		{
			switch (primitive)
			{
				case BOOLEAN:
					return in.readBoolean();
				case BYTE:
					return in.readByte();
				case UNSIGNED_BYTE:
					return in.readUnsignedByte();
				case SHORT:
					return in.readShort();
				case UNSIGNED_SHORT:
					return in.readUnsignedShort();
				case INT:
					return in.readInt();
				case LONG:
					return in.readLong();
				case FLOAT:
					return in.readFloat();
				case DOUBLE:
					return in.readDouble();
				case STRING:
					return in.readString();
				case VAR_INT:
					return in.readVarInt();
				case VAR_LONG:
					return in.readVarLong();
				default:
					return null;
			}
		}
	}

	public static enum PrimitiveTypes
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

		PrimitiveTypes(Class type)
		{
			this(type, null);
		}

		PrimitiveTypes(Class type, Class<? extends Annotation> annotation)
		{
			this.type = type;
			this.annotation = annotation;
		}

		public Class getType()
		{
			return type;
		}

		public Class<? extends Annotation> getAnnotation()
		{
			return annotation;
		}
	}
}
