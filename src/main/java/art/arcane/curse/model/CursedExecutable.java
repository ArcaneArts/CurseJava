package art.arcane.curse.model;

import java.lang.reflect.Executable;

public class CursedExecutable extends CursedMember {
    private final Executable executable;

    public CursedExecutable(CursedContext context, Executable executable) {
        super(context, executable);
        this.executable = executable;
    }
}
