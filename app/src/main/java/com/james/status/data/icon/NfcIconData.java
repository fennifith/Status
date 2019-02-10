/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
            onIconUpdate(0);
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
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_radial),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_mdi_nfc_radial
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_square),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_icons8_nfc_square
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_tap),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_mdi_nfc_tap
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
                    icon.onIconUpdate(-1);
                    break;
                case NfcAdapter.STATE_ON:
                case NfcAdapter.STATE_TURNING_ON:
                    icon.onIconUpdate(0);
                    break;
            }
        }
    }
}
