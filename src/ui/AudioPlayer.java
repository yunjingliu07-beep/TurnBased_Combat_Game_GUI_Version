package ui;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class AudioPlayer {
    private Clip clip;

    public void playLoop(String path) {
        stop();
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(path));
            Clip newClip = AudioSystem.getClip();
            newClip.open(stream);
            newClip.loop(Clip.LOOP_CONTINUOUSLY);
            newClip.start();
            clip = newClip;
        } catch (Exception ignored) {
            // Ignore if audio cannot be loaded; game should still run without audio.
        }
    }

    public void stop() {
        if (clip == null) return;
        clip.stop();
        clip.close();
        clip = null;
    }
}
