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
    public static Stream<CursedMethod> withMethod(Class<?> sourceJarClass, String name, Class<?>... parameters) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalMethod(name, parameters).isPresent())
                .map(i -> Curse.on(i).method(name, parameters));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedConstructor> withConstructor(Class<?> sourceJarClass, Class<?>... parameters) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalConstructor(parameters).isPresent())
                .map(i -> Curse.on(i).constructor(parameters));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedField> withField(Class<?> sourceJarClass, String name) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalField(name).isPresent())
                .map(i -> Curse.on(i).field(name));
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    public static Stream<CursedComponent> where(Class<?> sourceJarClass, Predicate<Class<?>> c) {
        try {
            return new JarLoader(sourceJarClass).all().map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedComponent> whereInPackage(Class<?> sourceJarClass, Predicate<Class<?>> c, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedComponent> implemented(Class<?> sourceJarClass, Class<?> interfaceOrClass) {
        try {
            return new JarLoader(sourceJarClass).all(interfaceOrClass).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedComponent> implementedInPackage(Class<?> sourceJarClass,  Class<?> interfaceOrClass, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg, interfaceOrClass).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedComponent> annotated(Class<?> sourceJarClass, Class<? extends Annotation> annotation) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> i.isAnnotationPresent(annotation)).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedComponent> annotatedName(Class<?> sourceJarClass, String annotation) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Arrays.stream(i.getDeclaredAnnotations()).anyMatch(f -> f.getClass().getSimpleName().equals(annotation))).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<CursedComponent> annotatedInPackage(Class<?> sourceJarClass, Class<? extends Annotation> annotation, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg).filter(i -> i.isAnnotationPresent(annotation)).map(Curse::on);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CursedComponent on(Object instance) {
        return new CursedComponent(new CursedContext().instance(instance).type(instance.getClass()));
    }

    public static CursedComponent on(Class<?> clazz) {
        return new CursedComponent(new CursedContext().type(clazz));
    }
}
