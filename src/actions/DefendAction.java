package actions;

import control.BattleContext;
import domain.Combatant;
import effects.DefenseBuffEffect;

public class DefendAction implements Actions {

    @Override
    public String actionName() {
        return "Defend Action!";
    }

    @Override
    public void execute(BattleContext ctx, Combatant actor){
        actor.addStatusEffect(new DefenseBuffEffect());
        System.out.println(actor.getName() + " has used defend! Add 10 DEF for this turn and next turn!");
        System.out.println("The current status of the target is:\n" + actor.getCurrentAttribute());
    }
}
