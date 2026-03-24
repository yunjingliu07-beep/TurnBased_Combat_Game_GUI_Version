package items;

public interface Items {
    String name();
    void apply(BattleContext ctx, Player p);
}
