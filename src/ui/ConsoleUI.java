package ui;

import control.BattleContext;
import control.BattleEngine;
import control.BattleOutcome;
import control.Difficulty;
import domain.Enemy;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI implements GameUI {

    Scanner input = new Scanner(System.in);

    //Methods at the initialization of the game
    @Override
    public int choosePlayer() {
        //Choose player, List attributes, Option to pick
        System.out.println("Choose your player:");
        System.out.println("1: Warrior");
        System.out.println("Hp: 260, Atk: 40, Def: 20, Spd: 30");
        System.out.println("Special Skill: ShieldBash -- Deal normal attack damage to enemy and stun them for 2 turns");
        System.out.println("2: Wizard");
        System.out.println("Hp: 200, Atk: 50, Def: 10, Spd: 20");
        System.out.println("Special Skill: ArcaneBlast -- Deal normal attack damage to " +
                "all the enemies, add 10 atk for every enemy defeated");
        System.out.println();
        System.out.print("Please choose your player: ");
        int choice = input.nextInt();
        return choice;
    }
    @Override
    public int[] chooseInitialItems(int inventorySize) {
        int[] itemChoices = new int[inventorySize];
        for(int i = 0; i < itemChoices.length; i++){
            System.out.println("Please enter the item you would like to add to inventory: ");
            System.out.println("1: Potion -- heals your player 100 hp");
            System.out.println("2: Power Stone -- your player can use special skill once ignoring cooldown");
            System.out.println("3: Smoke Bomb -- your player receives 0 dmg for this turn and next turn");
            System.out.println();
            int choice = input.nextInt();
            itemChoices[i] = choice;
        }
        return itemChoices;
    }
    @Override
    public void showDifficulty() {
        System.out.println("Choose your difficulty:");
        System.out.println("1: Easy: Initial Spawn 3 goblins, no backup spawn");
        System.out.println("2: Medium: Initial Spawn 1 goblin and 1 wolf, backup spawn 2 wolves");
        System.out.println("3: Hard: Initial Spawn 2 goblins, backup spawn 1 goblin 2 wolves");
        System.out.println();
    }
    @Override
    public void listEnemies() {
        System.out.println("Here are the enemies you'll encounter in the battle: ");
        System.out.println("Goblins: Hp 55, Atk 35, Def 15, Spd 25");
        System.out.println("Wolf: Hp 40, Atk 45, Def 5, Spd 35");
        System.out.println();
    }
    @Override
    public Difficulty selectDifficulty(){
        System.out.println("Choose your difficulty:");
        System.out.println("1: Easy");
        System.out.println("2: Medium");
        System.out.println("3: Hard");
        System.out.print("If you do not enter a valid difficulty, by default it will be medium: ");
        System.out.println();
        int choice = input.nextInt();
        switch(choice){
            case 1:
                return  Difficulty.EASY;
            case 2:
                return  Difficulty.MEDIUM;
            case 3:
                return  Difficulty.HARD;
        }
        return Difficulty.MEDIUM;
    }

    //Methods during the game
    @Override
    public void showBattleStart(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                System.out.println("You start the battle with easy difficulty!");
                System.out.println();
                break;
        }
    }
    @Override
    public int choosePlayerActions() {
        //Option to pick items, allow duplicates
        System.out.println("It's now your turn!");
        System.out.println("1: Basic attack to an enemy!");
        System.out.println("2: Defend the incoming attack! Def +10!");
        System.out.println("3: Open your inventory and use an item!");
        System.out.println("4: Perform your character's special skill!");
        System.out.print("Please choose your action: ");
        System.out.println();
        int choice = input.nextInt();
        return choice;
    }
    @Override
    public void showEnemies(BattleContext ctx) {
        List<Enemy> enemies = ctx.getAliveEnemies();
        for (int i = 1; i <= enemies.size(); i++) {
            System.out.println(i + " : " + enemies.get(i-1).getName());
        }
        System.out.println();
    }
    @Override
    public int chooseTarget(BattleContext ctx) {
        System.out.println("Choose your target: ");
        System.out.println();
        this.showEnemies(ctx);
        int target = input.nextInt();
        return target;
    }

    //Methods after the game ends
    @Override
    public void showBattleResult(boolean victory, BattleContext ctx) {
        if (victory) {
            System.out.println("You win! Remaining HP: " + ctx.getPlayer().getHp());
            System.out.println("After " + BattleEngine.getCurrentTurn() + "turn(s), you defeated all enemies and win!");
        }
        else {
            System.out.println("After " + BattleEngine.getCurrentTurn() + "turn(s), you are defeated by enemies!");
        }
    }
    @Override
    public int restartOption(){
        System.out.println("Choose your restart option:");
        System.out.println("1: Restart using the same setting");
        System.out.println("2: Start a new game");
        System.out.println("3: Quit");
        int choice = input.nextInt();
        return choice;
    }
}



