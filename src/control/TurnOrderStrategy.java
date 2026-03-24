package control;

import domain.Combatant;

import java.util.List;

public interface TurnOrderStrategy {
    List<Combatant> determineOrder(BattleContext ctx, List<Combatant> combatants);
}
