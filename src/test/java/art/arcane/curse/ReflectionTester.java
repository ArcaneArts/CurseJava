package art.arcane.curse;

import java.util.concurrent.atomic.AtomicReference;

public class ReflectionTester {
    private final boolean a = false;

    public void print() {
        System.out.println("Nonreflect READ: " + a);
    }
}
