package com.james.status.dialogs;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.activities.ImagePickerActivity;
import com.james.status.data.IconStyleData;

public class IconCreatorDialog extends AppCompatDialog {

    private Status status;
    private OnIconStyleListener listener;
    private int size;
    private String[] names = new String[0];

    private String name;
    private String[] paths = new String[0];

    private EditText editText;

    public IconCreatorDialog(Context context, int size, String[] names) {
        super(context, R.style.AppTheme_Dialog);
        setTitle(R.string.action_edit_style);

        status = (Status) context.getApplicationContext();

        this.size = size;
        if (names != null) this.names = names;

        paths = new String[size];
    }

    public IconCreatorDialog(Context context, IconStyleData style, String[] names) {
        super(context, R.style.AppTheme_Dialog);
        setTitle(R.string.action_edit_style);

        status = (Status) context.getApplicationContext();

        if (names != null) this.names = names;
        size = style.getSize();

        name = style.name;
        paths = style.path;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_icon_creator);

        editText = (EditText) findViewById(R.id.name);
        if (name != null) editText.setText(name);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name = editText.getText().toString();
                for (String string : names) {
                    if (string.equalsIgnoreCase(name)) {
                        name = null;
                        editText.setError(getContext().getString(R.string.error_name_exists));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
        for (int i = 0; i < size; i++) {
            final View v = LayoutInflater.from(getContext()).inflate(R.layout.item_icon_create, null);
            ((TextView) v.findViewById(R.id.number)).setText(String.valueOf(i + 1));
            if (paths[i] != null) {
                Drawable drawable = null;
                try {
                    drawable = Drawable.createFromPath(paths[i]);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }

                ((ImageView) v.findViewById(R.id.image)).setImageDrawable(drawable);
            }

            final int something = i;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    status.addListener(new Status.OnActivityResultListener() {
                        @Override
                        public void onActivityResult(int requestCode, int resultCode, Intent data) {
                            if (requestCode == ImagePickerActivity.ACTION_PICK_IMAGE && resultCode == ImagePickerActivity.RESULT_OK) {
                                String path = data.getDataString();
                                try {
                                    Cursor cursor = getContext().getContentResolver().query(data.getData(), null, null, null, null);
                                    String documentId;
                                    if (cursor != null) {
                                        cursor.moveToFirst();
                                        documentId = cursor.getString(0);
                                        documentId = documentId.substring(documentId.lastIndexOf(":") + 1);
                                        cursor.close();
                                    } else return;

                                    cursor = getContext().getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{documentId}, null);
                                    if (cursor != null) {
                                        cursor.moveToFirst();
                                        path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                        cursor.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                paths[something] = path;

                                Drawable drawable = null;
                                try {
                                    drawable = Drawable.createFromPath(path);
                                } catch (OutOfMemoryError e) {
                                    e.printStackTrace();
                                }

                                ((ImageView) v.findViewById(R.id.image)).setImageDrawable(drawable);
                            }

                            status.removeListener(this);
                        }
                    });

                    getContext().startActivity(new Intent(getContext(), ImagePickerActivity.class));
                }
            });

            layout.addView(v);
        }

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name != null && paths != null) {
                    for (String path : paths) {
                        if (path == null) {
                            Toast.makeText(getContext(), R.string.error_missing_icons, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (listener != null) listener.onIconStyle(new IconStyleData(name, paths));
                    if (isShowing()) dismiss();
                } else {
                    if (name == null)
                        editText.setError(getContext().getString(R.string.error_no_text_name));
                    else
                        Toast.makeText(getContext(), R.string.error_missing_icons, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isShowing()) dismiss();
            }
        });
    }

    public IconCreatorDialog setListener(OnIconStyleListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnIconStyleListener {
        void onIconStyle(IconStyleData style);
    }

}
