package com.alpdroid.huGen10;

import android.media.session.PlaybackState;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.util.Timer;

public class PlayerState {

    private static final String TAG = PlayerState.class.getName();

    private final String player;
    private final AlpdroidEr alpdroidEr;
 //   private final AlpdroidNotificationManager notificationManager;
    private final EventBus eventBus = AlpdroidApplication.Companion.getEventBus();
    private PlaybackItem playbackItem;
    private Timer submissionTimer;

    public PlayerState(
            String player, AlpdroidEr alpdroidEr) {
  //, AlpdroidNotificationManager notificationManager
        this.player = player;
        this.alpdroidEr = alpdroidEr;
        //this.notificationManager = notificationManager;
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
            Log.d(TAG, "Track playing");
            postEvent(playbackItem.getTrack());
            playbackItem.startPlaying();
    //* Mod        notificationManager.updateNowPlaying(playbackItem.getTrack());
    //* Mod        scheduleSubmission();
        } else {
            Log.d(TAG, String.format("Track paused (state %d)", state));
            postEvent(Track.empty());
            playbackItem.stopPlaying();
      //* Mod      notificationManager.removeNowPlaying();
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
            Log.d(TAG, String.format("Track metadata updated: %s", track));

            // Update track in PlaybackItem, as this new one probably has updated details/more keys.
            playbackItem.setTrack(track);
        } else {
            Log.d(TAG, String.format("Changed track: %s", track));

            if (playbackItem != null) {
                playbackItem.stopPlaying();
                alpdroidEr.submit(playbackItem);
            }

            playbackItem = new PlaybackItem(track, now);
        }

      if (isPlaying) {
            postEvent(track);
            alpdroidEr.updateNowPlaying(track);
    //* Mod        notificationManager.updateNowPlaying(track);
            playbackItem.startPlaying();
            alpdroidEr.submit(playbackItem);
        }
    }

 //   private void scheduleSubmission() {
 //       Log.d(TAG, "Audio submission");
//    }

    private void postEvent(Track track) {
        eventBus.post(NowPlayingChangeEvent.builder().track(track).source(player).build());
    }

}
