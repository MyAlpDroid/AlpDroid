package com.alpdroid.huGen10;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TitleExtractor implements MetadataTransform {

    private static final String[] SEPARATORS =
            new String[] {" -- ", "--", " - ", " – ", " — ", "-", "–", "—", ":", "|", "///"};

    @Override
    public Track transform(Track track) {
        String title = null;
        String artist = null;

        for (String separator : SEPARATORS) {
            String[] components = track.track().split(Pattern.quote(separator));

            if (components.length > 1) {
                String[] titleComponents = Arrays.copyOfRange(components, 1, components.length);

                artist = components[0];
                title = Joiner.on(separator).join(titleComponents);
                break;
            }
        }

        if (title == null || artist == null) {
            return track;
        }

        title = title.trim();
        artist = artist.trim();

        return track.toBuilder().artist(artist).track(title).build();
    }
}