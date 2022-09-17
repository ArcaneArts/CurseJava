package art.arcane.curse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {
    @Test
    public void testInstancedGet() {
        ReflectionTester t = new ReflectionTester();
        assertEquals(t.getA(), Curse.on(t).field("a").get());
    }

    @Test
    public void testBreakSingleton() {
        ImmutableSingletonClass second = Curse.on(ImmutableSingletonClass.class).make().set("key", "NOT THE ONLY KEY").instance();
        Curse.on(ImmutableSingletonClass.class).set("INSTANCE", second);
        assertEquals("NOT THE ONLY KEY", ((ImmutableSingletonClass) Curse.on(ImmutableSingletonClass.class).get("INSTANCE")).getKey());
    }

    @Test
    public void testDefine() {
        ReflectionTester t = Curse.on(ReflectionTester.class)
            .construct()
            .set("a", true)
            .set("b", true)
            .instance();

        assertTrue((boolean) Curse.on(t).get("a"));
        assertTrue((boolean) Curse.on(t).get("b"));
    }

    @Test
    public void testDefineWithoutConstructor() {
        ReflectionTester t = Curse.on(ReflectionTester.class)
            .make()
            .set("a", true)
            .set("b", true)
            .instance();

        assertTrue((boolean) Curse.on(t).get("a"));
        assertTrue((boolean) Curse.on(t).get("b"));
    }

    @Test
    public void testStaticGet() {
        assertEquals(ReflectionTester.getStaticA(), Curse.on(ReflectionTester.class).field("A").get());
    }

    @Test
    public void testInstancedGetterGet() {
        ReflectionTester t = new ReflectionTester();
        assertEquals(t.getB(), Curse.on(t).get("thing"));
    }

    @Test
    public void testInstancedSetterSet() {
        ReflectionTester t = new ReflectionTester();
        Curse.on(t).set("thing", true);
        assertTrue(t.getB());
    }

    @Test
    public void testInstancedNonFinalSet() {
        ReflectionTester t = new ReflectionTester();
        Curse.on(t).field("b").set(true);
        assertTrue(t.getB());
    }

    @Test
    public void testStaticNonFinalSet() {
        Curse.on(ReflectionTester.class).field("B").set(true);
        assertTrue(ReflectionTester.getStaticB());
    }

    @Test
    public void testInstancedFinalSet() {
        ReflectionTesterUnsafe t = new ReflectionTesterUnsafe();
        Curse.on(t).field("A").set(true);
        assertTrue((boolean) Curse.on(t).field("A").get());
    }

    @Test
    public void testStaticFinalSet() {
        Curse.on(ReflectionTesterUnsafe.class).field("A").set(true);
        assertTrue((boolean) Curse.on(ReflectionTesterUnsafe.class).field("A").get());
    }
}
