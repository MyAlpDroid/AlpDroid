package com.alpdroid.huGen10;


import android.media.MediaMetadata;
import android.media.session.PlaybackState;

import java.util.HashMap;
import java.util.Map;

public class PlaybackTracker {

    private final AlpdroidEr alpdroidEr;
    private final MetadataTransformers metadataTransformers = new MetadataTransformers();
    private final Map<String, PlayerState> playerStates = new HashMap<>();
    public static int playerType=0;



    public PlaybackTracker(
     AlpdroidEr alpdroidEr) {
        this.alpdroidEr = alpdroidEr;
    }


    public void handlePlaybackStateChange(String player, PlaybackState playbackState) {
        if (playbackState == null) {
            return;
        }

        PlayerState playerState = getOrCreatePlayerState(player);
        playerState.setPlaybackState(playbackState);

    }


    public void handleMetadataChange(String player, MediaMetadata metadata) {
        if (metadata == null) {
            return;
        }

        Track track =
                metadataTransformers.transformForPackageName(player, Track.fromMediaMetadata(metadata));

        if (!track.isValid()) {
            return;
        }

        PlayerState playerState = getOrCreatePlayerState(player);
        playerState.setTrack(track);
    }

    public void handleSessionTermination(String player) {
        PlayerState playerState = getOrCreatePlayerState(player);
        PlaybackState playbackState =
                new PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1)
                        .build();
        playerState.setPlaybackState(playbackState);
    }

    private PlayerState getOrCreatePlayerState(String player) {
        PlayerState playerState = playerStates.get(player);

        if (!playerStates.containsKey(player)) {
            playerState = new PlayerState(player, alpdroidEr);
            playerStates.put(player, playerState);
        }

        return playerState;
    }
}