package actions;

import domain.Combatant;

public interface Actions {
    String actionName();
    void execute(BattleContext ctx, Combatant c);
}
