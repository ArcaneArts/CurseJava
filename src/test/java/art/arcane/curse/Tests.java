package art.arcane.curse;

import static org.junit.jupiter.api.Assertions.*;

import art.arcane.curse.model.CursedField;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Tests
{
    @Test
    public void test()
    {
        ReflectionTester t = new ReflectionTester();
        CursedField field = Curse.on(t).field("a");

        System.out.println("Read: " + field.get());
        System.out.println("--------------------");
        field.set(true);
        t.print();
        System.out.println("Reflective Read: " + field.get());

        ReflectionTester tt = t;
        tt.print();

        new Thread(() -> {
            tt.print();
        }).start();
    }
}
