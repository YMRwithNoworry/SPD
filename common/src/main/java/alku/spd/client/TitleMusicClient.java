package alku.spd.client;

import alku.spd.registry.SpdSounds;
import dev.architectury.event.events.client.ClientTickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

@Environment(EnvType.CLIENT)
public final class TitleMusicClient {
    private static TitleMusicSound titleMusic;
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
        if (minecraft.screen instanceof TitleScreen) {
            minecraft.getMusicManager().stopPlaying();
            if (titleMusic == null || titleMusic.isStopped() || !minecraft.getSoundManager().isActive(titleMusic)) {
                titleMusic = new TitleMusicSound();
                minecraft.getSoundManager().play(titleMusic);
            }
            return;
        }

        if (titleMusic != null) {
            minecraft.getSoundManager().stop(titleMusic);
            titleMusic.finish();
            titleMusic = null;
        }
    }

    private static final class TitleMusicSound extends AbstractTickableSoundInstance {
        private TitleMusicSound() {
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
            if (!(minecraft.screen instanceof TitleScreen)) {
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
