package actions;

import control.BattleContext;
import domain.Combatant;
import domain.Player;
import items.Inventory;
import ui.GameUI;

import java.util.Scanner;

public class UseItems implements Actions {

    GameUI ui;

    public UseItems(GameUI ui){
        this.ui = ui;
    }

    @Override
    public String actionName(){
        return "UseItems";
    }

    @Override
    public boolean execute(BattleContext ctx, Combatant p){
        Player player = (Player) p;

        Inventory playerInventory = player.getInventory();
        if (playerInventory.displayItems()){
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter the number of the item you want to use:");
            int choice = sc.nextInt();
            playerInventory.useItem(choice - 1 , ctx, player);
        }
        else{
            System.out.println("You don't have enough items to use this action, congrats on wasting a turn!");
        }
        return false;
    }

}
