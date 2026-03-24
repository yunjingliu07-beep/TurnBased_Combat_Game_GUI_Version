package ui;

import domain.Combatant;

import java.util.List;

public interface GameUI {
    String showMessage(String msg);
    int chooseOption(String prompt, List<String> choices);
    Combatant chooseTarget(String prompt, List<Combatant> targets);
}
