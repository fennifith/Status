package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.IconStyleData;

public class IconStyleDialog extends AppCompatDialog {

    private OnIconStyleListener listener;
    private int size;
    private String[] names = new String[0];

    private String name;
    private String[] path;

    public IconStyleDialog(Context context, int size, String[] names) {
        super(context, R.style.AppTheme_Dialog);
        this.size = size;
        if (names != null) this.names = names;

        path = new String[size];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_icon_style);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        for (int i = 0; i < size; i++) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon_picker, null);
            ((TextView) v.findViewById(R.id.number)).setText(String.valueOf(i + 1));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            layout.addView(v);
        }

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name != null && path != null) {
                    if (listener != null) listener.onIconStyle(new IconStyleData(name, path));
                    if (isShowing()) dismiss();
                }
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowing()) dismiss();
            }
        });
    }

    public IconStyleDialog setListener(OnIconStyleListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnIconStyleListener {
        void onIconStyle(IconStyleData style);
    }

}
