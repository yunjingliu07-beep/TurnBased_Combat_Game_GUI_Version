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
    public void execute(BattleContext ctx, Combatant p){
        Player player = (Player) p;

        Inventory playerInventory = player.getInventory();
        playerInventory.displayItems();
        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();
        playerInventory.useItem(choice - 1 , ctx, player);

    }

}
