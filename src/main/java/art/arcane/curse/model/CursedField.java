package art.arcane.curse.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Accessors(chain = true, fluent = true)
public class CursedField extends CursedMember {
    @Getter
    private final Field field;

    public CursedField(CursedContext context, Field field) {
        super(context, field);
        this.field = field;
    }

    public <T> T get() {
        try {
            field.setAccessible(true);
            return (T) field.get(Modifier.isStatic(field.getModifiers()) ? null : context.instance());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Object value) {
        try {
            field.setAccessible(true);
            int modifiers = field.getModifiers();

            if (Modifier.isStatic(modifiers)) {
                if (Modifier.isFinal(modifiers)) {
                    if (Modifier.isVolatile(modifiers)) {
                        if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                            unsafe().putBooleanVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (boolean) value);
                        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                            unsafe().putIntVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (int) value);
                        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                            unsafe().putLongVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (long) value);
                        } else if (field.getType().equals(short.class) || field.getType().equals(Short.class)) {
                            unsafe().putShortVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (short) value);
                        } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                            unsafe().putDoubleVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (double) value);
                        } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                            unsafe().putFloatVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (float) value);
                        } else if (field.getType().equals(byte.class) || field.getType().equals(Byte.class)) {
                            unsafe().putByteVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (byte) value);
                        } else if (field.getType().equals(char.class) || field.getType().equals(Character.class)) {
                            unsafe().putCharVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (char) value);
                        } else {
                            unsafe().putObjectVolatile(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), value);
                        }
                    } else {
                        if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                            unsafe().putBoolean(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (boolean) value);
                        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                            unsafe().putInt(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (int) value);
                        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                            unsafe().putLong(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (long) value);
                        } else if (field.getType().equals(short.class) || field.getType().equals(Short.class)) {
                            unsafe().putShort(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (short) value);
                        } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                            unsafe().putDouble(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (double) value);
                        } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                            unsafe().putFloat(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (float) value);
                        } else if (field.getType().equals(byte.class) || field.getType().equals(Byte.class)) {
                            unsafe().putByte(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (byte) value);
                        } else if (field.getType().equals(char.class) || field.getType().equals(Character.class)) {
                            unsafe().putChar(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), (char) value);
                        } else {
                            unsafe().putObject(unsafe().staticFieldBase(field), unsafe().staticFieldOffset(field), value);
                        }
                    }
                } else {
                    field.set(null, value);
                }
            } else if (Modifier.isFinal(modifiers)) {
                if (Modifier.isVolatile(modifiers)) {
                    if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                        unsafe().putBooleanVolatile(context.instance(), unsafe().objectFieldOffset(field), (boolean) value);
                    } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                        unsafe().putIntVolatile(context.instance(), unsafe().objectFieldOffset(field), (int) value);
                    } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                        unsafe().putLongVolatile(context.instance(), unsafe().objectFieldOffset(field), (long) value);
                    } else if (field.getType().equals(short.class) || field.getType().equals(Short.class)) {
                        unsafe().putShortVolatile(context.instance(), unsafe().objectFieldOffset(field), (short) value);
                    } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                        unsafe().putDoubleVolatile(context.instance(), unsafe().objectFieldOffset(field), (double) value);
                    } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                        unsafe().putFloatVolatile(context.instance(), unsafe().objectFieldOffset(field), (float) value);
                    } else if (field.getType().equals(byte.class) || field.getType().equals(Byte.class)) {
                        unsafe().putByteVolatile(context.instance(), unsafe().objectFieldOffset(field), (byte) value);
                    } else if (field.getType().equals(char.class) || field.getType().equals(Character.class)) {
                        unsafe().putCharVolatile(context.instance(), unsafe().objectFieldOffset(field), (char) value);
                    } else {
                        unsafe().putObjectVolatile(context.instance(), unsafe().objectFieldOffset(field), value);
                    }
                } else {
                    if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                        unsafe().putBoolean(context.instance(), unsafe().objectFieldOffset(field), (boolean) value);
                    } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                        unsafe().putInt(context.instance(), unsafe().objectFieldOffset(field), (int) value);
                    } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                        unsafe().putLong(context.instance(), unsafe().objectFieldOffset(field), (long) value);
                    } else if (field.getType().equals(short.class) || field.getType().equals(Short.class)) {
                        unsafe().putShort(context.instance(), unsafe().objectFieldOffset(field), (short) value);
                    } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                        unsafe().putDouble(context.instance(), unsafe().objectFieldOffset(field), (double) value);
                    } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                        unsafe().putFloat(context.instance(), unsafe().objectFieldOffset(field), (float) value);
                    } else if (field.getType().equals(byte.class) || field.getType().equals(Byte.class)) {
                        unsafe().putByte(context.instance(), unsafe().objectFieldOffset(field), (byte) value);
                    } else if (field.getType().equals(char.class) || field.getType().equals(Character.class)) {
                        unsafe().putChar(context.instance(), unsafe().objectFieldOffset(field), (char) value);
                    } else {
                        unsafe().putObject(context.instance(), unsafe().objectFieldOffset(field), value);
                    }
                }
            } else {
                field.set(context.instance(), value);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
