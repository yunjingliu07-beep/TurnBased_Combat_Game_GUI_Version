package effects;

import domain.Combatant;

public interface StatusEffects {
    String getName();
    boolean blockAction();
    int modifyAtk();
    int modifyDef();
    int modifySpd();
    void onTick();
    boolean isExpired();
}
