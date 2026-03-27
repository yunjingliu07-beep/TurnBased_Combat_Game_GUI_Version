package actions;

import control.BattleContext;
import domain.Combatant;

public interface Actions {
    String actionName();
    void execute(BattleContext ctx, Combatant c);
}
