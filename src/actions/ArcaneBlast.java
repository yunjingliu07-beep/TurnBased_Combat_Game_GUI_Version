package actions;

import control.BattleContext;
import domain.Combatant;
import domain.Enemy;
import domain.Player;
import effects.ArcaneBoostEffect;

import java.util.List;

public class ArcaneBlast extends SpecialSkill{
    private boolean consumeCooldown;

    public String actionName(){
        return "Arcane Blast";
    }

    public ArcaneBlast(boolean consumeCoolDown) {
        this.consumeCooldown = consumeCoolDown;
    }
    @Override
    public boolean execute(BattleContext ctx, Combatant combatant){
        Player player = (Player) combatant;
        // player's special skill is still on CD & not using the skill via power stone
        if (consumeCooldown && !player.canUseSpecialSkill()) {
            String message = player.getName() + "'s Arcane Blast is on cooldown for "
                    + player.getSpecialCooldown() + " more turn(s).";
            if (ctx.getGameUI() != null) {
                ctx.getGameUI().showMessage("[SYSTEM] " + message);
            }
            System.out.println(message);
            return false;
        }
        int kills = 0;
        List<Enemy> enemyAlive = ctx.getAliveEnemies();

        // Deals basic attack to all enemies and check if they're dead
        for (int i = enemyAlive.size() - 1; i >= 0; i--) {
            BasicAttackAction BAaction = new BasicAttackAction(enemyAlive.get(i));
            boolean defeated = BAaction.execute(ctx, player);
            //If defeat an enemy, kills +=1
            if (defeated){
                kills++;
            }
        }

        // Based on the number of enemy defeated, add arcane boost effect on wizard
        if (kills > 0){
            System.out.println(player.getName() + " gain " + kills*10 + " ATK until the end of the level!");
            for (int i = 0; i < kills; i++) {
                player.addStatusEffect(new ArcaneBoostEffect());
            }
            return true; // Make kills
        }

        player.useSpecialSkill(consumeCooldown);
        return false; // Make no kills

    }

}
