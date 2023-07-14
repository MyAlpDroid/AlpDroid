package com.alpdroid.huGen10;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.eventbus.EventBus;

import com.alpdroid.huGen10.AlpdroidApplication;

public class AlpdroidEr {

  private static final String TAG = AlpdroidEr.class.getName();

  private final ConnectivityManager connectivityManager;


  public AlpdroidEr(
      ConnectivityManager connectivityManager) {
    this.connectivityManager = connectivityManager;

    EventBus eventBus = AlpdroidApplication.Companion.getEventBus();
    eventBus.register(this);
  }


  public void updateNowPlaying(Track track) {

    @SuppressLint("MissingPermission") NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

  }



  public void submit(PlaybackItem playbackItem) {
    // Set final value for amount played, in case it was playing up until now.
    playbackItem.updateAmountPlayed();

    Track track = playbackItem.getTrack();


    long playTime = playbackItem.getAmountPlayed();


  }




}
