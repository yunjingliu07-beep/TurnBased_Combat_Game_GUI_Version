package effects;

import domain.Combatant;

public class ArcaneBoostEffect implements StatusEffects{
    private int turns = 1; // Arcane Boost last for unlimited turns

    @Override
    public String getName(){
        return "Arcane Boost Effect";
    }

    @Override
    public boolean blockAction(){
        return false; // Arcane boost do not block actions
    }

    @Override
    public int modifyAtk(){
        return 10; // Arcane boost modify atk by 10
    }

    @Override
    public int modifyDef(){
        return 0; // Arcane boost do not modify def
    }

    @Override
    public int modifySpd(){
        return 0; // Arcane boost do not modify spd
    }

    @Override
    public void onTick(){
        turns -= 0;
    }

    @Override
    public boolean isExpired(){
        return false; // It's never expired
    }
}
