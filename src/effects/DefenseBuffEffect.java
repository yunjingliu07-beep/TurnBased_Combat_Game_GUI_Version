package effects;

import domain.Combatant;

public class DefenseBuffEffect implements StatusEffects {
    private int turns = 2; // Defense Buff lasts for 2 turns

    @Override
    public String getName() {
        return "Defense Buff";
    }

    @Override
    public boolean blockAction(){
        return false; // Defend does not block enemy actions
    }

    @Override
    public int modifyAtk(){
        return 0; // Defend adds atk by 0
    }

    @Override
    public int modifyDef(){
        return 10; // Defend adds def by 10
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
