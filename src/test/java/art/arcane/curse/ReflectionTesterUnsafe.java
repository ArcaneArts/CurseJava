package art.arcane.curse;

public class ReflectionTesterUnsafe {
    private final boolean a = false;
    private boolean b = false;
    private static final boolean A = false;
    private static boolean B = false;

    public void print() {
        System.out.println("Nonreflect READ: " + a);
    }

    public boolean getA() {
        return a;
    }

    public boolean getB() {
        return b;
    }

    public static boolean getStaticA() {
        return A;
    }

    public static boolean getStaticB() {
        return B;
    }
}
