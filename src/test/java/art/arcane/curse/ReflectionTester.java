package art.arcane.curse;

public class ReflectionTester {
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

    public void setThing(boolean b) {
        this.b = b;
    }

    public boolean getThing() {
        return b;
    }

    public boolean getB() {
        return b;
    }

    public static String repeat(String t, int c) {
        StringBuilder s = new StringBuilder();

        for(int i = 0; i < c; i++) {
            s.append(t);
        }

        return s.toString();
    }

    public static boolean getStaticA() {
        return A;
    }

    public static boolean getStaticB() {
        return B;
    }
}
