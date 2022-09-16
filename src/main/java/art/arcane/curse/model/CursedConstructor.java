package art.arcane.curse.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class CursedConstructor extends CursedExecutable {
    private final Constructor<?> constructor;

    public CursedConstructor(CursedContext context, Constructor<?> constructor) {
        super(context, constructor);
        this.constructor = constructor;
    }
}
