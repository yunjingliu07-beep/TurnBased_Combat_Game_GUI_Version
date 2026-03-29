package actions;

import control.BattleContext;
import domain.Combatant;

public interface Actions {
    String actionName();
    boolean execute(BattleContext ctx, Combatant c);
}
