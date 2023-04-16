package art.arcane.curse.tweakers;

import art.arcane.curse.Curse;
import art.arcane.curse.util.poet.JavaFile;
import art.arcane.curse.util.poet.MethodSpec;
import art.arcane.curse.util.poet.ParameterSpec;
import art.arcane.curse.util.poet.TypeSpec;
import com.strobel.reflection.Type;
import com.strobel.reflection.emit.TypeBuilder;

import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TweakerClassGenerator {
    public static <T> T interfaceDelegate(Class<?> tweaker, Class<?> itf, T theInterface) throws Throwable {
        Class<?> on = theInterface.getClass();
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
                .filter(i -> !java.lang.reflect.Modifier.isStatic(i.getModifiers()))
                .map(i -> MethodSpec.methodBuilder(i.getName())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameters(Arrays.stream(i.getParameters()).map(j -> ParameterSpec.builder(j.getType(), j.getName()).build()).collect(Collectors.toList()))
                        .returns(i.getReturnType())
                        .addStatement("return delegate." + i.getName() + "(" + Arrays.stream(i.getParameters()).map(j -> j.getName()).collect(Collectors.joining(", ")) + ")")
                        .build()).forEach(tb::addMethod);
        JavaFile src = JavaFile.builder("gen.art.arcane.curse", tb.build()).build();
        System.out.println(src.toString());
        return Curse.on(Curse.compile(src))
                .constructor(itf, tweaker)
                .invoke(theInterface, Curse.on(tweaker).construct().instance());
    }
}
