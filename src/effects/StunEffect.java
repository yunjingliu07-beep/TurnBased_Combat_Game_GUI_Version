package effects;

import domain.Combatant;

public class StunEffect implements StatusEffects{
    private int turns = 2;

    @Override
    public String getName(){
        return "Stunned!";
    }

    @Override
    public boolean blockAction(){
        return false;
    }

    @Override
    public int modifyAtk(){
        return 0; // Defend adds atk by 0
    }

    @Override
    public int modifyDef(){
        return 0; // Defend adds def by 10
    }

    @Override
    public int modifySpd(){
        return 0; // Defend adds spd by 0
    }

    @Override
    public void onTick(){
        turns--;
    }

    @Override
    public boolean isExpired(){
        return turns <= 0;
    }

}
