package com.samlanning.core.server.behaviours;

import org.bff.javampd.player.Player.Status;
import org.bff.javampd.song.MPDSong;
import org.slf4j.Logger;

import com.samlanning.core.server.hue.HueLightStates.LightState;
import com.samlanning.core.server.hue.HueLightStates.LightStateListener;
import com.samlanning.core.server.lighting.RGBLightValue;
import com.samlanning.core.server.lighting.LightingControl.LightFlash;
import com.samlanning.core.server.mpd.MPDMonitor.Listener;
import com.samlanning.core.server.switchboard.ServerSwitchboard;
import com.samlanning.core.server.util.Logging;
import com.samlanning.synesthesia.database.dummy.DummyTrackDatabase;
import com.samlanning.synesthesia.player.EventHandler;
import com.samlanning.synesthesia.player.SynesthesiaPlayer;
import com.samlanning.synesthesia.player.TrackPreprocessor;

public class Behaviours {

    private static final Logger log = Logging.logger(Behaviours.class);

    public static void initialise(final ServerSwitchboard switchboard) {
        log.info("Initialising Behaviours");
        
        final SynesthesiaPlayer synesthesia = new SynesthesiaPlayer(new DummyTrackDatabase());
        
        // Add a handler to synesthesia to make the lights flash
        EventHandler<LightFlash> lightFlashHandler = new EventHandler<LightFlash>() {

            @Override
            public void handle(LightFlash event) {
                switchboard.lighting().handleLightFlash(event);
            }

            @Override
            public void started() {
                switchboard.lighting().setUsingSynesthesia(true);
                System.out.println("SYNTH STARTED");
            }

            @Override
            public void stopped() {
                switchboard.lighting().setUsingSynesthesia(false);
                System.out.println("SYNTH STOPPED");
            }
            
        };
        TrackPreprocessor<LightFlash> trackPreprocessor = new TrackPreprocessor<LightFlash>() {

            @Override
            public LightFlash processMarker() {
                return new LightFlash(100, new RGBLightValue(0x0, 0xff, 0xff));
            }
            
        };
        synesthesia.addHandler(trackPreprocessor, lightFlashHandler);

        // Listen to MPD
        switchboard.listenToMPD(new Listener() {
            
            private MPDSong song;

            @Override
            public void statusChanged(Status status, long songStartTime) {
                switch (status) {
                    case STATUS_PLAYING:
                        switchboard.lighting().setStatePlayingMusic(songStartTime);
                        sendSongToSynesthesia(songStartTime);
                        break;
                    case STATUS_PAUSED:
                    case STATUS_STOPPED:
                        switchboard.lighting().setStateStatic();
                        synesthesia.stop();
                        break;
                }
            }

            @Override
            public void songChanged(MPDSong song, long songStartTime) {
                this.song = song;
                sendSongToSynesthesia(songStartTime);
            }
            
            private void sendSongToSynesthesia(long songStartTime) {
                if (song != null)
                    synesthesia.setCurrentSong(songStartTime, song.getName(), song.getArtistName(),
                        song.getAlbumName());
            }

        });
    }

    /**
     * Watch the given hue light, and use it to set the color of the lighting
     */
    public static void watchLight(ServerSwitchboard switchboard, String watchLight) {
        log.info("Watching hue light: " + watchLight);
        switchboard.hue().addLightStateListener(new LightStateListener() {
            
            @Override
            public void lightChanged(String key, LightState newState) {
                if(!key.equals(watchLight))
                    return;
                if (newState.on) {
                    switchboard.lighting().setStaticBrightness(1f);
                    switchboard.lighting().setColor(newState.color);
                } else {
                    // Turn Brightness down to 0
                    switchboard.lighting().setStaticBrightness(0f);
                }
            }
        });
    }

}
