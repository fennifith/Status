package com.james.status.data.icon;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@TargetApi(18)
public class NfcIconData extends IconData<NfcIconData.NfcReceiver> {

    private NfcManager manager;

    public NfcIconData(Context context) {
        super(context);
        manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
    }

    @Override
    public NfcReceiver getReceiver() {
        return new NfcReceiver(this);
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
            onDrawableUpdate(0);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_nfc);
    }

    @Override
    public int getIconStyleSize() {
        return 1;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_nfc
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_radial),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_nfc_radial"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_square),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_nfc_square"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_tap),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_nfc_tap"
                        )
                )
        );

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_nfc)
        };
    }

    static class NfcReceiver extends IconUpdateReceiver<NfcIconData> {

        private NfcReceiver(NfcIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(NfcIconData icon, Intent intent) {
            switch (intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF)) {
                case NfcAdapter.STATE_OFF:
                case NfcAdapter.STATE_TURNING_OFF:
                    icon.onDrawableUpdate(-1);
                    break;
                case NfcAdapter.STATE_ON:
                case NfcAdapter.STATE_TURNING_ON:
                    icon.onDrawableUpdate(0);
                    break;
            }
        }
    }
}
