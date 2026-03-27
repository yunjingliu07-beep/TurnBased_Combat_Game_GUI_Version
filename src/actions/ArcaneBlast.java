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
    public void execute(BattleContext ctx, Combatant combatant){
        Player player = (Player) combatant;
        // player's special skill is still on CD & not using the skill via power stone
        if (consumeCooldown && !player.canUseSpecialSkill()) {
            System.out.println(player.getName() + "'s Shield Bash is on cooldown for "
                    + player.getSpecialCooldown() + " more turn(s).");
        }
        int kills = 0;
        List<Enemy> enemyAlive = ctx.getAliveEnemies();

        // Deals dmg to all enemies and check if they're dead
        for (Enemy e: enemyAlive){
            int dmg = Math.max(0, player.getAtk() - e.getDef());
            System.out.println(player.getName() + " uses arcane blast on enemy " + e.getName());
            System.out.println("The damage is " + dmg);
            e.takeDamage(dmg);
            if(!e.isAlive()) {
                kills++;
                System.out.println(player.getName() + " has defeated "+ e.getName());
                ctx.getAliveEnemies().remove(e);
            }
        }

        // Based on the number of enemy defeated, add arcane boost effect on wizard
        if (kills > 0){
            System.out.println(player.getName() + " gain " + kills*10 + " ATK until the end of the level!");
            for (int i = 0; i < kills; i++) {
                player.addStatusEffect(new ArcaneBoostEffect());
            }
        }

        player.useSpecialSkill(consumeCooldown);

    }

}
