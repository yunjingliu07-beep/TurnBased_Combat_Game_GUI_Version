package items;

import actions.ArcaneBlast;
import actions.ShieldBash;
import control.BattleContext;
import domain.Combatant;
import domain.Player;
import domain.Warrior;
import domain.Wizard;
import ui.GameUI;

public class PowerStone implements Items{

    int target;

    @Override
    public String getName(){
        return "Power Stone";
    }

    @Override
    public void apply(BattleContext ctx, Player p){
        System.out.println(p.getName() + " uses power stone!");
        System.out.println("Now Triggering special skill without cooldown!");
        GameUI gameUI = ctx.getGameUI();
        if (p instanceof Warrior) {
            target = gameUI.chooseTarget(ctx);
            ShieldBash SBaction = new ShieldBash(ctx.getAliveEnemies().get(target-1), true);
            SBaction.execute(ctx, p);
        }
        else if  (p instanceof Wizard) {
            ArcaneBlast ABaction = new ArcaneBlast(true);
            ABaction.execute(ctx, p);
        }
        p.useSpecialSkill(false); // Power stone can be used without cooldown
    }

}
