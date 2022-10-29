# Curse

"It's right there! I can literally see it, I can read it, why the hell can't I change it?"

"Why the fuck did they make this final?"

"What dickhead decided to make this an immutable singleton?"

![](https://img.shields.io/github/v/release/ArcaneArts/Curse?color=%236f24f0&display_name=tag&label=Curse&sort=semver&style=for-the-badge)

## Get It
 
```groovy
maven { url "https://arcanearts.jfrog.io/artifactory/archives" }
```

```gradle
dependencies {
    implementation 'art.arcane:Curse:<VERSION>'
}
```
## You can do really nasty things

In this example we loop through all bukkit plugins and find any class which has a method double sample() and the class contains ANY annotation with the exact name XReactSampler, but the annotation must contain the properties String suffix, String id, int interval.
```java
public void scanForExternalSamplers() {~~~~
    Stream<ExternalSampler> samplers = Stream.of();
    for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
        samplers = Stream.concat(samplers, Curse.annotatedName(plugin.getClass(), "XReactSampler")
            .filter(i -> i.optionalMethod("sample").isPresent())
            .map(i -> i.method("sample"))
            .filter(i -> ((Method)i.getMember()).getReturnType() == double.class)
            .filter(i -> i.isPublic() && !i.isStatic() && !i.isFinal() && !i.isAbstract() && !i.isSynchronized())
            .filter(i -> Arrays.stream(i.getMember().getDeclaringClass().getDeclaredAnnotations())
                .anyMatch(j -> j.annotationType().getSimpleName().equals("XReactSampler")
                    && Curse.on(j.annotationType()).optionalMethod("id").isPresent()
                    && ((Method)Curse.on(j.annotationType()).method("id").getMember()).getReturnType() == String.class
                    && Curse.on(j.annotationType()).optionalMethod("interval").isPresent()
                    && ((Method)Curse.on(j.annotationType()).method("interval").getMember()).getReturnType() == int.class
                    && Curse.on(j.annotationType()).optionalMethod("suffix").isPresent()
                    && ((Method)Curse.on(j.annotationType()).method("suffix").getMember()).getReturnType() == String.class
                )).map(i -> Curse.on(i.getMember().getDeclaringClass()).construct())
            .map(i -> new ExternalSampler(i, i.method("sample"),
                Curse.on(Arrays.stream(i.type().getDeclaredAnnotations()).where(j -> j.annotationType().getSimpleName().equals("XReactSampler")).findFirst().get()).method("id").invoke(),
                Curse.on(Arrays.stream(i.type().getDeclaredAnnotations()).where(j -> j.annotationType().getSimpleName().equals("XReactSampler")).findFirst().get()).method("interval").invoke(),
                Curse.on(Arrays.stream(i.type().getDeclaredAnnotations()).where(j -> j.annotationType().getSimpleName().equals("XReactSampler")).findFirst().get()).method("suffix").invoke())));
    }
}
```