package domain;

import actions.SpecialSkill;

public class Warrior extends Player {
    // Constructor
    public Warrior() {
        super("Warrior", 260, 40, 20, 30);
    }

    @Override
    public void useSpecialSkill(boolean consumeCooldown){
        if (consumeCooldown) {
            super.specialCooldown = 3; // Cooldown turns = 3
        }
    }

}
