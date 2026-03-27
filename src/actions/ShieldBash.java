package actions;

import control.BattleContext;
import domain.Combatant;
import domain.Enemy;
import domain.Player;
import effects.StunEffect;

public class ShieldBash extends SpecialSkill{
    private Combatant target;
    private boolean consumeCooldown;

    public ShieldBash(Combatant target, boolean consumeCooldown){
        this.target = target;
        this.consumeCooldown = consumeCooldown;
    }

    @Override
    public String actionName(){
        return "Shield Bash!";
    }

    @Override
    public void execute(BattleContext ctx, Combatant actor){
        Player player = (Player) actor;

        // player's special skill is still on CD & not using the skill via power stone
        if (consumeCooldown && !player.canUseSpecialSkill()) {
            System.out.println(player.getName() + "'s Shield Bash is on cooldown for "
                    + player.getSpecialCooldown() + " more turn(s).");
        }

        int dmg = Math.max(0, player.getAtk() - target.getDef());
        System.out.println(actor.getName() + " uses Shield Bash on " + target.getName() + "!");
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
            target.addStatusEffect(new StunEffect());
            System.out.println(target.getName() + " has been stunned for this turn and next turn");
            System.out.println("The current status of the target is:\n" + target.getCurrentAttribute());
        }

        player.useSpecialSkill(consumeCooldown);

    }

}
