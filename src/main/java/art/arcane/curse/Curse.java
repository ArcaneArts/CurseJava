package art.arcane.curse;

import art.arcane.curse.model.*;
import art.arcane.curse.util.JarLoader;
import art.arcane.curse.util.poet.JavaFile;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import sun.misc.Unsafe;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Curse {
    /**
     * Find all classes with a method
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param name           the name of the method
     * @param parameters     any parameters in the method
     * @return a stream of cursed methods
     */
    public static Stream<CursedMethod> withMethod(Class<?> sourceJarClass, String name, Class<?>... parameters) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalMethod(name, parameters).isPresent())
                    .map(i -> Curse.on(i).method(name, parameters));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop(Thread thread) {
        thread.interrupt();
        thread.suspend();
        thread.stop();
    }

    public static void resume(Thread thread) {
        thread.resume();
    }

    public static void pause(Thread thread) {
        thread.suspend();
    }

    /**
     * You get unsafe! She gets unsafe! He gets unsafe! Everybody gets unsafe!
     * @return things you want but can never have.
     */
    public static Unsafe unsafe() {
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unpark a thread unsafely
     * @param thread the thread to wake up
     */
    public static void unpark(Thread thread) {
        unsafe().unpark(thread);
    }

    /**
     * Park the current thread for an amount of time. If the time is less than 999ms the decimal
     * accuracy is used by converting to nanoseconds. from the double ms
     * @param milliseconds the milliseconds to park the thread for.
     */
    public static void park(double milliseconds) {
        if(milliseconds == 0) {
            unsafe().park(true, 0);
        }

        else if(milliseconds > 999 || (double)((long)milliseconds) == milliseconds) {
            unsafe().park(true, System.currentTimeMillis() + (long) milliseconds);
        }

        else {
            long m = (long) (milliseconds * 1000000D);
            unsafe().park(false, m);
        }
    }

    /**
     * Decompile a class into source code (string)
     * @param clazz the class to decompile
     * @return the source code or an error message
     */
    public static String decompile(Class<?> clazz) {
        try {
            if(clazz.getCanonicalName() == null) {
                return "err: null canonical";
            }

            StringWriter s = new StringWriter();
            Decompiler.decompile(clazz.getCanonicalName().replaceAll("\\Q.\\E", "/"), new PlainTextOutput(s), DecompilerSettings.javaDefaults());
            return s.toString();
        }

        catch(Throwable ignored) {
            return "err: " + ignored.getClass().getCanonicalName() + " " + ignored.getMessage();
        }
    }

    /**
     * Find all classes with a constructor
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param parameters     the constructor parameters
     * @return a stream of cursed constructors
     */
    public static Stream<CursedConstructor> withConstructor(Class<?> sourceJarClass, Class<?>... parameters) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalConstructor(parameters).isPresent())
                    .map(i -> Curse.on(i).constructor(parameters));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a temp file to write to in the system temp folder
     *
     * @param filePath the file path seperated by commas
     * @return the file with a properly created parent directory
     */
    public static File temp(String... filePath) {
        File f = new File(System.getProperty("java.io.tmpdir"), String.join(File.separator, filePath));
        f.getParentFile().mkdirs();
        return f;
    }

    /**
     * Find all classes with a field
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param name           the name of the field
     * @return a stream of cursed fields
     */
    public static Stream<CursedField> withField(Class<?> sourceJarClass, String name) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> Curse.on(i).optionalField(name).isPresent())
                    .map(i -> Curse.on(i).field(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param sourceJarClass any class inside the jar you want to search
     * @param name           the name of the field
     * @param type           the type of the field
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Try to load a class. If it cannot be found, download and inject a dependency from the repository supplied then load the class.
     * @param canonical the canonical class name
     * @param dependency the dependency such as org.something:name:version
     * @param repository the repository such as maven central https://repo1.maven.org/maven2
     * @return the class
     */
    public static Class<?> loadOrInstallClass(String canonical, String dependency, String repository) {
        try {
            return Class.forName(canonical);
        } catch (Throwable e) {
            return installDependency(dependency, repository).filter(i -> i.getCanonicalName().equals(canonical)).findFirst().orElseThrow();
        }
    }

    /**
     * Install a dependency onto the vm
     * @param dependency the dependency such as org:something:name:version
     * @param repository the repository such as https://repo1.maven.org/maven2
     * @return the stream of classes in the jar
     */
    public static Stream<Class<?>> installDependency(String dependency, String repository) {
        try {
            File file = downloadCachedFile(getJarDependencyUrl(dependency, repository), "jar").get();
            return load(file);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the dependency jar download
     * @param dependency the dependency
     * @param repository the repo
     * @return the string url
     */
    public static String getJarDependencyUrl(String dependency, String repository) {
        //org.zeroturnaround:zt-zip:1.15
        //https://repo1.maven.org/maven2/org/zeroturnaround/zt-zip/1.15/zt-zip-1.15.jar
        String[] c = dependency.split("\\Q:\\E");
        return repository + "/" + c[0].replaceAll("\\Q.\\E", "/") + "/" + c[1] + "/" + c[2] + "/" + c[1] + "-" + c[2] + ".jar";
    }

    /**
     * Download a file if it isnt in the temp cache
     * @param url the url
     * @param ext the extension of the file
     * @return the file future
     */
    public static Future<File> downloadCachedFile(String url, String ext) {
        File f = temp("dlc", UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)) + "." + ext);

        if (f.exists()) {
            return CompletableFuture.completedFuture(f);
        }

        return downloadFile(url, f);
    }

    /**
     * Download a file
     * @param url the url
     * @param file the file
     * @return the file future
     */
    public static Future<File> downloadFile(String url, File file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                FileChannel fileChannel = fileOutputStream.getChannel();
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                fileOutputStream.close();
                return file;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Compile source code into a class file and load it into the vm
     * @param file the file
     * @return the class file
     * @throws Throwable programming is hard
     */
    public static Class<?> compile(JavaFile file) throws Throwable {
        return compile(file.packageName + "." + file.typeSpec.name, file.toString());
    }

    /**
     * Search all classes in the jar the provided basejar class resides in, decompile everything and search the source for a string
     * @param baseJar the class origin
     * @param sourceCodeSnippet the snippet to find
     * @return the stream of classes that match
     */
    public static Stream<Class<?>> containingSource(Class<?> baseJar, String sourceCodeSnippet) {
        return all(baseJar).map(i -> i.type())
                .filter(i -> decompile(i).contains(sourceCodeSnippet)).map(i -> (Class<?>) i);
    }

    /**
     * Compile source code into a class file and load it into the vm
     * @param canonicalName the canonical name
     * @param sourceCode the source code
     * @return the class
     * @throws Throwable programming is hard
     */
    public static Class<?> compile(String canonicalName, String sourceCode) throws Throwable {
        File sourceFolder = temp("compile", UUID.randomUUID().toString(), "src");
        File sourceFile = new File(sourceFolder, canonicalName.replaceAll("\\Q.\\E", "/") + ".java");
        sourceFile.getParentFile().mkdirs();
        Files.writeString(sourceFile.toPath(), sourceCode);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{sourceFolder.toURI().toURL()});
        return classLoader.loadClass(canonicalName);
    }

    /**
     * Get/load all classes in the jar/folder that the provided source class comes from
     *
     * @param sourceJarClass the source class as a reference to the specified jar
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> all(Class<?> sourceJarClass) {
        try {
            return new JarLoader(sourceJarClass).all().filter(Objects::nonNull).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shove all source properties that can be applied in the destination into the destination.
     *
     * @param source      the source object to copy
     * @param destination the destination object to apply fields to
     */
    public static void shove(Object source, Object destination) {
        implode(destination, explode(source));
    }

    /**
     * Shoves all fitting properties in the map into the object
     *
     * @param object the object
     * @param map    the map of data
     */
    public static void implode(Object object, Map<String, Object> map) {
        CursedComponent component = Curse.on(object);
        for (String i : map.keySet()) {
            component.set(i, map.get(i));
        }
    }

    /**
     * Extract top level fields into properties via map
     *
     * @param object the object to explode
     * @return the map of properties
     */
    public static Map<String, Object> explode(Object object) {
        Map<String, Object> map = new HashMap<>();

        Curse.on(object).instanceFields().forEach(i -> {
            Object o = i.get();

            if (o != null) {
                map.put(i.field().getName(), o);
            }
        });

        return map;
    }

    /**
     * Load a jar or folder of classes into a new classloader under the Curse classloader parent.
     *
     * @param jarOrFolder the jar or folder
     * @return a stream of all resolved & loadable classes
     */
    public static Stream<Class<?>> load(File jarOrFolder) {
        return load(Curse.class.getClassLoader(), jarOrFolder);
    }

    /**
     * Load a jar or folder of classes into a new classloader under the parent provided classloader
     *
     * @param parentClassLoader the parent classloader to use for the new classloader
     * @param jarOrFolder       the jar or folder
     * @return a stream of all resolved & loadable classes
     */
    public static Stream<Class<?>> load(ClassLoader parentClassLoader, File jarOrFolder) {
        try {
            ClassLoader loader = new URLClassLoader(
                    new URL[]{jarOrFolder.toURI().toURL()},
                    parentClassLoader
            );

            return new JarLoader(loader, jarOrFolder).all();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which match a predicate
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param c              the predicate to match
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> where(Class<?> sourceJarClass, Predicate<Class<?>> c) {
        try {
            return new JarLoader(sourceJarClass).all().map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes within a specific package (or subpackage)
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param c              the predicate to match any class
     * @param pkg            the root package to search
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> whereInPackage(Class<?> sourceJarClass, Predicate<Class<?>> c, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which implement or could be casted to a specific class/interface
     *
     * @param sourceJarClass   any class inside the jar you want to search
     * @param interfaceOrClass the class/interface the classes must implement or extend or be assignable to
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> implemented(Class<?> sourceJarClass, Class<?> interfaceOrClass) {
        try {
            return new JarLoader(sourceJarClass).all(interfaceOrClass).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which implement or could be casted to a specific class/interface only in a specific package
     *
     * @param sourceJarClass   any class inside the jar you want to search
     * @param interfaceOrClass the class/interface the classes must implement or extend or be assignable to
     * @param pkg              the root package to search
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> implementedInPackage(Class<?> sourceJarClass, Class<?> interfaceOrClass, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg, interfaceOrClass).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which have a specific annotation on their type
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param annotation     the annotation the classes must have
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> annotated(Class<?> sourceJarClass, Class<? extends Annotation> annotation) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> i.isAnnotationPresent(annotation)).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which have a specific annotation on their type only in a specific package
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param annotation     the annotation the classes must have
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> annotatedName(Class<?> sourceJarClass, String annotation) {
        try {
            return new JarLoader(sourceJarClass).all().filter(i -> {
                for (Annotation a : i.getDeclaredAnnotations()) {
                    if (a.annotationType().getSimpleName().equals(annotation)) {
                        return true;
                    }
                }

                for (Annotation a : i.getAnnotations()) {
                    if (a.annotationType().getSimpleName().equals(annotation)) {
                        return true;
                    }
                }

                return false;
            }).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all classes which have a specific annotation on their type only in a specific package
     *
     * @param sourceJarClass any class inside the jar you want to search
     * @param annotation     the annotation the classes must have
     * @param pkg            the root package to search
     * @return a stream of cursed components
     */
    public static Stream<CursedComponent> annotatedInPackage(Class<?> sourceJarClass, Class<? extends Annotation> annotation, String pkg) {
        try {
            return new JarLoader(sourceJarClass).inPackageNested(pkg).filter(i -> i.isAnnotationPresent(annotation)).map(Curse::on);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a cursed component on an actual instance to begin using reflection
     *
     * @param instance the instance to reflect on
     * @return a cursed component
     */
    public static CursedComponent on(Object instance) {
        return new CursedComponent(new CursedContext().instance(instance).type(instance.getClass()));
    }

    /**
     * Get a cursed component on a class to begin using reflection
     *
     * @param clazz the class to reflect on
     * @return a cursed component
     */
    public static CursedComponent on(Class<?> clazz) {
        return new CursedComponent(new CursedContext().type(clazz));
    }
}
