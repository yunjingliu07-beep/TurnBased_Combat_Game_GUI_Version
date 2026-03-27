package ui;

import control.Difficulty;
import domain.Combatant;

import java.util.List;

public interface GameUI {
    int choosePlayer();
    int choosePlayerActions();
    int chooseTarget();
    int[] chooseInitialItems();
    void showEnemies();
    void showBattleResult(boolean victory);
    void showBattleStart(Difficulty difficulty);
}
