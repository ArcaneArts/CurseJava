package art.arcane.curse.model;

import java.lang.reflect.Constructor;

public class CursedConstructor extends CursedExecutable {
    private final Constructor<?> constructor;

    public CursedConstructor(CursedContext context, Constructor<?> constructor) {
        super(context, constructor);
        this.constructor = constructor;
    }

    public <T> T invoke(Object... args) {
        try {
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
