package ui;

import control.BattleContext;
import control.GraphicBattleEngine;
import control.Difficulty;
import control.GameSettings;
import control.GraphicBattleEngine;
import domain.Enemy;
import domain.Player;
import domain.Warrior;
import domain.Wizard;
import items.Items;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * GraphicUI
 *
 * A full Swing GUI version of the battle game.
 *
 * Screens:
 * 1. Start Screen - shows player/enemy/difficulty info
 * 2. Setup Screen - choose class, initial items, difficulty
 * 3. Battle Screen - action buttons + clickable enemies + HP bars + battle log
 *
 * NOTE:
 * - This class implements GameUI only for compatibility with your existing project.
 * - Most of the old "console input" methods are no longer used by the new BattleEngine.
 */
public class GraphicUI extends JFrame implements GameUI {

    // =========================
    // Window + Card Layout
    // =========================
    private CardLayout cardLayout;
    private JPanel rootPanel;

    private JPanel startPanel;
    private JPanel setupPanel;
    private JPanel battlePanel;

    // =========================
    // Fonts
    // =========================
    private Font titleFont;
    private Font bodyFont;
    private Font smallFont;

    // =========================
    // Setup screen controls
    // =========================
    private JComboBox<String> classBox;
    private JComboBox<String> item1Box;
    private JComboBox<String> item2Box;
    private JComboBox<String> difficultyBox;

    // =========================
    // Battle screen components
    // =========================
    private JLabel playerNameLabel;
    private JLabel playerStatsLabel;
    private JProgressBar playerHpBar;

    private JPanel enemyPanel;
    private JTextArea battleLog;

    private JButton attackButton;
    private JButton defendButton;
    private JButton itemButton;
    private JButton specialButton;

    // =========================
    // Game state
    // =========================
    private GraphicBattleEngine engine;
    private BattleContext currentContext;
    private GameSettings currentSettings;

    // Which action is waiting for an enemy click?
    private enum ActionMode {
        NONE,
        ATTACK_TARGET,
        SPECIAL_TARGET,
        ITEM_POWERSTONE_TARGET
    }

    private ActionMode actionMode = ActionMode.NONE;

    // If user uses Power Stone as Warrior, we need to remember it
    private boolean pendingPowerStone = false;

    public GraphicUI() {
        setTitle("Pixel Battle Game");
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Load fonts (pixel style if available)
        titleFont = loadPixelFont(20f);
        bodyFont = loadPixelFont(13f);
        smallFont = loadPixelFont(11f);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(new Color(18, 18, 18));

        buildStartPanel();
        buildSetupPanel();
        buildBattlePanel();

        rootPanel.add(startPanel, "START");
        rootPanel.add(setupPanel, "SETUP");
        rootPanel.add(battlePanel, "BATTLE");

        add(rootPanel);
        cardLayout.show(rootPanel, "START");
    }

    // ============================================================
    // Screen 1: Start Screen
    // ============================================================
    private void buildStartPanel() {
        startPanel = new JPanel(new BorderLayout());
        startPanel.setBackground(new Color(22, 22, 22));
        startPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("PIXEL BATTLE ARENA", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(titleFont);
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        startPanel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        infoPanel.setOpaque(false);

        infoPanel.add(createInfoCard(
                "PLAYERS",
                "1. Warrior\n" +
                        "- HP: 260\n" +
                        "- ATK: 40\n" +
                        "- DEF: 20\n" +
                        "- SPD: 30\n" +
                        "- Special: Shield Bash\n\n" +
                        "2. Wizard\n" +
                        "- HP: 200\n" +
                        "- ATK: 50\n" +
                        "- DEF: 10\n" +
                        "- SPD: 20\n" +
                        "- Special: Arcane Blast"
        ));

        infoPanel.add(createInfoCard(
                "ENEMIES",
                "Goblin\n" +
                        "- Balanced early enemy\n\n" +
                        "Wolf\n" +
                        "- Stronger and faster\n\n" +
                        "Enemies vary by difficulty.\n" +
                        "Some difficulties spawn backup waves."
        ));

        infoPanel.add(createInfoCard(
                "DIFFICULTY",
                "EASY\n" +
                        "- 3 Goblins\n\n" +
                        "MEDIUM\n" +
                        "- 1 Goblin + 1 Wolf\n" +
                        "- 2 backup Wolves\n\n" +
                        "HARD\n" +
                        "- 2 Goblins\n" +
                        "- 1 Goblin + 2 Wolves backup"
        ));

        startPanel.add(infoPanel, BorderLayout.CENTER);

        JButton startButton = createPixelButton("START");
        startButton.addActionListener(e -> cardLayout.show(rootPanel, "SETUP"));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(20, 0, 0, 0));
        bottom.add(startButton);

        startPanel.add(bottom, BorderLayout.SOUTH);
    }

    // ============================================================
    // Screen 2: Setup Screen
    // ============================================================
    private void buildSetupPanel() {
        setupPanel = new JPanel(new BorderLayout());
        setupPanel.setBackground(new Color(24, 24, 24));
        setupPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("GAME SETUP", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(titleFont);
        title.setBorder(new EmptyBorder(0, 0, 24, 0));
        setupPanel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        classBox = new JComboBox<>(new String[]{"Warrior", "Wizard"});
        item1Box = new JComboBox<>(new String[]{"Heal Potion", "Power Stone", "Smoke Bomb"});
        item2Box = new JComboBox<>(new String[]{"Heal Potion", "Power Stone", "Smoke Bomb"});
        difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        styleComboBox(classBox);
        styleComboBox(item1Box);
        styleComboBox(item2Box);
        styleComboBox(difficultyBox);

        addFormRow(form, gbc, 0, "Player Class:", classBox);
        addFormRow(form, gbc, 1, "Initial Item 1:", item1Box);
        addFormRow(form, gbc, 2, "Initial Item 2:", item2Box);
        addFormRow(form, gbc, 3, "Difficulty:", difficultyBox);

        center.add(form);
        setupPanel.add(center, BorderLayout.CENTER);

        JButton backButton = createPixelButton("BACK");
        backButton.addActionListener(e -> cardLayout.show(rootPanel, "START"));

        JButton confirmButton = createPixelButton("CONFIRM");
        confirmButton.addActionListener(e -> startGameFromSetup());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottom.setOpaque(false);
        bottom.add(backButton);
        bottom.add(confirmButton);

        setupPanel.add(bottom, BorderLayout.SOUTH);
    }

    // ============================================================
    // Screen 3: Battle Screen
    // ============================================================
    private void buildBattlePanel() {
        battlePanel = new JPanel(new BorderLayout(16, 16));
        battlePanel.setBackground(new Color(20, 20, 20));
        battlePanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top area: player panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(30, 30, 30));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setFont(bodyFont);

        playerStatsLabel = new JLabel("ATK/DEF/SPD");
        playerStatsLabel.setForeground(Color.LIGHT_GRAY);
        playerStatsLabel.setFont(smallFont);

        playerHpBar = new JProgressBar();
        playerHpBar.setStringPainted(true);
        playerHpBar.setFont(smallFont);
        playerHpBar.setPreferredSize(new Dimension(400, 28));

        JPanel playerInfo = new JPanel();
        playerInfo.setOpaque(false);
        playerInfo.setLayout(new BoxLayout(playerInfo, BoxLayout.Y_AXIS));
        playerInfo.add(playerNameLabel);
        playerInfo.add(Box.createVerticalStrut(8));
        playerInfo.add(playerHpBar);
        playerInfo.add(Box.createVerticalStrut(8));
        playerInfo.add(playerStatsLabel);

        topPanel.add(playerInfo, BorderLayout.CENTER);
        battlePanel.add(topPanel, BorderLayout.NORTH);

        // Center area: enemies + battle log
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centerPanel.setOpaque(false);

        // Enemy side
        JPanel enemyContainer = new JPanel(new BorderLayout());
        enemyContainer.setBackground(new Color(30, 30, 30));
        enemyContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel enemyTitle = new JLabel("ENEMIES", SwingConstants.CENTER);
        enemyTitle.setForeground(Color.WHITE);
        enemyTitle.setFont(bodyFont);
        enemyTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        enemyPanel = new JPanel();
        enemyPanel.setOpaque(false);
        enemyPanel.setLayout(new GridLayout(0, 2, 12, 12));

        JScrollPane enemyScroll = new JScrollPane(enemyPanel);
        enemyScroll.setBorder(null);
        enemyScroll.getViewport().setBackground(new Color(30, 30, 30));

        enemyContainer.add(enemyTitle, BorderLayout.NORTH);
        enemyContainer.add(enemyScroll, BorderLayout.CENTER);

        // Log side
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setBackground(new Color(30, 30, 30));
        logContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel logTitle = new JLabel("BATTLE LOG", SwingConstants.CENTER);
        logTitle.setForeground(Color.WHITE);
        logTitle.setFont(bodyFont);
        logTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBackground(new Color(18, 18, 18));
        battleLog.setForeground(Color.GREEN);
        battleLog.setFont(smallFont);
        battleLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(null);

        logContainer.add(logTitle, BorderLayout.NORTH);
        logContainer.add(logScroll, BorderLayout.CENTER);

        centerPanel.add(enemyContainer);
        centerPanel.add(logContainer);

        battlePanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom area: 4 action buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        actionPanel.setOpaque(false);

        attackButton = createPixelButton("ATTACK");
        defendButton = createPixelButton("DEFEND");
        itemButton = createPixelButton("ITEM");
        specialButton = createPixelButton("SPECIAL");

        attackButton.addActionListener(e -> {
            if (engine == null || engine.isBattleEnded()) return;
            actionMode = ActionMode.ATTACK_TARGET;
            appendLog("[SYSTEM] Select an enemy to ATTACK.");
        });

        defendButton.addActionListener(e -> {
            if (engine == null || engine.isBattleEnded()) return;
            actionMode = ActionMode.NONE;
            pendingPowerStone = false;
            engine.playerDefend();
        });

        itemButton.addActionListener(e -> {
            if (engine == null || engine.isBattleEnded()) return;
            showItemDialog();
        });

        specialButton.addActionListener(e -> {
            if (engine == null || engine.isBattleEnded()) return;

            Player player = currentContext.getPlayer();

            if (!player.canUseSpecialSkill()) {
                appendLog("[SYSTEM] Special skill on cooldown: " + player.getSpecialCooldown() + " turn(s) left.");
                return;
            }

            if (player instanceof Warrior) {
                actionMode = ActionMode.SPECIAL_TARGET;
                appendLog("[SYSTEM] Select an enemy for SHIELD BASH.");
            } else if (player instanceof Wizard) {
                actionMode = ActionMode.NONE;
                engine.playerSpecial(-1);
            }
        });

        actionPanel.add(attackButton);
        actionPanel.add(defendButton);
        actionPanel.add(itemButton);
        actionPanel.add(specialButton);

        battlePanel.add(actionPanel, BorderLayout.SOUTH);

        enablePlayerActions(false);
    }

    // ============================================================
    // Start the game after setup
    // ============================================================
    private void startGameFromSetup() {
        int playerChoice = classBox.getSelectedIndex() + 1;

        int[] items = new int[] {
                item1Box.getSelectedIndex() + 1,
                item2Box.getSelectedIndex() + 1
        };

        Difficulty difficulty;
        switch (difficultyBox.getSelectedIndex()) {
            case 0:
                difficulty = Difficulty.EASY;
                break;
            case 1:
                difficulty = Difficulty.MEDIUM;
                break;
            case 2:
            default:
                difficulty = Difficulty.HARD;
                break;
        }

        currentSettings = new GameSettings(playerChoice, items, difficulty);

        // Create battle context directly (no console flow)
        currentContext = createBattleContextFromSettings(currentSettings);

        engine = new GraphicBattleEngine(this);

        battleLog.setText("");
        actionMode = ActionMode.NONE;
        pendingPowerStone = false;

        cardLayout.show(rootPanel, "BATTLE");
        refreshBattleScreen();

        engine.startBattle(currentContext, difficulty);
    }

    /**
     * This replaces GameControl.createBattleContext(...) inside GUI mode
     * so we don't need to launch ConsoleUI.
     */
    private BattleContext createBattleContextFromSettings(GameSettings gameSettings) {
        domain.Player player = null;

        switch (gameSettings.getPlayerChoice()) {
            case 1:
                player = new Warrior();
                break;
            case 2:
                player = new Wizard();
                break;
        }

        // Add items
        int[] itemChoice = gameSettings.getInitialItemChoice();
        for (int choice : itemChoice) {
            switch (choice) {
                case 1:
                    player.getInventory().addItem(new items.PotionItem());
                    break;
                case 2:
                    player.getInventory().addItem(new items.PowerStone());
                    break;
                case 3:
                    player.getInventory().addItem(new items.SmokeBomb());
                    break;
            }
        }

        java.util.List<domain.Enemy> enemyList = new java.util.ArrayList<>();
        java.util.List<domain.Enemy> backupEnemyList = new java.util.ArrayList<>();

        switch (gameSettings.getDifficulty()) {
            case EASY:
                enemyList.add(new domain.Goblin());
                enemyList.add(new domain.Goblin());
                enemyList.add(new domain.Goblin());
                break;

            case MEDIUM:
                enemyList.add(new domain.Goblin());
                enemyList.add(new domain.Wolf());
                backupEnemyList.add(new domain.Wolf());
                backupEnemyList.add(new domain.Wolf());
                break;

            case HARD:
                enemyList.add(new domain.Goblin());
                enemyList.add(new domain.Goblin());
                backupEnemyList.add(new domain.Goblin());
                backupEnemyList.add(new domain.Wolf());
                backupEnemyList.add(new domain.Wolf());
                break;
        }

        return new BattleContext(player, enemyList, backupEnemyList, this);
    }

    // ============================================================
    // Public methods used by the NEW BattleEngine
    // ============================================================
    public void appendLog(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    public void enablePlayerActions(boolean enabled) {
        attackButton.setEnabled(enabled);
        defendButton.setEnabled(enabled);
        itemButton.setEnabled(enabled);
        specialButton.setEnabled(enabled);
    }

    public void refreshBattleScreen() {
        if (currentContext == null) return;

        Player player = currentContext.getPlayer();

        playerNameLabel.setText(player.getName() + "   (Turn " + GraphicBattleEngine.getCurrentTurn() + ")");
        playerHpBar.setMaximum(player.getMaxHp());
        playerHpBar.setValue(player.getHp());
        playerHpBar.setString(player.getHp() + " / " + player.getMaxHp());

        playerStatsLabel.setText(
                "ATK: " + player.getAtk() +
                        "    DEF: " + player.getDef() +
                        "    SPD: " + player.getSpd() +
                        "    Special CD: " + player.getSpecialCooldown()
        );

        rebuildEnemyPanel();
    }

    private void rebuildEnemyPanel() {
        enemyPanel.removeAll();

        List<Enemy> enemies = currentContext.getAliveEnemies();

        if (enemies.isEmpty()) {
            JLabel noEnemies = new JLabel("No enemies on field", SwingConstants.CENTER);
            noEnemies.setForeground(Color.LIGHT_GRAY);
            noEnemies.setFont(bodyFont);
            enemyPanel.setLayout(new BorderLayout());
            enemyPanel.add(noEnemies, BorderLayout.CENTER);
        } else {
            enemyPanel.setLayout(new GridLayout(0, 2, 12, 12));

            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);

                JPanel enemyCard = new JPanel(new BorderLayout(8, 8));
                enemyCard.setBackground(new Color(45, 45, 45));
                enemyCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        new EmptyBorder(10, 10, 10, 10)
                ));

                JLabel sprite = new JLabel(getEnemyAscii(enemy), SwingConstants.CENTER);
                sprite.setForeground(Color.WHITE);
                sprite.setFont(bodyFont);

                JButton enemyButton = createPixelButton(enemy.getName());
                final int enemyIndex = i;
                enemyButton.addActionListener(e -> onEnemyClicked(enemyIndex));

                JProgressBar hpBar = new JProgressBar(0, enemy.getMaxHp());
                hpBar.setValue(enemy.getHp());
                hpBar.setStringPainted(true);
                hpBar.setFont(smallFont);
                hpBar.setString(enemy.getHp() + " / " + enemy.getMaxHp());

                JLabel statLabel = new JLabel(
                        "ATK " + enemy.getAtk() + " | DEF " + enemy.getDef() + " | SPD " + enemy.getSpd(),
                        SwingConstants.CENTER
                );
                statLabel.setForeground(Color.LIGHT_GRAY);
                statLabel.setFont(smallFont);

                enemyCard.add(sprite, BorderLayout.NORTH);
                enemyCard.add(enemyButton, BorderLayout.CENTER);

                JPanel south = new JPanel();
                south.setOpaque(false);
                south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
                south.add(hpBar);
                south.add(Box.createVerticalStrut(6));
                south.add(statLabel);

                enemyCard.add(south, BorderLayout.SOUTH);

                enemyPanel.add(enemyCard);
            }
        }

        enemyPanel.revalidate();
        enemyPanel.repaint();
    }

    private void onEnemyClicked(int enemyIndex) {
        if (engine == null || engine.isBattleEnded()) return;

        switch (actionMode) {
            case ATTACK_TARGET:
                actionMode = ActionMode.NONE;
                pendingPowerStone = false;
                engine.playerBasicAttack(enemyIndex);
                break;

            case SPECIAL_TARGET:
                actionMode = ActionMode.NONE;
                pendingPowerStone = false;
                engine.playerSpecial(enemyIndex);
                break;

            case ITEM_POWERSTONE_TARGET:
                actionMode = ActionMode.NONE;
                pendingPowerStone = false;
                engine.playerUsePowerStoneOnTarget(enemyIndex);
                break;

            default:
                appendLog("[SYSTEM] Choose an action first.");
        }
    }

    // ============================================================
    // GUI item dialog
    // ============================================================
    private void showItemDialog() {
        Player player = currentContext.getPlayer();

        if (player.getInventory().isEmpty()) {
            appendLog("[SYSTEM] No items in inventory.");
            return;
        }

        List<Items> inventoryItems = player.getInventory().getItems();
        String[] options = new String[inventoryItems.size()];

        for (int i = 0; i < inventoryItems.size(); i++) {
            options[i] = (i + 1) + ". " + inventoryItems.get(i).getName();
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Choose an item:",
                "Inventory",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (selected == null) return;

        int index = Arrays.asList(options).indexOf(selected);
        if (index < 0) return;

        Items chosenItem = inventoryItems.get(index);

        // Special handling for Power Stone + Warrior target selection
        if ("Power Stone".equals(chosenItem.getName()) && currentContext.getPlayer() instanceof Warrior) {
            pendingPowerStone = true;
            actionMode = ActionMode.ITEM_POWERSTONE_TARGET;
            appendLog("[SYSTEM] Power Stone selected. Click an enemy to trigger Shield Bash without cooldown.");
        } else {
            actionMode = ActionMode.NONE;
            pendingPowerStone = false;
            engine.playerUseItem(index);
        }
    }
    @Override
    public void showMessage(String message) {
        appendLog(message);
    }

    // ============================================================
    // Battle result
    // ============================================================
    @Override
    public void showBattleResult(boolean victory, BattleContext ctx) {
        enablePlayerActions(false);

        String message;
        if (victory) {
            message = "VICTORY!\nYou defeated all enemies!";
            appendLog("[SYSTEM] VICTORY!");
        } else {
            message = "DEFEAT...\nYour hero has fallen.";
            appendLog("[SYSTEM] DEFEAT...");
        }

        int choice = JOptionPane.showOptionDialog(
                this,
                message + "\n\nWould you like to return to setup screen?",
                "Battle Result",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Return to Setup", "Close Game"},
                "Return to Setup"
        );

        if (choice == 0) {
            cardLayout.show(rootPanel, "SETUP");
        } else {
            dispose();
        }
    }

    // ============================================================
    // Styling helpers
    // ============================================================
    private JPanel createInfoCard(String title, String content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 35));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(bodyFont);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));

        JTextArea text = new JTextArea(content);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(smallFont);
        text.setBackground(new Color(35, 35, 35));
        text.setForeground(Color.LIGHT_GRAY);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(text, BorderLayout.CENTER);

        return panel;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent input) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(bodyFont);

        gbc.gridx = 0;
        gbc.gridy = row;
        form.add(label, gbc);

        gbc.gridx = 1;
        form.add(input, gbc);
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(bodyFont);
        comboBox.setPreferredSize(new Dimension(250, 34));
    }

    private JButton createPixelButton(String text) {
        JButton button = new JButton(text);
        button.setFont(bodyFont);
        button.setFocusPainted(false);
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setPreferredSize(new Dimension(180, 42));
        return button;
    }

    private String getEnemyAscii(Enemy enemy) {
        String name = enemy.getName().toLowerCase();
        if (name.contains("goblin")) {
            return "[GOBLIN]";
        } else if (name.contains("wolf")) {
            return "[WOLF]";
        }
        return "[ENEMY]";
    }

    /**
     * Loads a pixel font from resources if available.
     *
     * Put your font here if you want:
     * src/assets/fonts/PressStart2P-Regular.ttf
     *
     * If not found, it falls back to Monospaced.
     */
    private Font loadPixelFont(float size) {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf");
            if (is != null) {
                Font base = Font.createFont(Font.TRUETYPE_FONT, is);
                return base.deriveFont(size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("Monospaced", Font.BOLD, (int) size);
    }

    // ============================================================
    // Legacy GameUI methods (unused by new GUI flow)
    // These are only here so GraphicUI can still implement GameUI
    // ============================================================

    @Override
    public int choosePlayer() {
        return 1; // unused
    }

    @Override
    public int[] chooseInitialItems(int inventorySize) {
        return new int[]{1, 1}; // unused
    }

    @Override
    public void showDifficulty() {
        // unused
    }

    @Override
    public void listEnemies() {
        // unused
    }

    @Override
    public Difficulty selectDifficulty() {
        return Difficulty.MEDIUM; // unused
    }

    @Override
    public void showBattleStart(Difficulty difficulty) {
        // optional legacy hook
        appendLog("[SYSTEM] Battle started! Difficulty: " + difficulty);
    }

    @Override
    public int choosePlayerActions() {
        return -1; // unused
    }

    @Override
    public void showEnemies(BattleContext ctx) {
        // unused (replaced by refreshBattleScreen)
    }

    @Override
    public int chooseTarget(BattleContext ctx) {
        return -1; // intentionally unused in GUI flow
    }

    @Override
    public int chooseItem(BattleContext ctx) {
        return -1; // unused
    }

    @Override
    public int restartOption() {
        return 3; // unused
    }
}