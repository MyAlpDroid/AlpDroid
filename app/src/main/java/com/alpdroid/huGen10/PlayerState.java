package com.alpdroid.huGen10;

import android.media.session.PlaybackState;
import android.util.Log;

import com.google.common.eventbus.EventBus;

public class PlayerState {

    private static final String TAG = PlayerState.class.getName();

    private final String player;
    private final AlpdroidEr alpdroidEr;
    private final EventBus eventBus = AlpdroidApplication.Companion.getEventBus();
    private PlaybackItem playbackItem;

    public PlayerState(
            String player, AlpdroidEr alpdroidEr) {
        this.player = player;
        this.alpdroidEr = alpdroidEr;
        eventBus.register(this);
    }

    public void setPlaybackState(PlaybackState playbackState) {
        if (playbackItem == null) {
            return;
        }

        playbackItem.updateAmountPlayed();

        int state = playbackState.getState();
        boolean isPlaying = state == PlaybackState.STATE_PLAYING;

        if (isPlaying) {
            postEvent(playbackItem.getTrack());
            playbackItem.startPlaying();
        } else {
            postEvent(Track.empty());
            playbackItem.stopPlaying();
            alpdroidEr.submit(playbackItem);
        }
    }

    public void setTrack(Track track) {
       Track currentTrack = null;
        boolean isPlaying = false;
        long now = System.currentTimeMillis();


     if (playbackItem != null) {
            currentTrack = playbackItem.getTrack();
            isPlaying = playbackItem.isPlaying();
        }

        if (track.isSameTrack(currentTrack)) {
            // Update track in PlaybackItem, as this new one probably has updated details/more keys.
            playbackItem.setTrack(track);
        } else {

            if (playbackItem != null) {
                playbackItem.stopPlaying();
                alpdroidEr.submit(playbackItem);
            }

            playbackItem = new PlaybackItem(track, now);
        }

      if (isPlaying) {
            postEvent(track);
            alpdroidEr.updateNowPlaying(track);
            playbackItem.startPlaying();
            alpdroidEr.submit(playbackItem);
        }
    }

    private void postEvent(Track track) {
        eventBus.post(NowPlayingChangeEvent.builder().track(track).source(player).build());
    }

}
