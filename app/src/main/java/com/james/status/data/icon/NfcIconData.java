package com.james.status.data.icon;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(18)
public class NfcIconData extends IconData<NfcIconData.NfcReceiver> {

    NfcManager manager;

    public NfcIconData(Context context) {
        super(context);
        manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
    }

    @Override
    public NfcReceiver getReceiver() {
        return new NfcReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
    }

    @Override
    public void register() {
        super.register();

        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled())
            onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
    }

    @Override
    public int[] getDefaultIconResource() {
        return new int[]{R.drawable.ic_nfc};
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_nfc);
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = new ArrayList<>();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                R.drawable.ic_nfc
                        )
                )
        );

        return styles;
    }

    public class NfcReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)) {
                case NfcAdapter.STATE_OFF:
                case NfcAdapter.STATE_TURNING_OFF:
                    onDrawableUpdate(null);
                    break;
                case NfcAdapter.STATE_ON:
                case NfcAdapter.STATE_TURNING_ON:
                    onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
                    break;
            }
        }
    }
}
