package alku.spd.client;

import alku.spd.registry.SpdSounds;
import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public final class TitleMusicClient {
    private static NonWorldMusicSound nonWorldMusic;
    private static boolean registered;

    private TitleMusicClient() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvent.CLIENT_POST.register(TitleMusicClient::tick);
    }

    private static void tick(Minecraft minecraft) {
        if (shouldPlayBreathingMusic(minecraft)) {
            minecraft.getMusicManager().stopPlaying();
            if (nonWorldMusic == null || nonWorldMusic.isStopped() || !minecraft.getSoundManager().isActive(nonWorldMusic)) {
                nonWorldMusic = new NonWorldMusicSound();
                minecraft.getSoundManager().play(nonWorldMusic);
            }
            return;
        }

        if (nonWorldMusic != null) {
            minecraft.getSoundManager().stop(nonWorldMusic);
            nonWorldMusic.finish();
            nonWorldMusic = null;
        }
    }

    private static boolean shouldPlayBreathingMusic(Minecraft minecraft) {
        return minecraft.level == null;
    }

    private static final class NonWorldMusicSound extends AbstractTickableSoundInstance {
        private NonWorldMusicSound() {
            super(SpdSounds.BREATHING_MUSIC.get(), SoundSource.MUSIC, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.82F;
            this.pitch = 1.0F;
        }

        @Override
        public void tick() {
            Minecraft minecraft = Minecraft.getInstance();
            if (!shouldPlayBreathingMusic(minecraft)) {
                finish();
                return;
            }
            minecraft.getMusicManager().stopPlaying();
        }

        private void finish() {
            stop();
        }
    }
}
