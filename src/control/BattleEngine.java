package control;

import actions.*;
import domain.*;
import ui.GameUI;

import java.util.ArrayList;
import java.util.List;

public class BattleEngine {
    private final GameUI gameUI;
    private static int currentTurn;

    public BattleEngine(GameUI gameUI) {
        this.gameUI = gameUI;
    }

    public static int getCurrentTurn() {
        return currentTurn;
    }

    public BattleOutcome runGame(BattleContext ctx, Difficulty difficulty) {
        gameUI.showBattleStart(difficulty);
        Player player = ctx.getPlayer();
        currentTurn = 0;

        while (player.isAlive()) {
            ctx.spawnBackupEnemies();
            System.out.println("It's now turn " + currentTurn + "!");
            System.out.println("Current enemies remaining!");


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
                        System.out.println(combatant.getName() + " is stunned! Cannot act! Turn skipped!\n");
                    }
                    //Player action ends, update effects
                    player.tickEffects();
                    player.tickSpecialCooldown();
                }
                //Enemy's turn
                else{
                    if(combatant.canAct()) {
                        enemyTurn((Enemy)  combatant, ctx);
                        //Enemy turn ends, update effects
                        combatant.tickEffects();
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
            // Turn ends, update effects and check if player wins
            if (ctx.isWaveCleared() && ctx.getBackupEnemies().isEmpty()) {
                gameUI.showBattleResult(true, ctx);
                return BattleOutcome.WIN;
            }
            ctx.endTurn();
            currentTurn++;
        }
        // Loop ends, player is dead
        gameUI.showBattleResult(false, ctx);
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
                    // ArcaneBlast attacks all enemies, no need for asking target
                    ArcaneBlast ABaction = new ArcaneBlast(true);
                    ABaction.execute(ctx, player);
                }
        }

    }
    void enemyTurn(Enemy e, BattleContext ctx) {
        BasicAttackAction EBAaction = new BasicAttackAction(ctx.getPlayer());
        EBAaction.execute(ctx, e);
    }
}
