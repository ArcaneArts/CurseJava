package art.arcane.curse.model;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class CursedMember {
    protected final CursedContext context;
    protected final Member member;

    public CursedMember(CursedContext context, Member member) {
        this.context = context;
        this.member = member;
    }

    public Member getMember()
    {
        return member;
    }

    public boolean isStatic() {
        return Modifier.isStatic(member.getModifiers());
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(member.getModifiers());
    }

    public boolean isPublic() {
        return Modifier.isPublic(member.getModifiers());
    }

    public boolean isProtected() {
        return Modifier.isProtected(member.getModifiers());
    }

    public boolean isFinal() {
        return Modifier.isFinal(member.getModifiers());
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(member.getModifiers());
    }

    public boolean isVolatile() {
        return Modifier.isVolatile(member.getModifiers());
    }

    public boolean isInterface() {
        return Modifier.isInterface(member.getModifiers());
    }

    public boolean isNative() {
        return Modifier.isNative(member.getModifiers());
    }

    public boolean isSynchronized() {
        return Modifier.isSynchronized(member.getModifiers());
    }

    public boolean isStrict() {
        return Modifier.isStrict(member.getModifiers());
    }

    public boolean isTransient() {
        return Modifier.isTransient(member.getModifiers());
    }

    protected Unsafe unsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
