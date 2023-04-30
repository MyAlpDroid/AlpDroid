package com.alpdroid.huGen10;

public class MyTunerProTitleCleaner  implements MetadataTransform {

/**
 * Fixes artist names emitted by MyTunerPro including DEL character
 *
 */

    @Override
    public Track transform(Track track) {
        return track.toBuilder().artist(track.artist().replaceAll("[^\\x1F-\\x7E]", "")).build();
    }
}