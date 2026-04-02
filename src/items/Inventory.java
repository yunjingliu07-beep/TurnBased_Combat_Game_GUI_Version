package items;

import domain.Player;
import control.BattleContext;

import java.util.ArrayList;
import java.util.List;

public class Inventory{
    private List<Items> items = new ArrayList<Items>();

    public void addItem(Items i){
        items.add(i);
    }

    public List<Items> getItems(){
        return items;
    }

    public boolean displayItems(){
        if(!items.isEmpty()){
            for(int i = 0; i < items.size(); i++){
                System.out.println(i+1 + " : " + items.get(i).getName());
            }
            return true;
        }
        else{
            System.out.println("There are no items in your inventory!");
            return false;
        }
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
