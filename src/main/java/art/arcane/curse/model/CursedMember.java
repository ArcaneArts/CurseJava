package art.arcane.curse.model;

import java.lang.reflect.Member;

public class CursedMember extends CursedComponent
{
    private final Member member;

    public CursedMember(CursedContext context, Member member) {
        super(context);
        this.member = member;
    }
}
