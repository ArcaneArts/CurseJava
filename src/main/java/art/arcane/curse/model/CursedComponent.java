package art.arcane.curse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Optional;

@Data
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
public class CursedComponent {
    private final CursedContext context;

    public CursedComponent type(Class<?> clazz) {
        context.type(clazz);
        return this;
    }

    public CursedComponent instance(Object instance) {
        context.instance(instance);
        return this;
    }

    public CursedField field(String field) {
        return Optional.ofNullable(getField(context.type(), field)).map((i) -> new CursedField(context, i)).get();
    }

    public Unsafe unsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        }

        catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException | SecurityException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }
}
