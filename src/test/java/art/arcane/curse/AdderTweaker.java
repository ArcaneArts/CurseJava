package art.arcane.curse;

import art.arcane.curse.tweakers.Inject;

public class AdderTweaker
{
    @Inject.Replace
    public int add(int a, int b)
    {
        return a - b;
    }
}
