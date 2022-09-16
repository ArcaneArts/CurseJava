package art.arcane.curse;

import art.arcane.curse.model.CursedComponent;
import art.arcane.curse.model.CursedContext;
import art.arcane.curse.model.CursedField;
import art.arcane.curse.model.CursedMember;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Optional;

public class Curse {
    public static CursedComponent on(Object instance) {
        return new CursedComponent(new CursedContext().instance(instance).type(instance.getClass()));
    }

    public static CursedComponent on(Class<?> clazz) {
        return new CursedComponent(new CursedContext().type(clazz));
    }
}
