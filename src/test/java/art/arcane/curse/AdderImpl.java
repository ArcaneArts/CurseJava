package art.arcane.curse;

public class AdderImpl implements Adder{
    public int add(int a, int b) {
        return a+b;
    }

    @Override
    public int subtract(int a, int b) {
        return a-b;
    }

    @Override
    public void dummy() {

    }
}
