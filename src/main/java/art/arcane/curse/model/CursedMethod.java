package art.arcane.curse.model;

import java.lang.reflect.Method;

public class CursedMethod extends CursedExecutable {
    private final Method method;

    public CursedMethod(CursedContext context, Method method) {
        super(context, method);
        this.method = method;
    }

    public <T> T invoke(Object... args) {
        try {
            method.setAccessible(true);
            return (T) method.invoke(context.instance(), args);
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
