package actions;

import control.BattleContext;
import domain.Combatant;
import domain.Player;
import items.Inventory;
import ui.GameUI;

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

        int choice = ui.chooseItems();
        Inventory playerInventory = player.getInventory();
        playerInventory.useItem(choice, ctx, player);

    }

}
