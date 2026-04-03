package control;

import domain.Enemy;
import domain.Player;
import domain.Warrior;
import domain.Wizard;
import ui.GameUI;

import java.util.ArrayList;
import java.util.List;

public class BattleContext {
    private Player player;
    private String specialSkill;
    private List<Enemy> enemiesAlive;
    private List<Enemy> backupEnemies;
    private int smokeBombTurnsLeft;
    private int initialEnemyCount;
    private GameUI gameUI;

    // If no backup, just pass in an empty list
    public BattleContext(Player player, List<Enemy> enemies, List<Enemy> backupEnemies,  GameUI gameUI) {
        this.player = player;
        this.enemiesAlive = enemies;
        this.backupEnemies = backupEnemies;
        initialEnemyCount =  enemies.size();
        smokeBombTurnsLeft = 0;
        this.gameUI = gameUI;
    }

    public Player getPlayer(){
        return player;
    }

    public List<Enemy> getAliveEnemies(){
        return enemiesAlive;
    }

    public GameUI getGameUI(){
        return gameUI;
    }

    public void activateSmokeBomb(){
        smokeBombTurnsLeft = 3;
    }

    public boolean isSmokeBombActive(){
        // If smoke bomb active, still turns left
        return smokeBombTurnsLeft != 0;
    }

    public boolean isWaveCleared(){
        return enemiesAlive.isEmpty();
    }

    // Spawn backup enemies if have
    public void spawnBackupEnemies(){
        if (!backupEnemies.isEmpty() && enemiesAlive.isEmpty()){
            enemiesAlive.addAll(backupEnemies);
            backupEnemies.clear();
            System.out.println("Backup enemies appear! >>> ");
        }
    }

    public List<Enemy> getBackupEnemies(){
        return backupEnemies;
    }

    public void endTurn(){
        if (smokeBombTurnsLeft != 0){
            smokeBombTurnsLeft --;
        }
    }
}
