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
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.awt.image.BufferedImage;

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
    private static final String MENU_BGM_PATH = "GameAssets/music/BackGroundMusic.wav";
    private static final String BATTLE_BGM_PATH = "GameAssets/music/Music-Dungeon.wav";

    // =========================
    // Window + Card Layout
    // =========================
    private CardLayout cardLayout;
    private JPanel rootPanel;

    private JPanel titlePanel;
    private JPanel startPanel;
    private JPanel setupPanel;
    private JPanel battlePanel;
    private JPanel battleCenterPanel;
    private CardLayout battleCenterLayout;
    private JPanel resultPanel;
    private JLabel resultTitleLabel;
    private JLabel resultMessageLabel;
    private JPanel itemPanel;
    private JPanel itemGrid;
    private JPanel actionContainer;

    // =========================
    // Fonts
    // =========================
    private Font titleFont;
    private Font bodyFont;
    private Font smallFont;

    // =========================
    // Setup screen controls
    // =========================
    private JButton[] classButtons;
    private JButton[] item1Buttons;
    private JButton[] item2Buttons;
    private JButton[] difficultyButtons;
    private int selectedClassIndex = 0;
    private int selectedItem1Index = 0;
    private int selectedItem2Index = 0;
    private int selectedDifficultyIndex = 0;

    // =========================
    // Battle screen components
    // =========================
    private JLabel playerNameLabel;
    private JLabel playerStatsLabel;
    private JProgressBar playerHpBar;
    private JLabel playerPortraitLabel;

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
    private AudioPlayer audioPlayer;
    private Image backgroundImage;

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
        setTitle("Into the Dungeon");
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        audioPlayer = new AudioPlayer();

        // Load fonts (pixel style if available)
        titleFont = loadPixelFont(26f);
        bodyFont = loadPixelFont(18f);
        smallFont = loadPixelFont(14f);
        backgroundImage = loadRawImage("GameAssets/images/Background.png");

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(new Color(18, 18, 18));

        buildTitlePanel();
        buildStartPanel();
        buildSetupPanel();
        buildBattlePanel();

        rootPanel.add(titlePanel, "TITLE");
        rootPanel.add(startPanel, "START");
        rootPanel.add(setupPanel, "SETUP");
        rootPanel.add(battlePanel, "BATTLE");

        add(rootPanel);
        audioPlayer.playLoop(MENU_BGM_PATH);
        cardLayout.show(rootPanel, "TITLE");
        setVisible(true);
    }

    // ============================================================
    // Screen 1: Title Screen
    // ============================================================
    private void buildTitlePanel() {
        titlePanel = createBackgroundPanel(new BorderLayout());
        titlePanel.setBorder(new EmptyBorder(36, 36, 36, 36));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel titleImage = loadImageLabel("GameAssets/images/Title.png", 900, 300);
        titleImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleImage.setHorizontalAlignment(SwingConstants.CENTER);

        JButton startGameButton = createPixelButton("START GAME");
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameButton.addActionListener(e -> cardLayout.show(rootPanel, "START"));

        JButton quitButton = createPixelButton("QUIT");
        quitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        quitButton.addActionListener(e -> dispose());

        center.add(Box.createVerticalGlue());
        center.add(titleImage);
        center.add(Box.createVerticalStrut(36));
        center.add(startGameButton);
        center.add(Box.createVerticalStrut(16));
        center.add(quitButton);
        center.add(Box.createVerticalGlue());

        titlePanel.add(center, BorderLayout.CENTER);
    }

    // ============================================================
    // Screen 2: Info Screen
    // ============================================================
    private void buildStartPanel() {
        startPanel = createBackgroundPanel(new BorderLayout());
        startPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("INTO THE DUNGEON", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(titleFont);
        title.setBorder(new EmptyBorder(10, 0, 20, 0));
        startPanel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        infoPanel.setOpaque(false);

        infoPanel.add(createInfoCard(
                "PLAYERS",
                "1. Warrior\n" +
                        "- HP: 260\n" +
                        "- ATK: 40\n" +
                        "- DEF: 20\n" +
                        "- SPD: 30\n" +
                        "- Special: Shield Bash -- Deal a basic attack, stun the enemy hit for 2 turns\n\n" +
                        "2. Wizard\n" +
                        "- HP: 200\n" +
                        "- ATK: 50\n" +
                        "- DEF: 10\n" +
                        "- SPD: 20\n" +
                        "- Special: Arcane Blast -- Deal basic attack to all enemies alive, +10 atk for all enemies defeated",
                Arrays.asList(
                        loadImageLabel("GameAssets/images/Warrior.png", 96, 96),
                        loadImageLabel("GameAssets/images/Wizard.png", 96, 96)
                )
        ));

        infoPanel.add(createInfoCard(
                "ENEMIES",
                "Goblin\n" +
                        "- Balanced early enemy\n\n" +
                        "- HP: 55\n" +
                        "- ATK: 35\n" +
                        "- DEF: 15\n" +
                        "- SPD: 20\n" +
                        "\n" +
                        "Wolf\n" +
                        "- Stronger and faster, but with lower hp and def\n\n" +
                        "- HP: 40\n" +
                        "- ATK: 45\n" +
                        "- DEF: 5\n" +
                        "- SPD: 35\n" +
                        "\n" +
                        "Enemies vary by difficulty.\n" +
                        "Some difficulties spawn backup waves.",
                Arrays.asList(
                        loadImageLabel("GameAssets/images/Goblin.png", 96, 96),
                        loadImageLabel("GameAssets/images/Wolf.png", 96, 96)
                )
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

        infoPanel.add(createItemInfoCard());

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
    // Screen 3: Setup Screen
    // ============================================================
    private void buildSetupPanel() {
        setupPanel = createBackgroundPanel(new BorderLayout());
        setupPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("GAME SETUP", SwingConstants.CENTER);
        title.setForeground(new Color(240, 220, 180));
        title.setFont(titleFont);
        title.setBorder(new EmptyBorder(0, 0, 24, 0));
        setupPanel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel classSection = createSetupSection("CHOOSE YOUR CLASS");
        JPanel classRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        classRow.setOpaque(false);

        JButton warriorButton = createSelectionButton(
                "Warrior",
                loadImageIcon("GameAssets/images/Warrior.png", 96, 96)
        );
        JButton wizardButton = createSelectionButton(
                "Wizard",
                loadImageIcon("GameAssets/images/Wizard.png", 96, 96)
        );

        classButtons = new JButton[]{warriorButton, wizardButton};
        for (int i = 0; i < classButtons.length; i++) {
            final int index = i;
            classButtons[i].addActionListener(e -> {
                selectedClassIndex = index;
                updateSelectionGroup(classButtons, selectedClassIndex);
            });
            classRow.add(classButtons[i]);
        }
        updateSelectionGroup(classButtons, selectedClassIndex);
        classSection.add(classRow, BorderLayout.CENTER);

        JPanel itemSection = createSetupSection("CHOOSE INITIAL ITEMS");
        JPanel itemContainer = new JPanel();
        itemContainer.setOpaque(false);
        itemContainer.setLayout(new BoxLayout(itemContainer, BoxLayout.Y_AXIS));

        ImageIcon potionIcon = getItemIcon("Heal Potion");
        ImageIcon powerIcon = getItemIcon("Power Stone");
        ImageIcon smokeIcon = getItemIcon("Smoke Bomb");

        item1Buttons = new JButton[]{
                createSelectionButton("Heal Potion", potionIcon),
                createSelectionButton("Power Stone", powerIcon),
                createSelectionButton("Smoke Bomb", smokeIcon)
        };
        item2Buttons = new JButton[]{
                createSelectionButton("Heal Potion", potionIcon),
                createSelectionButton("Power Stone", powerIcon),
                createSelectionButton("Smoke Bomb", smokeIcon)
        };

        JPanel itemRow1 = createItemRow("Item Slot 1", item1Buttons);
        JPanel itemRow2 = createItemRow("Item Slot 2", item2Buttons);

        for (int i = 0; i < item1Buttons.length; i++) {
            final int index = i;
            item1Buttons[i].addActionListener(e -> {
                selectedItem1Index = index;
                updateSelectionGroup(item1Buttons, selectedItem1Index);
            });
            item2Buttons[i].addActionListener(e -> {
                selectedItem2Index = index;
                updateSelectionGroup(item2Buttons, selectedItem2Index);
            });
        }
        updateSelectionGroup(item1Buttons, selectedItem1Index);
        updateSelectionGroup(item2Buttons, selectedItem2Index);

        itemContainer.add(itemRow1);
        itemContainer.add(Box.createVerticalStrut(10));
        itemContainer.add(itemRow2);
        itemSection.add(itemContainer, BorderLayout.CENTER);

        JPanel difficultySection = createSetupSection("CHOOSE DIFFICULTY");
        JPanel difficultyRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        difficultyRow.setOpaque(false);

        difficultyButtons = new JButton[]{
                createSelectionButton("Easy", createBadgeIcon("E", new Color(70, 120, 70), new Color(240, 250, 240))),
                createSelectionButton("Medium", createBadgeIcon("M", new Color(140, 110, 40), new Color(255, 245, 220))),
                createSelectionButton("Hard", createBadgeIcon("H", new Color(140, 60, 50), new Color(255, 235, 230)))
        };

        for (int i = 0; i < difficultyButtons.length; i++) {
            final int index = i;
            difficultyButtons[i].addActionListener(e -> {
                selectedDifficultyIndex = index;
                updateSelectionGroup(difficultyButtons, selectedDifficultyIndex);
            });
            difficultyRow.add(difficultyButtons[i]);
        }
        updateSelectionGroup(difficultyButtons, selectedDifficultyIndex);
        difficultySection.add(difficultyRow, BorderLayout.CENTER);

        center.add(classSection);
        center.add(Box.createVerticalStrut(16));
        center.add(itemSection);
        center.add(Box.createVerticalStrut(16));
        center.add(difficultySection);
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
        battlePanel = createBackgroundPanel(new BorderLayout(16, 16));
        battlePanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top area: player panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(36, 28, 20));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel statusTitle = new JLabel("HERO STATUS");
        statusTitle.setForeground(new Color(240, 220, 180));
        statusTitle.setFont(bodyFont);
        statusTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setForeground(Color.WHITE);
        playerNameLabel.setFont(bodyFont);

        playerStatsLabel = new JLabel("ATK/DEF/SPD");
        playerStatsLabel.setForeground(Color.LIGHT_GRAY);
        playerStatsLabel.setFont(smallFont);

        playerHpBar = new JProgressBar();
        playerHpBar.setStringPainted(true);
        playerHpBar.setFont(smallFont);
        playerHpBar.setForeground(new Color(200, 70, 60));
        playerHpBar.setBackground(new Color(55, 35, 30));
        playerHpBar.setPreferredSize(new Dimension(400, 28));

        playerPortraitLabel = new JLabel();
        playerPortraitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        playerPortraitLabel.setPreferredSize(new Dimension(120, 120));
        playerPortraitLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(6, 6, 6, 6)
        ));

        JPanel playerInfo = new JPanel();
        playerInfo.setOpaque(false);
        playerInfo.setLayout(new BoxLayout(playerInfo, BoxLayout.Y_AXIS));
        playerInfo.add(statusTitle);
        playerInfo.add(playerNameLabel);
        playerInfo.add(Box.createVerticalStrut(8));
        playerInfo.add(playerHpBar);
        playerInfo.add(Box.createVerticalStrut(8));
        playerInfo.add(playerStatsLabel);

        topPanel.add(playerPortraitLabel, BorderLayout.WEST);
        topPanel.add(playerInfo, BorderLayout.CENTER);
        battlePanel.add(topPanel, BorderLayout.NORTH);

        // Center area: enemies + battle log
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        centerPanel.setOpaque(false);

        // Enemy side
        JPanel enemyContainer = new JPanel(new BorderLayout());
        enemyContainer.setBackground(new Color(36, 28, 20));
        enemyContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel enemyTitle = new JLabel("ENEMIES", SwingConstants.CENTER);
        enemyTitle.setForeground(new Color(240, 220, 180));
        enemyTitle.setFont(bodyFont);
        enemyTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        enemyPanel = new JPanel();
        enemyPanel.setOpaque(false);
        enemyPanel.setLayout(new GridLayout(0, 2, 12, 12));

        JScrollPane enemyScroll = new JScrollPane(enemyPanel);
        enemyScroll.setBorder(null);
        enemyScroll.getViewport().setBackground(new Color(36, 28, 20));

        enemyContainer.add(enemyTitle, BorderLayout.NORTH);
        enemyContainer.add(enemyScroll, BorderLayout.CENTER);

        // Log side
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setBackground(new Color(36, 28, 20));
        logContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel logTitle = new JLabel("BATTLE LOG", SwingConstants.CENTER);
        logTitle.setForeground(new Color(240, 220, 180));
        logTitle.setFont(bodyFont);
        logTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBackground(new Color(20, 16, 12));
        battleLog.setForeground(new Color(190, 220, 180));
        battleLog.setFont(smallFont);
        battleLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(null);

        logContainer.add(logTitle, BorderLayout.NORTH);
        logContainer.add(logScroll, BorderLayout.CENTER);

        centerPanel.add(enemyContainer);
        centerPanel.add(logContainer);

        resultPanel = new JPanel();
        resultPanel.setBackground(new Color(28, 22, 16));
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(24, 24, 24, 24)
        ));
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        resultTitleLabel = new JLabel("VICTORY!", SwingConstants.CENTER);
        resultTitleLabel.setForeground(new Color(240, 220, 180));
        resultTitleLabel.setFont(titleFont);
        resultTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultMessageLabel = new JLabel("You defeated all enemies!", SwingConstants.CENTER);
        resultMessageLabel.setForeground(Color.LIGHT_GRAY);
        resultMessageLabel.setFont(bodyFont);
        resultMessageLabel.setBorder(new EmptyBorder(12, 0, 20, 0));
        resultMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resultReturnButton = createPixelButton("RETURN TO SETUP");
        resultReturnButton.addActionListener(e -> {
            battleCenterLayout.show(battleCenterPanel, "BATTLE");
            actionContainer.setVisible(true);
            audioPlayer.playLoop(MENU_BGM_PATH);
            cardLayout.show(rootPanel, "SETUP");
        });

        JButton resultCloseButton = createPixelButton("CLOSE GAME");
        resultCloseButton.addActionListener(e -> {
            audioPlayer.stop();
            dispose();
        });

        JPanel resultButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        resultButtons.setOpaque(false);
        resultButtons.add(resultReturnButton);
        resultButtons.add(resultCloseButton);

        resultPanel.add(resultTitleLabel);
        resultPanel.add(resultMessageLabel);
        resultPanel.add(resultButtons);

        itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(new Color(28, 22, 16));
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel itemTitle = new JLabel("CHOOSE AN ITEM", SwingConstants.CENTER);
        itemTitle.setForeground(new Color(240, 220, 180));
        itemTitle.setFont(bodyFont);
        itemTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        itemPanel.add(itemTitle, BorderLayout.NORTH);

        itemGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        itemGrid.setOpaque(false);
        itemPanel.add(itemGrid, BorderLayout.CENTER);

        JButton itemBackButton = createPixelButton("BACK");
        itemBackButton.addActionListener(e -> {
            battleCenterLayout.show(battleCenterPanel, "BATTLE");
            actionContainer.setVisible(true);
        });

        JPanel itemBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        itemBottom.setOpaque(false);
        itemBottom.add(itemBackButton);
        itemPanel.add(itemBottom, BorderLayout.SOUTH);

        battleCenterLayout = new CardLayout();
        battleCenterPanel = new JPanel(battleCenterLayout);
        battleCenterPanel.setOpaque(false);
        battleCenterPanel.add(centerPanel, "BATTLE");
        battleCenterPanel.add(resultPanel, "RESULT");
        battleCenterPanel.add(itemPanel, "ITEM");

        battlePanel.add(battleCenterPanel, BorderLayout.CENTER);

        // Bottom area: 4 action buttons
        actionContainer = new JPanel(new BorderLayout());
        actionContainer.setOpaque(false);

        JLabel commandTitle = new JLabel("COMMAND", SwingConstants.CENTER);
        commandTitle.setForeground(new Color(240, 220, 180));
        commandTitle.setFont(bodyFont);
        commandTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

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

        actionContainer.add(commandTitle, BorderLayout.NORTH);
        actionContainer.add(actionPanel, BorderLayout.CENTER);
        battlePanel.add(actionContainer, BorderLayout.SOUTH);

        enablePlayerActions(false);
    }

    // ============================================================
    // Start the game after setup
    // ============================================================
    private void startGameFromSetup() {
        int playerChoice = selectedClassIndex + 1;

        int[] items = new int[] {
                selectedItem1Index + 1,
                selectedItem2Index + 1
        };

        Difficulty difficulty;
        switch (selectedDifficultyIndex) {
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
        battleCenterLayout.show(battleCenterPanel, "BATTLE");
        actionContainer.setVisible(true);
        audioPlayer.playLoop(BATTLE_BGM_PATH);
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
        updatePlayerPortrait(player);
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
                enemyCard.setBackground(new Color(42, 32, 22));
                enemyCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                        new EmptyBorder(10, 10, 10, 10)
                ));

                JLabel sprite = createSpriteLabel(getEnemyImagePath(enemy), getEnemyAscii(enemy));
                sprite.setForeground(new Color(240, 220, 180));
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
        itemGrid.removeAll();

        for (int i = 0; i < inventoryItems.size(); i++) {
            Items item = inventoryItems.get(i);
            ImageIcon icon = getItemIcon(item.getName());
            JButton button = createSelectionButton(item.getName(), icon);
            final int index = i;
            button.addActionListener(e -> {
                Items chosenItem = inventoryItems.get(index);

                if ("Power Stone".equals(chosenItem.getName()) && currentContext.getPlayer() instanceof Warrior) {
                    pendingPowerStone = true;
                    actionMode = ActionMode.ITEM_POWERSTONE_TARGET;
                    appendLog("[SYSTEM] Power Stone selected. Click an enemy to trigger Shield Bash without cooldown.");
                } else {
                    actionMode = ActionMode.NONE;
                    pendingPowerStone = false;
                    engine.playerUseItem(index);
                }

                battleCenterLayout.show(battleCenterPanel, "BATTLE");
                actionContainer.setVisible(true);
            });
            itemGrid.add(button);
        }

        itemGrid.revalidate();
        itemGrid.repaint();
        actionContainer.setVisible(false);
        battleCenterLayout.show(battleCenterPanel, "ITEM");
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

        if (victory) {
            resultTitleLabel.setText("VICTORY!");
            resultMessageLabel.setText("You defeated all enemies!");
            appendLog("[SYSTEM] VICTORY!");
        } else {
            resultTitleLabel.setText("DEFEAT...");
            resultMessageLabel.setText("Your hero has fallen.");
            appendLog("[SYSTEM] DEFEAT...");
        }

        actionContainer.setVisible(false);
        battleCenterLayout.show(battleCenterPanel, "RESULT");
    }

    // ============================================================
    // Styling helpers
    // ============================================================
    private JPanel createInfoCard(String title, String content) {
        return createInfoCard(title, content, null);
    }

    private JPanel createInfoCard(String title, String content, List<JLabel> images) {
        return createInfoCard(title, content, images, false);
    }

    private JPanel createInfoCard(String title, String content, List<JLabel> images, boolean verticalImages) {
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

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        if (images != null && !images.isEmpty()) {
            JPanel imagePanel = new JPanel();
            imagePanel.setOpaque(false);
            if (verticalImages) {
                imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
            } else {
                imagePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
            }
            for (int i = 0; i < images.size(); i++) {
                JLabel imageLabel = images.get(i);
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                imagePanel.add(imageLabel);
                if (verticalImages && i < images.size() - 1) {
                    imagePanel.add(Box.createVerticalStrut(8));
                }
            }
            imagePanel.setBorder(new EmptyBorder(0, 0, 10, 0));
            body.add(imagePanel, BorderLayout.NORTH);
        }

        body.add(text, BorderLayout.CENTER);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createItemInfoCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(35, 35, 35));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel titleLabel = new JLabel("ITEMS", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(bodyFont);
        titleLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(createItemIntroRow(
                "GameAssets/images/Heal_Potion.png",
                "Heal Potion",
                "Heal for 100 HP"
        ));
        body.add(Box.createVerticalStrut(12));
        body.add(createItemIntroRow(
                "GameAssets/images/Power_Stone.png",
                "Power Stone",
                "Trigger your special once without cooldown"
        ));
        body.add(Box.createVerticalStrut(12));
        body.add(createItemIntroRow(
                "GameAssets/images/Smoke_Bomb.png",
                "Smoke Bomb",
                "Receive 0 damage for the next 2 turns"
        ));

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createItemIntroRow(String imagePath, String itemName, String description) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        JLabel iconLabel = loadImageLabel(imagePath, 56, 56);
        iconLabel.setPreferredSize(new Dimension(56, 56));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        row.add(iconLabel, BorderLayout.WEST);

        JTextArea text = new JTextArea(itemName + "\n- " + description);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(smallFont);
        text.setBackground(new Color(35, 35, 35));
        text.setForeground(Color.LIGHT_GRAY);
        row.add(text, BorderLayout.CENTER);

        return row;
    }

    private JPanel createSetupSection(String title) {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 200, 160), 2),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setForeground(new Color(240, 220, 180));
        label.setFont(bodyFont);
        label.setBorder(new EmptyBorder(0, 0, 10, 0));

        section.add(label, BorderLayout.NORTH);
        return section;
    }

    private JPanel createItemRow(String labelText, JButton[] buttons) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(220, 210, 190));
        label.setFont(smallFont);
        label.setPreferredSize(new Dimension(90, 24));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);
        for (JButton button : buttons) {
            buttonRow.add(button);
        }

        row.add(label, BorderLayout.WEST);
        row.add(buttonRow, BorderLayout.CENTER);
        return row;
    }

    private JButton createSelectionButton(String text, ImageIcon icon) {
        JButton button = new JButton(text, icon);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(smallFont);
        button.setFocusPainted(false);
        button.setBackground(new Color(40, 32, 24));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 80), 2));
        button.setPreferredSize(new Dimension(140, 140));
        return button;
    }

    private void updateSelectionGroup(JButton[] buttons, int selectedIndex) {
        for (int i = 0; i < buttons.length; i++) {
            if (i == selectedIndex) {
                buttons[i].setBorder(BorderFactory.createLineBorder(new Color(230, 200, 120), 3));
                buttons[i].setBackground(new Color(70, 50, 32));
            } else {
                buttons[i].setBorder(BorderFactory.createLineBorder(new Color(120, 100, 80), 2));
                buttons[i].setBackground(new Color(40, 32, 24));
            }
        }
    }

    private ImageIcon createBadgeIcon(String text, Color fill, Color textColor) {
        int size = 56;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(4, 4, size - 8, size - 8, 12, 12);
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawRoundRect(4, 4, size - 8, size - 8, 12, 12);
        g2.setColor(textColor);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));

        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - 4);
        g2.dispose();
        return new ImageIcon(image);
    }

    private ImageIcon getItemIcon(String itemName) {
        String path = getItemImagePath(itemName);
        ImageIcon icon = loadImageIcon(path, 72, 72);
        if (icon != null) {
            return icon;
        }

        if ("Heal Potion".equals(itemName)) {
            return createBadgeIcon("HP", new Color(150, 40, 40), new Color(255, 235, 210));
        } else if ("Power Stone".equals(itemName)) {
            return createBadgeIcon("PS", new Color(40, 70, 140), new Color(230, 235, 255));
        } else if ("Smoke Bomb".equals(itemName)) {
            return createBadgeIcon("SM", new Color(70, 70, 70), new Color(240, 240, 240));
        }
        return createBadgeIcon("?", new Color(80, 80, 80), new Color(240, 240, 240));
    }

    private String getItemImagePath(String itemName) {
        if ("Heal Potion".equals(itemName)) {
            return "GameAssets/images/Heal_Potion.png";
        } else if ("Power Stone".equals(itemName)) {
            return "GameAssets/images/Power_Stone.png";
        } else if ("Smoke Bomb".equals(itemName)) {
            return "GameAssets/images/Smoke_Bomb.png";
        }
        return null;
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

    private JLabel loadImageLabel(String path, int width, int height) {
        ImageIcon icon = loadImageIcon(path, width, height);
        if (icon != null) {
            return new JLabel(icon);
        }
        JLabel fallback = new JLabel("[missing]");
        fallback.setForeground(Color.LIGHT_GRAY);
        fallback.setFont(smallFont);
        return fallback;
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

    private String getEnemyImagePath(Enemy enemy) {
        String name = enemy.getName().toLowerCase();
        if (name.contains("goblin")) {
            return "GameAssets/images/Goblin.png";
        } else if (name.contains("wolf")) {
            return "GameAssets/images/Wolf.png";
        }
        return null;
    }

    private void updatePlayerPortrait(Player player) {
        String path = null;
        if (player instanceof Warrior) {
            path = "GameAssets/images/Warrior.png";
        } else if (player instanceof Wizard) {
            path = "GameAssets/images/Wizard.png";
        }

        ImageIcon icon = loadImageIcon(path, 108, 108);
        if (icon != null) {
            playerPortraitLabel.setIcon(icon);
            playerPortraitLabel.setText("");
        } else {
            playerPortraitLabel.setIcon(null);
            playerPortraitLabel.setText("[no image]");
            playerPortraitLabel.setForeground(Color.LIGHT_GRAY);
            playerPortraitLabel.setFont(smallFont);
        }
    }

    private JLabel createSpriteLabel(String path, String fallbackText) {
        JLabel label = new JLabel(fallbackText, SwingConstants.CENTER);
        ImageIcon icon = loadImageIcon(path, 96, 96);
        if (icon != null) {
            label.setText("");
            label.setIcon(icon);
        }
        return label;
    }

    private JPanel createBackgroundPanel(LayoutManager layout) {
        return new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    super.paintComponent(g);
                }
            }
        };
    }

    private Image loadRawImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (Exception e) {
            return null;
        }
    }

    private ImageIcon loadImageIcon(String path, int width, int height) {
        if (path == null) return null;
        try {
            BufferedImage image = ImageIO.read(new File(path));
            if (image != null) {
                return new ImageIcon(scaleToFit(image, width, height));
            }
        } catch (Exception e) {
            // Ignore and let caller handle fallback.
        }
        return null;
    }

    private Image scaleToFit(BufferedImage image, int maxWidth, int maxHeight) {
        double scale = Math.min((double) maxWidth / image.getWidth(), (double) maxHeight / image.getHeight());
        scale = Math.min(scale, 1.0);

        int scaledWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int scaledHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));

        BufferedImage canvas = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int x = (maxWidth - scaledWidth) / 2;
        int y = (maxHeight - scaledHeight) / 2;
        g2.drawImage(image, x, y, scaledWidth, scaledHeight, null);
        g2.dispose();
        return canvas;
    }

    /**
     * Loads the bundled pixel font from GameAssets if available.
     * Falls back to Monospaced if the font file cannot be loaded.
     */
    private Font loadPixelFont(float size) {
        try {
            Font base = Font.createFont(Font.TRUETYPE_FONT, new File("GameAssets/fonts/Minecraft.ttf"));
            return base.deriveFont(size);
        } catch (Exception e) {
            // Ignore and use fallback font.
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
