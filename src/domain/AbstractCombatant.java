package domain;

import effects.StatusEffects;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCombatant implements Combatant {
    protected String name;
    protected int hp;
    protected int maxHp;
    protected int atk;
    protected int def;
    protected int spd;
    protected List<StatusEffects> effects = new ArrayList<>();

    // Constructor
    public AbstractCombatant(String name, int hp, int atk, int def, int spd) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.atk = atk;
        this.def = def;
        this.spd = spd;
    }

    @Override
    public String getName(){
        return this.name;
    }

    @Override
    public int getHp() {
        return this.hp;
    }

    @Override
    public int getMaxHp() {
        return this.maxHp;
    }

    @Override
    public int getAtk() {
        // Return the sum of atk after summing all effects
        int finalAtk = atk + effects.stream().mapToInt(StatusEffects::modifyAtk).sum();
        return finalAtk;
    }

    @Override
    public int getDef() {
        int finalDef = def + effects.stream().mapToInt(StatusEffects::modifyDef).sum();
        return finalDef;
    }

    @Override
    public int getSpd() {
        int finalSpd = spd + effects.stream().mapToInt(StatusEffects::modifySpd).sum();
        return finalSpd;
    }

    @Override
    public boolean isAlive() {
        return this.hp <= 0; // If hp <= 0, combatant is dead, return true
    }

    @Override
    public void takeDamage(int damage) {
        this.hp -= damage;
    }

    @Override
    public void heal(int amount) {
        this.hp += amount;
    }

    @Override
    public void addStatusEffect(StatusEffects e){
        effects.add(e);
    }

    @Override
    public boolean canAct(){
        // If none of the status effects blocks action, return true
        return effects.stream().noneMatch(StatusEffects::blockAction);
    }
}