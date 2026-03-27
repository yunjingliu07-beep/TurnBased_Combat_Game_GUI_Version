package control;

import actions.Actions;
import domain.Combatant;
import domain.Enemy;
import domain.Player;

import java.util.List;

public interface EnemyActionStrategy {
    void chooseAction(BattleContext ctx, Enemy enemy);
}
