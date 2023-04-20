package art.arcane.curse.tweakers;

import art.arcane.curse.Curse;
import art.arcane.curse.model.CursedComponent;
import art.arcane.curse.util.poet.JavaFile;
import art.arcane.curse.util.poet.MethodSpec;
import art.arcane.curse.util.poet.ParameterSpec;
import art.arcane.curse.util.poet.TypeSpec;
import com.strobel.assembler.metadata.signatures.ReturnType;
import com.strobel.reflection.Type;
import com.strobel.reflection.emit.TypeBuilder;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TweakerClassGenerator {
    public static <T> T interfaceDelegate(Class<?> tweaker, Class<?> itf, T theInterface) throws Throwable {
        Class<?> on = theInterface.getClass();
        CursedComponent t = Curse.on(tweaker).construct();
        t.fields().filter(i -> i.type().equals(itf) && i.isAnnotated(Delegate.class)).forEach(i -> i.set(theInterface));
        TypeSpec.Builder tb = TypeSpec.classBuilder("T" + tweaker.getSimpleName() + "X" + on.getSimpleName())
                .addSuperinterface(itf)
                .addField(itf, "delegate", Modifier.PRIVATE, Modifier.FINAL)
                .addField(tweaker, "tweaker", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(itf, "delegate")
                        .addParameter(tweaker, "tweaker")
                        .addStatement("this.delegate = delegate")
                        .addStatement("this.tweaker = tweaker")
                        .build());
        Curse.on(theInterface).declaredMethods()
                .filter(i -> !java.lang.reflect.Modifier.isStatic(i.method().getModifiers()))
                .map(i -> {
                    String rt = i.method().getReturnType().equals(Void.TYPE) ? "" : "return ";
                    String src = "delegate";

                    if(t.optionalMethod(i.method().getName(), i.method().getParameterTypes()).map(j -> j.isAnnotated(Inject.class)).orElse(false)) {
                        src = "tweaker";
                    }

                    MethodSpec.Builder m = MethodSpec.methodBuilder(i.method().getName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameters(Arrays.stream(i.method().getParameters()).map(j -> ParameterSpec.builder(j.getType(), j.getName()).build())
                            .collect(Collectors.toList()))
                        .addStatement(rt + src + "." + i.method().getName() + "(" + Arrays.stream(i.method().getParameters())
                            .map(Parameter::getName).collect(Collectors.joining(", ")) + ")");

                    if(!i.method().getReturnType().equals(Void.TYPE)) {
                        m = m.returns(i.method().getReturnType());
                    }

                    return m.build();
                }).forEach(tb::addMethod);
        JavaFile src = JavaFile.builder("gen.art.arcane.curse", tb.build()).build();
        System.out.println(src.toString());
        return Curse.on(Curse.compile(src))
                .constructor(itf, tweaker)
                .invoke(theInterface, t.instance());
    }
}
