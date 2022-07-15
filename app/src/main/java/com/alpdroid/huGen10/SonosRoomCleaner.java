package com.alpdroid.huGen10;


/**
 * Fixes artist names emitted by Sonos being prefixed by the room name.
 */
public class SonosRoomCleaner implements MetadataTransform {

    @Override
    public Track transform(Track track) {
        if (track.composer().isPresent()) {
            return track.toBuilder().artist(track.composer().get()).build();
        } else {
            return track;
        }
    }
}