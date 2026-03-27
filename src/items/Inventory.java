package items;

import domain.Player;

import java.util.ArrayList;
import java.util.List;

public class Inventory{
    private List<Items> items = new ArrayList<Items>();

    public void addItem(Items i){
        items.add(i);
    }

    public void useItem(int index, BattleContext ctx, Player p){
        if(index >= 0 && index < items.size()){
            Items itemUsed =  items.remove(index);
            itemUsed.apply(ctx, p);
        }
        else{
            System.err.println("Invalid item index");
        }
    }

    public boolean isEmpty(){
        return items.isEmpty();
    }

    public int  getSize(){
        return items.size();
    }

}
