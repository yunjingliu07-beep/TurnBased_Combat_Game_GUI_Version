package domain;

public interface Combatant {
    String getName(); //Get the name of the combatant
    int getHp(); //Get current Hp
    int getMaxHp(); //Get max Hp
    int getAtk();
    int getDef();
    int getSpd();
    boolean isAlive();
    void takeDamage(int damage);
    void heal(int amount);
    void addStatusEffect(StatusEffect e);
    boolean canAct();

}
