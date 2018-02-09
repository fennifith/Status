package com.james.status.services;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.james.status.R;
import com.james.status.data.PreferenceData;
import com.james.status.utils.StaticUtils;

@TargetApi(24)
public class QuickToggleService extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setIcon(Icon.createWithResource(this, StaticUtils.isStatusServiceRunning(this) ? R.drawable.ic_check_box_enabled : R.drawable.ic_check_box_disabled));
            tile.updateTile();
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setIcon(Icon.createWithResource(this, StaticUtils.isStatusServiceRunning(this) ? R.drawable.ic_check_box_enabled : R.drawable.ic_check_box_disabled));
            tile.updateTile();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile != null) {
            if (StaticUtils.isStatusServiceRunning(this)) {
                PreferenceData.STATUS_ENABLED.setValue(this, false);

                Intent intent = new Intent(StatusService.ACTION_STOP);
                intent.setClass(this, StatusService.class);
                stopService(intent);

                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_check_box_disabled));
            } else if (StaticUtils.isReady(this)) {
                PreferenceData.STATUS_ENABLED.setValue(this, true);

                Intent intent = new Intent(StatusService.ACTION_START);
                intent.setClass(this, StatusService.class);
                startService(intent);

                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_check_box_enabled));
            }

            tile.updateTile();
        }
    }
}
