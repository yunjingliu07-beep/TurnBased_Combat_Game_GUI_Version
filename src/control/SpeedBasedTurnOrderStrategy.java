package control;

import domain.Combatant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SpeedBasedTurnOrderStrategy implements TurnOrderStrategy{

    @Override
    public List<Combatant> determineOrder(BattleContext ctx) {
        List<Combatant> order = new ArrayList<Combatant>();
        order.addAll(ctx.getAliveEnemies());
        order.add(ctx.getPlayer());

        // Sort combatants in reverse order based on spd
        order.sort(Comparator.comparingInt(Combatant::getSpd).reversed());
        return order;
    }
}
