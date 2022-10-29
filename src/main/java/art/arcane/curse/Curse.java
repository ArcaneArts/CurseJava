package art.arcane.curse;

import art.arcane.curse.model.CursedComponent;
import art.arcane.curse.model.CursedConstructor;
import art.arcane.curse.model.CursedContext;
import art.arcane.curse.model.CursedField;
import art.arcane.curse.model.CursedMethod;
import art.arcane.curse.util.JarLoader;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Curse {
    /**
     * Find all classes with a method
     * @param sourceJarClass any class inside the jar you want to search
     * @param name the name of the method
     * @param parameters any parameters in the method
     * @return a stream of cursed methods
     */
    public static Stream<CursedMethod> withMethod(Class<?> sourceJarClass, String name, Class<?>... parameters) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalMethod(name, parameters).isPresent())
                .map(i -> Curse.on(i).method(name, parameters));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes with a constructor
     * @param sourceJarClass any class inside the jar you want to search
     * @param parameters the constructor parameters
     * @return a stream of cursed constructors
     */
    public static Stream<CursedConstructor> withConstructor(Class<?> sourceJarClass, Class<?>... parameters) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalConstructor(parameters).isPresent())
                .map(i -> Curse.on(i).constructor(parameters));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes with a field
     * @param sourceJarClass any class inside the jar you want to search
     * @param name the name of the field
     * @return a stream of cursed fields
     */
    public static Stream<CursedField> withField(Class<?> sourceJarClass, String name) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalField(name).isPresent())
                .map(i -> Curse.on(i).field(name));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param sourceJarClass any class inside the jar you want to search
     * @param name the name of the field
     * @param type the type of the field
     * @return a stream of cursed fields
     */
    public static Stream<CursedField> withField(Class<?> sourceJarClass, String name, Class<?> type) {
        try {
            return new JarLoader(sourceJarClass).all()
                .filter(i -> {
                    Optional<CursedField> f = Curse.on(i).optionalField(name);
                    return f.isPresent() && f.get().field().getType().equals(type);
                })
                .map(i -> Curse.on(i).field(name));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which match a predicate
     * @param sourceJarClass any class inside the jar you want to search
     * @param c the predicate to match
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> where(Class<?> sourceJarClass, Predicate<Class<?>> c) {
        try {
            return new JarLoader(sourceJarClass).all().map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes within a specific package (or subpackage)
     * @param sourceJarClass any class inside the jar you want to search
     * @param c the predicate to match any class
     * @param pkg the root package to search
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> whereInPackage(Class<?> sourceJarClass, Predicate<Class<?>> c, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which implement or could be casted to a specific class/interface
     * @param sourceJarClass any class inside the jar you want to search
     * @param interfaceOrClass the class/interface the classes must implement or extend or be assignable to
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> implemented(Class<?> sourceJarClass, Class<?> interfaceOrClass) {
        try {
            return new JarLoader(sourceJarClass).all(interfaceOrClass).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which implement or could be casted to a specific class/interface only in a specific package
     * @param sourceJarClass any class inside the jar you want to search
     * @param interfaceOrClass the class/interface the classes must implement or extend or be assignable to
     * @param pkg the root package to search
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> implementedInPackage(Class<?> sourceJarClass,  Class<?> interfaceOrClass, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg, interfaceOrClass).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which have a specific annotation on their type
     * @param sourceJarClass any class inside the jar you want to search
     * @param annotation the annotation the classes must have
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> annotated(Class<?> sourceJarClass, Class<? extends Annotation> annotation) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> i.isAnnotationPresent(annotation)).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which have a specific annotation on their type only in a specific package
     * @param sourceJarClass any class inside the jar you want to search
     * @param annotation the annotation the classes must have
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> annotatedName(Class<?> sourceJarClass, String annotation) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Arrays.stream(i.getAnnotations()).anyMatch(f -> f.getClass().getSimpleName().equals(annotation))).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which have a specific annotation on their type only in a specific package
     * @param sourceJarClass any class inside the jar you want to search
     * @param annotation the annotation the classes must have
     * @param pkg the root package to search
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> annotatedInPackage(Class<?> sourceJarClass, Class<? extends Annotation> annotation, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg).filter(i -> i.isAnnotationPresent(annotation)).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a cursed component on an actual instance to begin using reflection
     * @param instance the instance to reflect on
     * @return a cursed component
     */
    public static CursedComponent on(Object instance) {
        return new CursedComponent(new CursedContext().instance(instance).type(instance.getClass()));
    }

    /**
     * Get a cursed component on a class to begin using reflection
     * @param clazz the class to reflect on
     * @return a cursed component
     */
    public static CursedComponent on(Class<?> clazz) {
        return new CursedComponent(new CursedContext().type(clazz));
    }
}
