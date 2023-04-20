package art.arcane.curse.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;

public class CursedExecutable extends CursedMember {
    private final Executable executable;

    public CursedExecutable(CursedContext context, Executable executable) {
        super(context, executable);
        this.executable = executable;
    }

    public <A extends Annotation> A annotated(Class<A> annotation) {
        return executable.getDeclaredAnnotation(annotation);
    }

    public boolean isAnnotated(Class<? extends Annotation> a) {
        return executable.isAnnotationPresent(a);
    }
}
