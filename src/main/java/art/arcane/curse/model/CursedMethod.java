package art.arcane.curse.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CursedMethod extends CursedExecutable {
    private final Method method;

    public CursedMethod(CursedContext context, Method method) {
        super(context, method);
        this.method = method;
    }
}
