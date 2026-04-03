import control.*;
import domain.*;
import items.PotionItem;
import items.PowerStone;
import items.SmokeBomb;
import ui.ConsoleUI;
import ui.GameUI;
import ui.GraphicUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public  class GameControl {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new GraphicUI();
        });
    }
}