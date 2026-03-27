package items;

import control.BattleContext;
import domain.Player;

public class PotionItem implements Items{

    @Override
    public String getName(){
        return "Heal Potion";
    }

    @Override
    public void apply(BattleContext ctx, Player p){
        System.out.println(p.getName() + " uses " + this.getName() + ", heal by 100!");
        p.heal(100);
    }
}
