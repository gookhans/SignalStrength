package com.example.electra;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoAdapter implements GoogleMap.InfoWindowAdapter {
    private Activity context;

    public InfoAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = context.getLayoutInflater().inflate(R.layout.info_window, null);

        TextView tvTitle = view.findViewById(R.id.information);
       // TextView tvSubTitle = (TextView) view.findViewById(R.id.tv_subtitle);

        tvTitle.setText(marker.getTitle());
      //  tvSubTitle.setText(marker.getSnippet());

        return view;
    }
}
