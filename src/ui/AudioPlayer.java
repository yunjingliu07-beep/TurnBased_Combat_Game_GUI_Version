package ui;

import java.io.File;

public class AudioPlayer {
    private Object mediaPlayer;

    public void playLoop(String path) {
        stop();
        try {
            String uri = new File(path).toURI().toString();
            Class<?> platformClass = Class.forName("javafx.application.Platform");
            try {
                platformClass.getMethod("startup", Runnable.class).invoke(null, (Runnable) () -> {});
            } catch (Exception ignored) {
                // JavaFX may already be initialized.
            }

            Class<?> mediaClass = Class.forName("javafx.scene.media.Media");
            Class<?> mediaPlayerClass = Class.forName("javafx.scene.media.MediaPlayer");

            Object media = mediaClass.getConstructor(String.class).newInstance(uri);
            Object player = mediaPlayerClass.getConstructor(mediaClass).newInstance(media);

            int indefinite = mediaPlayerClass.getField("INDEFINITE").getInt(null);
            mediaPlayerClass.getMethod("setCycleCount", int.class).invoke(player, indefinite);
            mediaPlayerClass.getMethod("play").invoke(player);

            mediaPlayer = player;
        } catch (Exception e) {
            // Ignore if JavaFX is unavailable; game should still run without audio.
        }
    }

    public void stop() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.getClass().getMethod("stop").invoke(mediaPlayer);
            mediaPlayer.getClass().getMethod("dispose").invoke(mediaPlayer);
        } catch (Exception ignored) {
        } finally {
            mediaPlayer = null;
        }
    }
}
