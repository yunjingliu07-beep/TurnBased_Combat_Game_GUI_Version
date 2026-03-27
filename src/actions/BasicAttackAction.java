package actions;

import control.BattleContext;
import domain.Combatant;
import domain.Enemy;

public class BasicAttackAction implements Actions {

    private Combatant target;

    public BasicAttackAction(Combatant target) {
        this.target = target;
    }

    @Override
    public String actionName(){
        return "basic attack";
    }

    @Override
    public void execute(BattleContext ctx, Combatant actor){
        // Calculate dmg dealt.
        int dmg = Math.max(0, actor.getAtk() - target.getDef());

        // Special Case: If smoke bomb active
        if (actor instanceof Enemy && ctx.isSmokeBombActive()){
            dmg = 0;
        }

        // Report dmg info
        System.out.println(actor.getName() + " uses basic attack on " + target.getName() + " !");
        System.out.println("The damage is " + dmg);
        target.takeDamage(dmg);

        if (!target.isAlive()){
            System.out.println(actor.getName() + " has defeated "+ target.getName());
            if (target instanceof Enemy) {
                // Remove dead enemies from the list
                ctx.getAliveEnemies().remove(target);
            }
        }
        else{
            System.out.println("The current status of the target is:\n" + target.getCurrentAttribute());
        }

    }

}
