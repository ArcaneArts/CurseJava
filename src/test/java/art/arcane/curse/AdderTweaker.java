package art.arcane.curse;

import art.arcane.curse.tweakers.Delegate;
import art.arcane.curse.tweakers.Inject;

public class AdderTweaker {
    @Delegate
    private Adder delegate;

    @Inject
    public int add(int a, int b) {
        return a - b;
    }
}
