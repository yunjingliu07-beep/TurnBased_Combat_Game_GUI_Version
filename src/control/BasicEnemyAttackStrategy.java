package control;

import actions.BasicAttackAction;
import domain.Enemy;
import domain.Player;

public class BasicEnemyAttackStrategy implements EnemyActionStrategy {
    @Override
    public void chooseAction(BattleContext ctx, Enemy enemy){
        Player player = ctx.getPlayer();
        new BasicAttackAction(player).execute(ctx, enemy);
    }
}
