package com.alpdroid.huGen10.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.fragment.app.Fragment;

import com.alpdroid.huGen10.Alpdroid;
import com.alpdroid.huGen10.AlpdroidApplication;
import com.alpdroid.huGen10.R;

import java.util.ArrayList;
import java.util.List;

public class AlpdroidHistoryFragment extends Fragment {

  private ArrayAdapter adapter;
  private final List<Alpdroid> alpdroids = new ArrayList<>();
  private final LongSparseArray<Alpdroid> alpdroidMap = new LongSparseArray<>();

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_alpdroid_history, container, false);

   refreshData();

    adapter =
        new AlpdroidHistoryItemAdapter(
            getContext(), android.R.layout.simple_list_item_1, alpdroids);
    ListView listView = rootView.findViewById(R.id.scrobble_history_list_view);
    listView.setAdapter(adapter);

    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    AlpdroidApplication.Companion.getEventBus().register(this);
    refreshData();
  }

  @Override
  public void onPause() {
    super.onPause();
    AlpdroidApplication.Companion.getEventBus().unregister(this);
  }



  private void refreshData() {
    alpdroids.clear();
   for (Alpdroid alpdroid : alpdroids) {
      alpdroidMap.put(alpdroid.status().getDbId(), alpdroid);
    }
  }
}
