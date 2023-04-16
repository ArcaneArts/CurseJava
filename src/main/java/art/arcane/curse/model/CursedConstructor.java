package art.arcane.curse.model;

import art.arcane.curse.Curse;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

import java.lang.reflect.Constructor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CursedConstructor extends CursedExecutable {
    private final Constructor<?> constructor;

    public CursedConstructor(CursedContext context, Constructor<?> constructor) {
        super(context, constructor);
        this.constructor = constructor;
    }

    public CtConstructor<?> model() {
        return Curse.on(constructor.getDeclaringClass()).model().filterChildren((CtConstructor<?> m) ->
                m.getParameters().stream().map(i -> i.getType().getActualClass().getCanonicalName())
                        .collect(Collectors.joining(", ")).equals(Stream.of(constructor.getParameterTypes())
                                .map(Class::getCanonicalName).collect(Collectors.joining(", ")))).first();
    }

    public <T> T invoke(Object... args) {
        try {
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
