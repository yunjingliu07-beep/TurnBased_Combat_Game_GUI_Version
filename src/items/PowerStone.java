package items;

import control.BattleContext;
import domain.Player;

public class PowerStone implements Items{

    @Override
    public String getName(){
        return "Power Stone";
    }

    @Override
    public void apply(BattleContext ctx, Player p){
        System.out.println(p.getName() + " uses power stone!");
        System.out.println("Now Triggering special skill without cooldown!");
        p.useSpecialSkill(false); // Power stone can be used without cooldown
    }

}
