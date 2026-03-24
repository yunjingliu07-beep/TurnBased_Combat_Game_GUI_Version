package domain;

public abstract class AbstractCombatant {
    protected String name;
    protected int hp;
    protected int maxHp;
    protected int atk;
    protected int def;
    protected int spd;
    protected List<StatusEffects> effects;
}