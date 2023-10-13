package com.alpdroid.huGen10.ui;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.alpdroid.huGen10.AlpdroidApplication;
import com.alpdroid.huGen10.NowPlayingChangeEvent;
import com.alpdroid.huGen10.R;
import com.alpdroid.huGen10.Track;
import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NowPlayingFragment extends Fragment {



    public NowPlayingFragment() {
        //do something

    }

    private final EventBus eventBus = new EventBus();
    private  final String TAG = NowPlayingFragment.class.getName();

    private ViewGroup detailGroup;
    private ImageView artImageView;
    private TextView titleTextView;
    private TextView artistTextView;
    private TextView nothingPlayingTextView;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_now_playing, container, false);
        detailGroup = rootView.findViewById(R.id.now_playing_detail);
        artImageView = rootView.findViewById(R.id.now_playing_art);
        titleTextView = rootView.findViewById(R.id.now_playing_title);
        artistTextView = rootView.findViewById(R.id.now_playing_artist);
        nothingPlayingTextView = rootView.findViewById(R.id.now_playing_nothing_playing);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlpdroidApplication.Companion.getEventBus().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        onNowPlayingChange(AlpdroidApplication.Companion.getLastNowPlayingChangeEvent());
    }

    @Override
    public void onStop() {
        AlpdroidApplication.Companion.getEventBus().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onNowPlayingChange(NowPlayingChangeEvent event) {
        Track track = event.track();
        Optional<Bitmap> art = track.art();

        if (track.isValid()) {
            String artistText = track.artist();
            if (track.album().isPresent()) {
                artistText = String.format("%s — %s", track.artist(), track.album().get());
            }

            titleTextView.setText(track.track());
            artistTextView.setText(artistText);

            if (art.isPresent()) {
                artImageView.setImageBitmap(art.get());
            } else {
                try {
                    Drawable icon = getActivity().getPackageManager().getApplicationIcon(event.source());
                    artImageView.setImageDrawable(icon);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.d(TAG, "Failed to read application icon for player", e);
                }
            }

            detailGroup.setVisibility(View.VISIBLE);
            nothingPlayingTextView.setVisibility(View.GONE);
        } else {
            detailGroup.setVisibility(View.GONE);
            nothingPlayingTextView.setVisibility(View.VISIBLE);
        }
    }
}
