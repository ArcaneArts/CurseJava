package art.arcane.curse.model;

import art.arcane.curse.Curse;
import art.arcane.curse.util.poet.JavaFile;
import art.arcane.curse.util.poet.TypeSpec;
import spoon.Launcher;
import spoon.compiler.SpoonResource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.support.reflect.declaration.CtClassImpl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CursedMethod extends CursedExecutable {
    private final Method method;

    public CursedMethod(CursedContext context, Method method) {
        super(context, method);
        this.method = method;
    }

    public CtMethod<?> model() {
       return Curse.on(method.getDeclaringClass()).model().filterChildren((CtMethod<?> m) ->
               m.getSimpleName().equals(method.getName())
               && m.getParameters().stream().map(i -> i.getType().getActualClass().getCanonicalName())
               .collect(Collectors.joining(", ")).equals(Stream.of(method.getParameterTypes())
               .map(Class::getCanonicalName).collect(Collectors.joining(", ")))).first();
    }

    public <T> T invoke(Object... args) {
        try {
            method.setAccessible(true);
            return (T) method.invoke(context.instance(), args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
