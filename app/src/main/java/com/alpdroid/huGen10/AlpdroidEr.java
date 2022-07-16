package com.alpdroid.huGen10;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.eventbus.EventBus;

public class AlpdroidEr {

  private static final String TAG = AlpdroidEr.class.getName();
  private static final int MINIMUM_PLAYING_TIME = 30 * 1000;

  private final ConnectivityManager connectivityManager;

  private final EventBus eventBus = AlpdroidApplication.Companion.getEventBus();


  public AlpdroidEr(
      ConnectivityManager connectivityManager) {
    this.connectivityManager = connectivityManager;

    // TODO write unit test to ensure non-network plays get scrobbled with duration lookup.

    eventBus.register(this);
  }

  public void updateNowPlaying(Track track) {

    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

  }



  public void submit(PlaybackItem playbackItem) {
    // Set final value for amount played, in case it was playing up until now.
    playbackItem.updateAmountPlayed();

    // Generate one scrobble per played period.
    Track track = playbackItem.getTrack();


    long timestamp = playbackItem.getTimestamp();
    long duration = 0;

    if (track.duration().isPresent()) {
      duration = track.duration().get();
    }

    long playTime = playbackItem.getAmountPlayed();

    if (playTime < 1) {
      return;
    }

    if (duration == 0) {
      duration = playTime;
    }

    int playCount = (int) (playTime / duration);

    if (duration < MINIMUM_PLAYING_TIME) {
      return;
    }

//     for (int i = playbackItem.getPlaysScrobbled(); i < playCount; i++) {
     int itemTimestamp = (int) ((timestamp + duration) / 1000);

    Alpdroid alpdroid = Alpdroid.builder().track(track).timestamp(itemTimestamp).build();

 //   }


  }


  /*
  public void fetchTrackDurationAndSubmit(final PlaybackItem playbackItem) {
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


    Track track = playbackItem.getTrack();

          Log.d(TAG, String.format("Track info updated: %s", playbackItem));

          submit(playbackItem);
          return ;

  }*/



/*  public void alpdroidPending() {
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    return ;
  }
*/


}
