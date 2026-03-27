package domain;

import items.Inventory;

public abstract class Player extends AbstractCombatant{
    protected Inventory inventory;
    protected int specialCooldown;

    public Player(String name, int hp, int atk, int def, int spd) {
        super(name, hp, atk, def, spd);
        this.inventory = new Inventory();
    }

    public int getSpecialCooldown() {
        return specialCooldown;
    }

    public void tickSpecialCooldown() {
        if (super.canAct() &&  this.specialCooldown > 0){
            this.specialCooldown--;
        }
    }

    public boolean canUseSpecialSkill(){
        return specialCooldown == 0;
    }


    public Inventory getInventory() {
        return inventory;
    }

    public abstract void useSpecialSkill(boolean consumeCoolDown);
}
