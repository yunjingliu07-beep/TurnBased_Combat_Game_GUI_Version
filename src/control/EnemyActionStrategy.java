package control;

import actions.Actions;
import domain.Combatant;
import domain.Enemy;

import java.util.List;

public interface EnemyActionStrategy {
    Actions chooseAction(BattleContext ctx, Enemy enemy);
    Combatant chooseTarget(BattleContext ctx, Enemy enemy);

}
