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
    public boolean execute(BattleContext ctx, Combatant actor){
        Player player = (Player) actor;

        // player's special skill is still on CD & not using the skill via power stone
        if (consumeCooldown && !player.canUseSpecialSkill()) {
            String message = player.getName() + "'s Shield Bash is on cooldown for "
                    + player.getSpecialCooldown() + " more turn(s).";
            if (ctx.getGameUI() != null) {
                ctx.getGameUI().showMessage("[SYSTEM] " + message);
            }
            System.out.println(message);
            return false;
        }

        // Perform basic attack to the enemy
        BasicAttackAction BAaction = new BasicAttackAction(target);
        boolean defeated = BAaction.execute(ctx, player);

        if(!defeated){
            target.addStatusEffect(new StunEffect());
            System.out.println(target.getName() + " has been stunned for this turn and next turn");
            System.out.println("The current status of the target is:\n" + target.getCurrentAttribute());
            return false;
        }

        player.useSpecialSkill(consumeCooldown);
        return true;

    }

}
