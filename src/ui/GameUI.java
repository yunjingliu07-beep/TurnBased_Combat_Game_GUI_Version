package ui;

import control.BattleContext;
import control.Difficulty;
import domain.Combatant;

import java.util.List;

public interface GameUI {
    // Methods at game initialization
    int choosePlayer();
    int[] chooseInitialItems(int inventorySize);
    void showDifficulty();
    void listEnemies();
    Difficulty selectDifficulty();

    //Methods during the game
    void showBattleStart(Difficulty difficulty);
    int choosePlayerActions();
    void showEnemies(BattleContext ctx);
    int chooseTarget(BattleContext ctx);
    int chooseItem(BattleContext ctx);
    void showMessage(String message);

    //Methods after the game ends
    void showBattleResult(boolean victory, BattleContext ctx);
    int restartOption();
}
