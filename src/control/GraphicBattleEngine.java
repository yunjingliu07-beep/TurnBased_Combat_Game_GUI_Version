package control;

import actions.ArcaneBlast;
import actions.BasicAttackAction;
import actions.DefendAction;
import actions.ShieldBash;
import domain.Combatant;
import domain.Enemy;
import domain.Player;
import domain.Warrior;
import domain.Wizard;
import items.Items;
import ui.GraphicUI;

import javax.swing.Timer;
import java.util.List;

/**
 * BattleEngine (GUI version)
 *
 * This version abandons console blocking flow.
 *
 * OLD console flow:
 * - engine asks UI for action
 * - engine waits for user input
 *
 * NEW GUI flow:
 * - engine starts battle
 * - when it is player's turn, it enables action buttons and waits
 * - button clicks call engine methods:
 *   - playerBasicAttack(...)
 *   - playerDefend()
 *   - playerUseItem(...)
 *   - playerSpecial(...)
 *
 * This is event-driven and suitable for Swing.
 */
public class GraphicBattleEngine {
    private final GraphicUI gameUI;

    private BattleContext ctx;
    private Difficulty difficulty;

    private List<Combatant> turnOrder;
    private int turnIndex;

    private boolean battleEnded = false;
    private static int currentTurn;
    private static final int ACTION_DELAY_MS = 1000;

    public GraphicBattleEngine(GraphicUI gameUI) {
        this.gameUI = gameUI;
    }

    public static int getCurrentTurn() {
        return currentTurn;
    }

    public BattleContext getContext() {
        return ctx;
    }

    public boolean isBattleEnded() {
        return battleEnded;
    }

    // ============================================================
    // Start battle
    // ============================================================
    public void startBattle(BattleContext ctx, Difficulty difficulty) {
        this.ctx = ctx;
        this.difficulty = difficulty;
        this.battleEnded = false;
        currentTurn = 1;

        gameUI.showBattleStart(difficulty);
        gameUI.refreshBattleScreen();

        beginTurnCycle();
    }

    /**
     * Each turn cycle:
     * - spawn backup enemies if current wave cleared
     * - determine turn order
     * - process combatants one by one
     */
    private void beginTurnCycle() {
        if (battleEnded) return;

        // If player already dead
        if (!ctx.getPlayer().isAlive()) {
            endBattle(false);
            return;
        }

        // Spawn backup enemies if needed
        boolean hadNoEnemies = ctx.getAliveEnemies().isEmpty();
        boolean hadBackups = !ctx.getBackupEnemies().isEmpty();
        ctx.spawnBackupEnemies();

        if (hadNoEnemies && hadBackups && !ctx.getAliveEnemies().isEmpty()) {
            gameUI.appendLog("[SYSTEM] Backup enemies appear!");
        }

        // If still no enemies and no backups => victory
        if (ctx.isWaveCleared() && ctx.getBackupEnemies().isEmpty()) {
            endBattle(true);
            return;
        }

        TurnOrderStrategy strategy = new SpeedBasedTurnOrderStrategy();
        turnOrder = strategy.determineOrder(ctx);
        turnIndex = 0;

        gameUI.appendLog("========== TURN " + currentTurn + " ==========");
        gameUI.refreshBattleScreen();

        processNextCombatant();
    }

    /**
     * Process the next combatant in the turn order.
     */
    private void processNextCombatant() {
        if (battleEnded) return;

        Player player = ctx.getPlayer();

        // Player died?
        if (!player.isAlive()) {
            endBattle(false);
            return;
        }

        // All enemies gone?
        if (ctx.isWaveCleared() && ctx.getBackupEnemies().isEmpty()) {
            endBattle(true);
            return;
        }

        // End of this round => next turn
        if (turnIndex >= turnOrder.size()) {
            ctx.endTurn();
            currentTurn++;
            beginTurnCycle();
            return;
        }

        Combatant combatant = turnOrder.get(turnIndex);

        // Skip dead combatants
        if (!combatant.isAlive()) {
            turnIndex++;
            processNextCombatant();
            return;
        }

        if (combatant instanceof Player) {
            handlePlayerTurn((Player) combatant);
        } else {
            handleEnemyTurn((Enemy) combatant);
        }
    }

    // ============================================================
    // Player turn
    // ============================================================
    private void handlePlayerTurn(Player player) {
        if (!player.canAct()) {
            gameUI.appendLog(player.getName() + " is stunned! Turn skipped!");
            player.tickEffects();
            player.removeExpiredEffect();
            player.tickSpecialCooldown();

            gameUI.refreshBattleScreen();

            turnIndex++;
            processNextCombatant();
            return;
        }

        gameUI.appendLog("[PLAYER TURN] Choose your action.");
        gameUI.enablePlayerActions(true);
        gameUI.refreshBattleScreen();
        // STOP here and wait for button click
    }

    // ============================================================
    // Enemy turn
    // ============================================================
    private void handleEnemyTurn(Enemy enemy) {
        if (!enemy.canAct()) {
            gameUI.appendLog(enemy.getName() + " is stunned! Turn skipped!");
            enemy.tickEffects();
            enemy.removeExpiredEffect();

            gameUI.refreshBattleScreen();

            turnIndex++;
            processNextCombatant();
            return;
        }

        Player player = ctx.getPlayer();

        int damage = Math.max(0, enemy.getAtk() - player.getDef());
        if (ctx.isSmokeBombActive()) {
            damage = 0;
        }

        gameUI.appendLog("[ENEMY TURN] " + enemy.getName() + " attacks " + player.getName() + "!");
        gameUI.appendLog("Damage dealt: " + damage);
        runWithDelay(() -> {
            if (battleEnded) return;
            BasicAttackAction attack = new BasicAttackAction(player);
            attack.execute(ctx, enemy);

            enemy.tickEffects();
            enemy.removeExpiredEffect();

            gameUI.refreshBattleScreen();

            if (!player.isAlive()) {
                endBattle(false);
                return;
            }

            turnIndex++;
            processNextCombatant();
        });
    }

    // ============================================================
    // Methods called by GUI buttons
    // ============================================================

    /**
     * Player uses a normal basic attack on a selected enemy.
     */
    public void playerBasicAttack(int targetIndex) {
        if (battleEnded) return;

        List<Enemy> enemies = ctx.getAliveEnemies();
        if (targetIndex < 0 || targetIndex >= enemies.size()) {
            gameUI.appendLog("[SYSTEM] Invalid target.");
            return;
        }

        gameUI.enablePlayerActions(false);
        Player player = ctx.getPlayer();
        Enemy target = enemies.get(targetIndex);

        int damage = Math.max(0, player.getAtk() - target.getDef());
        gameUI.appendLog(player.getName() + " uses Basic Attack on " + target.getName() + "!");
        gameUI.appendLog("Damage dealt: " + damage);

        runWithDelay(() -> {
            if (battleEnded) return;
            BasicAttackAction action = new BasicAttackAction(target);
            boolean defeated = action.execute(ctx, player);

            if (defeated) {
                gameUI.appendLog(target.getName() + " was defeated!");
            }

            finishPlayerAction(player);
        });
    }

    /**
     * Player uses defend.
     */
    public void playerDefend() {
        if (battleEnded) return;

        Player player = ctx.getPlayer();

        gameUI.appendLog(player.getName() + " uses DEFEND!");
        gameUI.appendLog("DEF +10 for this turn and next turn.");

        DefendAction action = new DefendAction();
        action.execute(ctx, player);

        finishPlayerAction(player);
    }

    /**
     * Generic item use.
     *
     * NOTE:
     * - We bypass the old console UseItems class.
     * - We manually handle Power Stone here so GUI can stay event-driven.
     */
    public void playerUseItem(int itemIndex) {
        if (battleEnded) return;

        Player player = ctx.getPlayer();

        if (itemIndex < 0 || itemIndex >= player.getInventory().getSize()) {
            gameUI.appendLog("[SYSTEM] Invalid item choice.");
            return;
        }

        Items item = player.getInventory().getItems().get(itemIndex);
        String itemName = item.getName();

        // Power Stone special handling
        if ("Power Stone".equals(itemName)) {
            if (player instanceof Warrior) {
                // Warrior Power Stone needs target; GUI should call playerUsePowerStoneOnTarget instead
                gameUI.appendLog("[SYSTEM] Please select a target for Power Stone Shield Bash.");
                return;
            } else if (player instanceof Wizard) {
                // Remove item first
                player.getInventory().getItems().remove(itemIndex);

                gameUI.appendLog(player.getName() + " uses Power Stone!");
                gameUI.appendLog("Special skill triggered without cooldown: Arcane Blast!");

                gameUI.enablePlayerActions(false);
                runWithDelay(() -> {
                    if (battleEnded) return;
                    ArcaneBlast blast = new ArcaneBlast(false); // no cooldown consumption
                    blast.execute(ctx, player);
                    finishPlayerAction(player);
                });
                return;
            }
        }

        // Normal items (Potion / Smoke Bomb etc.)
        gameUI.appendLog(player.getName() + " uses " + itemName + "!");
        player.getInventory().useItem(itemIndex, ctx, player);

        // Optional extra user-friendly log
        if ("Heal Potion".equals(itemName)) {
            gameUI.appendLog("Recovered 100 HP.");
        } else if ("Smoke Bomb".equals(itemName)) {
            gameUI.appendLog("Enemy attacks deal 0 damage for the next 2 turns!");
        }

        finishPlayerAction(player);
    }

    /**
     * For Warrior using Power Stone -> Shield Bash without cooldown.
     * GUI first asks user to click an enemy, then calls this.
     */
    public void playerUsePowerStoneOnTarget(int targetIndex) {
        if (battleEnded) return;

        Player player = ctx.getPlayer();

        if (!(player instanceof Warrior)) {
            gameUI.appendLog("[SYSTEM] This action is only for Warrior Power Stone.");
            return;
        }

        List<Enemy> enemies = ctx.getAliveEnemies();
        if (targetIndex < 0 || targetIndex >= enemies.size()) {
            gameUI.appendLog("[SYSTEM] Invalid target.");
            return;
        }

        // Find and remove one Power Stone
        int powerStoneIndex = -1;
        for (int i = 0; i < player.getInventory().getItems().size(); i++) {
            if ("Power Stone".equals(player.getInventory().getItems().get(i).getName())) {
                powerStoneIndex = i;
                break;
            }
        }

        if (powerStoneIndex == -1) {
            gameUI.appendLog("[SYSTEM] No Power Stone found in inventory.");
            return;
        }

        player.getInventory().getItems().remove(powerStoneIndex);

        Enemy target = enemies.get(targetIndex);

        gameUI.appendLog(player.getName() + " uses Power Stone!");
        gameUI.appendLog("Special skill triggered without cooldown: Shield Bash on " + target.getName() + "!");

        gameUI.enablePlayerActions(false);
        runWithDelay(() -> {
            if (battleEnded) return;
            // false = do NOT consume cooldown
            ShieldBash shieldBash = new ShieldBash(target, false);
            boolean defeated = shieldBash.execute(ctx, player);

            if (defeated) {
                gameUI.appendLog(target.getName() + " was defeated!");
            } else {
                gameUI.appendLog(target.getName() + " is stunned!");
            }

            finishPlayerAction(player);
        });
    }

    /**
     * Player uses class special.
     * - Warrior: target required
     * - Wizard: no target required
     */
    public void playerSpecial(int targetIndex) {
        if (battleEnded) return;

        Player player = ctx.getPlayer();

        if (!player.canUseSpecialSkill()) {
            gameUI.appendLog("[SYSTEM] Special skill is on cooldown for " + player.getSpecialCooldown() + " turn(s).");
            return;
        }

        if (player instanceof Warrior) {
            List<Enemy> enemies = ctx.getAliveEnemies();
            if (targetIndex < 0 || targetIndex >= enemies.size()) {
                gameUI.appendLog("[SYSTEM] Invalid target.");
                return;
            }

            Enemy target = enemies.get(targetIndex);
            int damage = Math.max(0, player.getAtk() - target.getDef());

            gameUI.appendLog(player.getName() + " uses SHIELD BASH on " + target.getName() + "!");
            gameUI.appendLog("Damage dealt: " + damage);

            gameUI.enablePlayerActions(false);
            runWithDelay(() -> {
                if (battleEnded) return;
                // true = consume cooldown normally
                ShieldBash action = new ShieldBash(target, true);
                boolean defeated = action.execute(ctx, player);

                if (defeated) {
                    gameUI.appendLog(target.getName() + " was defeated!");
                } else {
                    gameUI.appendLog(target.getName() + " is stunned!");
                }

                finishPlayerAction(player);
            });
            return;

        } else if (player instanceof Wizard) {
            gameUI.appendLog(player.getName() + " uses ARCANE BLAST!");
            gameUI.appendLog("Attacks all enemies!");

            gameUI.enablePlayerActions(false);
            runWithDelay(() -> {
                if (battleEnded) return;
                ArcaneBlast action = new ArcaneBlast(true); // consume cooldown normally
                action.execute(ctx, player);
                finishPlayerAction(player);
            });
            return;
        }

        finishPlayerAction(player);
    }

    private void runWithDelay(Runnable action) {
        Timer timer = new Timer(ACTION_DELAY_MS, e -> action.run());
        timer.setRepeats(false);
        timer.start();
    }

    // ============================================================
    // End of player action
    // ============================================================
    private void finishPlayerAction(Player player) {
        // Update player effects
        player.tickEffects();
        player.removeExpiredEffect();
        player.tickSpecialCooldown();

        gameUI.enablePlayerActions(false);
        gameUI.refreshBattleScreen();

        // Check win immediately after player acts
        if (ctx.isWaveCleared() && ctx.getBackupEnemies().isEmpty()) {
            endBattle(true);
            return;
        }

        // Move to next combatant
        turnIndex++;
        processNextCombatant();
    }

    // ============================================================
    // End battle
    // ============================================================
    private void endBattle(boolean victory) {
        if (battleEnded) return;

        battleEnded = true;
        gameUI.enablePlayerActions(false);
        gameUI.refreshBattleScreen();
        gameUI.showBattleResult(victory, ctx);
    }
}
