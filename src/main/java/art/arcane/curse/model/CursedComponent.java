package art.arcane.curse.model;

import art.arcane.curse.Curse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

@Data
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
public class CursedComponent {
    private final CursedContext context;

    /**
     * Retype a component to a new class
     *
     * @param clazz the new class
     * @return itself
     */
    public CursedComponent type(Class<?> clazz) {
        context.type(clazz);
        return this;
    }

    /**
     * Assign an instance to this component
     *
     * @param instance the instance
     * @return itself
     */
    public CursedComponent instance(Object instance) {
        context.instance(instance);
        return this;
    }

    public <A extends Annotation> A annotated(Class<A> annotation) {
        return context.type().getDeclaredAnnotation(annotation);
    }

    public Stream<CursedMethod> declaredMethods() {
        return Arrays.stream(context.type().getDeclaredMethods()).map(i -> new CursedMethod(context(), i));
    }

    public Stream<CursedMethod> methods() {
        return getMethods(context.type()).map(i -> new CursedMethod(context(), i));
    }

    public CtType<?> model() {
        Launcher l = new Launcher();
        File t = Curse.temp("lchr", UUID.randomUUID() + ".java");
        String src = Curse.decompile(context.type());
        Curse.write(t, src);
        l.addInputResource(t.getAbsolutePath());
        return l.buildModel().filterChildren((CtType<?> i) -> i.getQualifiedName().equals(context.type().getCanonicalName())).first();
    }

    /**
     * Get the instance of this component
     *
     * @return the instance
     */
    public <T> T instance() {
        return (T) context.instance();
    }

    /**
     * Get the class of this component
     *
     * @return the class
     */
    public Class<?> type() {
        return context.type();
    }

    /**
     * Tries to find a constructor to call otherwise uses forceMake
     *
     * @return the new instance wrapped in a cursed component
     */
    public CursedComponent make() {
        try {
            return construct();
        }

        catch(Throwable e) {

        }

        return forceMake();
    }

    /**
     * Create a new instance of this component class WITHOUT CALLING ITS CONSTRUCTOR
     * This is dangerous but sometimes useful, just make sure to define the final fields after initialization...
     *
     * @return the new instance wrapped in a cursed component
     */
    public CursedComponent forceMake() {
        try {
            return Curse.on(type()).instance(unsafe().allocateInstance(type()));
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private Unsafe unsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Properly call the object's constructor
     *
     * @param args the arguments
     * @return a cursed component of the new instance
     */
    public CursedComponent construct(Object... args) {
        List<Constructor<?>> c = getConstructors(context.type()).filter(i -> i.getParameterCount() == args.length).toList();

        for (Constructor<?> i : c) {
            System.out.println("Found constructor: " + i + " with args " + Arrays.deepToString(args));
            try {
                return Curse.on(i.newInstance(args));
            } catch (Throwable ignored) {

            }
        }

        throw new RuntimeException("No constructor found for " + context.type() + " with args (" + Arrays.deepToString(args) + ")");
    }

    public Stream<CursedField> fields() {
        return getFields(type()).map(i -> new CursedField(context(), i));
    }

    public Stream<CursedField> instanceFields() {
        return fields().filter(i -> !Modifier.isStatic(i.getMember().getModifiers()));
    }

    public Stream<CursedField> staticFields() {
        return fields().filter(i -> Modifier.isStatic(i.getMember().getModifiers()));
    }

    public boolean hasInstance() {
        return instance() != null;
    }

    public Optional<CursedField> fuzzyField(Class<?> type) {
        return fuzzyField(type, !hasInstance());
    }

    public Optional<CursedField> fuzzyField(Class<?> type, boolean staticField) {
        return fields().filter(i -> i.isStatic() == staticField && i.field().getType().equals(type)).findFirst()
                .or(() -> fields().filter(i -> i.isStatic() == staticField && (i.field().getType().isAssignableFrom(type))).findFirst());
    }

    public Optional<CursedField> fuzzyField(FuzzyField invocation) {
        return fields()
                .filter(i -> invocation.isStaticField() == i.isStatic())
                .filter(i -> invocation.getType().equals(i.field().getType()))
                .filter(i -> (invocation.getPossibleNames() == null || invocation.getPossibleNames().isEmpty()) || invocation.getPossibleNames().contains(i.field().getName()))
                .findFirst();
    }

    public Optional<CursedMethod> fuzzyMethod(FuzzyMethod invocation) {
        return getMethods(type())
                .filter(i -> invocation.isStaticMethod() == Modifier.isStatic(i.getModifiers()))
                .filter(i -> invocation.getReturns().equals(i.getReturnType()))
                .filter(i -> (invocation.getPossibleNames() == null || !invocation.getPossibleNames().isEmpty()) || invocation.getPossibleNames().contains(i.getName()))
                .filter(i -> {
                    if (((invocation.getParameters() == null || invocation.getParameters().isEmpty()) && i.getParameterCount() == 0)) {
                        return true;
                    }

                    if (invocation.getParameters() != null && i.getParameterCount() == invocation.getParameters().size()) {
                        for (int jj = 0; jj < i.getParameterCount(); jj++) {
                            Class<?> a = i.getParameterTypes()[jj];
                            Class<?> b = invocation.getParameters().get(jj);

                            if (a.isAssignableFrom(b) || b.isAssignableFrom(a)) {
                                return true;
                            }
                        }
                    }

                    return false;
                })
                .map(i -> new CursedMethod(context(), i))
                .findFirst();
    }

    public <T> T getTyped(Class<?> type) {
        return (T) fuzzyField(type).map(i -> i.get())
                .or(() -> fuzzyMethod(FuzzyMethod.builder()
                .parameters(new ArrayList<>())
                .returns(type)
                .staticMethod(!hasInstance())
                .build()).map(i -> i.invoke()));
    }

    public <T> T get(String name) {
        try {
            return field(name).get();
        } catch (Throwable ignored) {

        }

        try {
            return method("get" + capitalize(name)).invoke();
        } catch (Throwable ignored) {

        }

        throw new RuntimeException("No such field or getter: " + name + " or get" + capitalize(name) + "()");
    }

    public CursedComponent set(String name, Object v) {
        try {
            return setOrThrow(name, v);
        } catch (Throwable e) {
            return this;
        }
    }

    public CursedComponent setOrThrow(String name, Object v) {
        try {
            field(name).set(v);
            return this;
        } catch (Throwable ignored) {

        }

        try {
            method("set" + capitalize(name), v.getClass()).invoke(v);
            return this;
        } catch (Throwable ignored) {
        }

        try {
            getMethods(context().type()).filter(i -> i.getName().equals("set" + capitalize(name))).findFirst().ifPresent(i -> {
                try {
                    i.invoke(context().instance(), v);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            return this;
        } catch (Throwable ignored) {
        }

        throw new RuntimeException("No such field or setter: " + name + " or set" + capitalize(name) + "(" + v.getClass() + "). Also searched for any setter with any parameter type. If you are trying to set an object through a setter which does not have the same exact parameter type as your value, use set(name, value, type) instead.");
    }

    public CursedComponent set(String name, Object v, Class<?> type) {
        try {
            field(name).set(v);
            return this;
        } catch (Throwable ignored) {

        }

        try {
            method("set" + capitalize(name), type).invoke(v);
            return this;
        } catch (Throwable ignored) {

        }

        throw new RuntimeException("No such field or setter: " + name + " or set" + capitalize(name) + "(" + v.getClass() + ").");
    }

    private static String capitalize(String f) {
        return Character.toUpperCase(f.charAt(0)) + f.substring(1);
    }

    public CursedField field(Class<?> type) {
        return getFields(context().type()).filter((i) -> i.getType().equals(type)).findFirst().map(i -> field(i.getName()))
                .orElseThrow(() -> new RuntimeException("No field of type " + type + " in " + context.type()));
    }

    public CursedMethod methodReturning(Class<?> type) {
        return getMethods(context().type()).filter((i) -> i.getReturnType().equals(type)).findFirst().map(i -> method(i.getName(), i.getParameterTypes()))
                .orElseThrow(() -> new RuntimeException("No method returning type " + type + " in " + context.type()));
    }

    public CursedMethod methodReturningArgs(Class<?> type, Class<?>... args) {
        return getMethods(context().type())
                .filter((i) -> i.getReturnType().equals(type))
                .filter((i) -> Arrays.equals(i.getParameterTypes(), args))
                .findFirst().map(i -> method(i.getName(), i.getParameterTypes()))
                .orElseThrow(() -> new RuntimeException("No method returning type " + type + " with args (" + Arrays.deepToString(args) + ") in " + context.type()));
    }

    public CursedMethod methodArgs(Class<?>... args) {
        return getMethods(context().type())
                .filter((i) -> Arrays.equals(i.getParameterTypes(), args))
                .findFirst().map(i -> method(i.getName(), i.getParameterTypes()))
                .orElseThrow(() -> new RuntimeException("No method with args (" + Arrays.deepToString(args) + ") in " + context.type()));
    }

    public Optional<CursedField> optionalField(String field) {
        return Optional.ofNullable(getField(context.type(), field)).map((i) -> new CursedField(context, i));
    }

    public CursedField field(String field) {
        return optionalField(field).get();
    }

    public Optional<CursedMethod> optionalMethod(String method, Class<?>... args) {
        return Optional.ofNullable(getMethod(context.type(), method, args)).map((i) -> new CursedMethod(context, i));
    }

    public CursedMethod method(String method, Class<?>... args) {
        return optionalMethod(method, args).get();
    }

    public Optional<CursedConstructor> optionalConstructor(Class<?>... args) {
        return Optional.ofNullable(getConstructor(context.type(), args)).map((i) -> new CursedConstructor(context, i));
    }

    public CursedConstructor constructor(Class<?>... args) {
        return optionalConstructor(args).get();
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

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            return clazz.getDeclaredMethod(methodName, args);
        } catch (NoSuchMethodException | SecurityException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getMethod(superClass, methodName, args);
            }
        }
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... args) {
        try {
            return clazz.getDeclaredConstructor(args);
        } catch (NoSuchMethodException | SecurityException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                return null;
            } else {
                return getConstructor(superClass, args);
            }
        }
    }

    private static Stream<Field> getFields(Class<?> clazz) {
        Stream.Builder<Field> fields = Stream.builder();

        while (clazz != Object.class) {
            for (Field i : clazz.getDeclaredFields()) {
                fields.add(i);
            }

            clazz = clazz.getSuperclass();
        }

        return fields.build();
    }

    private static Stream<Method> getMethods(Class<?> clazz) {
        Stream.Builder<Method> methods = Stream.builder();

        while (clazz != Object.class) {
            for (Method i : clazz.getDeclaredMethods()) {
                methods.add(i);
            }

            clazz = clazz.getSuperclass();
        }

        return methods.build();
    }

    private static Stream<Constructor<?>> getConstructors(Class<?> clazz) {
        Stream.Builder<Constructor<?>> constructors = Stream.builder();

        while (clazz != Object.class) {
            for (Constructor<?> i : clazz.getDeclaredConstructors()) {
                constructors.add(i);
            }

            clazz = clazz.getSuperclass();
        }

        return constructors.build();
    }
}
