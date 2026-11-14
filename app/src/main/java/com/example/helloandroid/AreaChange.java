package com.example.helloandroid;

import android.widget.ImageView;

public class AreaChange {

    public void changeArea(
            ImageView mapView,
            int mapResourceId
    ) {
        mapView.setImageResource(mapResourceId);
        mapView.invalidate();    // ← ★再描画を即時実行
        mapView.requestLayout(); // ← ★レイアウト更新も要求
    }
}