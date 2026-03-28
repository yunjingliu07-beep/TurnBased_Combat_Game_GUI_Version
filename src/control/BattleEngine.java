package control;

import actions.*;
import domain.*;
import ui.GameUI;

import java.util.ArrayList;
import java.util.List;

public class BattleEngine {
    private final GameUI gameUI;

    public BattleEngine(GameUI gameUI) {
        this.gameUI = gameUI;
    }

    public BattleOutcome runGame(BattleContext ctx, Difficulty difficulty) {
        gameUI.showBattleStart(difficulty);
        Player player = ctx.getPlayer();

        while (player.isAlive()) {
            ctx.spawnBackupEnemies();

            //If player wins
            if (ctx.isWaveCleared() && ctx.getBackupEnemies().isEmpty()) {
                gameUI.showBattleResult(true);
                return BattleOutcome.WIN;
            }

            //Show enemies
            gameUI.showEnemies(ctx);

            //Determine turn order
            List<Combatant> order = new ArrayList<>();
            TurnOrderStrategy strategy = new SpeedBasedTurnOrderStrategy();
            order = strategy.determineOrder(ctx);

            //Start to loop through the order for combat
            for (Combatant combatant : order) {
                //Player's turn
                if (combatant instanceof Player) {
                    // If not stunned
                    if(combatant.canAct()) {
                        playersTurn((Player) combatant, ctx);
                    }
                    else {
                        System.out.println(combatant.getName() + " is stunned! Cannot act!");
                    }
                }
                //Enemy's turn
                else{
                    if(combatant.canAct()) {
                        enemyTurn((Enemy)  combatant, ctx);
                    }
                    else {
                        System.out.println(combatant.getName() + " is stunned! Cannot act!");
                    }
                }
                //Check if the player's dead after each combatant's turn
                if(!player.isAlive()){
                    break;
                }
            }
        }
        // Loop ends, player is dead
        gameUI.showBattleResult(false);
        return BattleOutcome.LOSE;
    }
    void playersTurn(Player player, BattleContext ctx) {
        int choice = gameUI.choosePlayerActions();
        switch (choice) {
            case 1: // 1 for basic attack
                int target = gameUI.chooseTarget(ctx);
                BasicAttackAction ATKaction = new BasicAttackAction(ctx.getAliveEnemies().get(target-1));
                ATKaction.execute(ctx, player);
                break;

            case 2: // 2 for defend
                DefendAction DEFaction = new DefendAction();
                DEFaction.execute(ctx, player);
                break;

            case 3: // 3 for useItem
                UseItems USEaction = new UseItems(gameUI);
                USEaction.execute(ctx, player);
                break;

            case 4: // 4 for special skill
                // Check player type to identify the skill to use
                if (player instanceof Warrior) {
                    target = gameUI.chooseTarget(ctx);
                    ShieldBash SBaction = new ShieldBash(ctx.getAliveEnemies().get(target-1), true);
                    SBaction.execute(ctx, player);
                }
                else if  (player instanceof Wizard) {
                    target = gameUI.chooseTarget(ctx);
                    ArcaneBlast ABaction = new ArcaneBlast(true);
                    ABaction.execute(ctx, player);
                }
            default:
                System.out.println("Invalid choice, turn skipped");
        }
        // Update effects after turn
        player.tickEffects();
        player.tickSpecialCooldown();
    }
    void enemyTurn(Enemy e, BattleContext ctx) {
        BasicAttackAction EBAaction = new BasicAttackAction(ctx.getPlayer());
        EBAaction.execute(ctx, e);
    }
}
