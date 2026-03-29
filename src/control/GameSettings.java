package control;

public class GameSettings {
    private int playerChoice; //Store the choice of player
    private int[] initialItemChoice;
    private Difficulty difficulty;

    public GameSettings(int playerChoice, int[] initialItemChoice, Difficulty difficulty) {
        this.playerChoice = playerChoice;
        this.initialItemChoice = initialItemChoice;
        this.difficulty = difficulty;
    }

    public int getPlayerChoice() {
        return playerChoice;
    }

    public int[] getInitialItemChoice() {
        return initialItemChoice;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

}
