import control.*;
import domain.*;
import items.PotionItem;
import items.PowerStone;
import items.SmokeBomb;
import ui.ConsoleUI;
import ui.GameUI;
import ui.GraphicUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public  class GameControl {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GraphicUI();
        });
    }

    // Create a GameSettings file to store the current settings of the game
    public static GameSettings createNewSettings(GameUI ui) {
        // Choose Player
        int playerChoice = ui.choosePlayer();

        // Choose initial items
        int[] itemChoice = ui.chooseInitialItems(2);

        // Show enemy info and difficulty options
        ui.listEnemies();
        ui.showDifficulty();

        // Choose difficulty
        Difficulty difficulty = ui.selectDifficulty();

        //Save the settings into a GameSettings class
        return new  GameSettings(playerChoice, itemChoice, difficulty);
    }
    // Create a BattleContext based on the settings
    public static BattleContext createBattleContext(GameSettings gameSettings, GameUI ui) {
        //Create player
        Player player = null;
        switch(gameSettings.getPlayerChoice()) {
            case 1:
                player = new Warrior();
                break;
            case 2:
                player = new Wizard();
                break;
        }

        //Add selected items to player inventory
        int[] itemChoice = gameSettings.getInitialItemChoice();
        for (int i = 0; i < itemChoice.length; i++) {
            switch (itemChoice[i]) {
                case 1:
                    player.getInventory().addItem(new PotionItem());
                    break;
                case 2:
                    player.getInventory().addItem(new PowerStone());
                    break;
                case 3:
                    player.getInventory().addItem(new SmokeBomb());
                    break;
            }
        }

        //Create list of enemies based on choice of difficulty
        List<Enemy> enemyList = new ArrayList<>();
        List<Enemy> backupEnemyList = new ArrayList<>();
        switch (gameSettings.getDifficulty()) {
            case EASY:
                // Add 3 goblins
                enemyList.add(new Goblin());
                enemyList.add(new Goblin());
                enemyList.add(new Goblin());
                break;

            case MEDIUM:
                // Add 1 goblin 1 wolf to initial spawn, add 2 wolves to backup spawn
                enemyList.add(new Goblin());
                enemyList.add(new Wolf());
                backupEnemyList.add(new Wolf());
                backupEnemyList.add(new Wolf());
                break;

            case HARD:
                // Add 2 goblins to initial spawn, add 1 goblin and 2 wolves to backup spawn
                enemyList.add(new Goblin());
                enemyList.add(new Goblin());
                backupEnemyList.add(new Goblin());
                backupEnemyList.add(new Wolf());
                backupEnemyList.add(new Wolf());
                break;
        }

        //Create a new BattleContext based on the game settings
        return new BattleContext(player, enemyList, backupEnemyList, ui);
    }

    //Run the game once using the given BattleContext
    public static void runSingleGame(GameUI ui, GameSettings gameSettings) {
        //Get the BattleContext created using the game setting
        BattleContext battleContext = createBattleContext(gameSettings, ui);

        //Create a BattleEngine to run the game
        GraphicBattleEngine engine = new GraphicBattleEngine((GraphicUI) ui);

        engine.startBattle(battleContext, gameSettings.getDifficulty());
    }
}