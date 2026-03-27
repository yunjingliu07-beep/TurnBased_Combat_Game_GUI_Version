package items;

import control.BattleContext;
import domain.Player;

public interface Items {
    String getName();
    void apply(BattleContext ctx, Player p);
}
