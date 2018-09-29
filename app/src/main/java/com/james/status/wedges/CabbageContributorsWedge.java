package com.james.status.wedges;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.james.status.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.jfenn.attribouter.wedges.ContributorsWedge;

public class CabbageContributorsWedge extends ContributorsWedge {

    public CabbageContributorsWedge(XmlResourceParser parser) throws XmlPullParserException, IOException {
        super(parser);
    }

    @Override
    public void bind(Context context, final ViewHolder viewHolder) {
        super.bind(context, viewHolder);
        viewHolder.itemView.findViewById(R.id.first).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Glide.with(v.getContext()).load("https://jfenn.me/images/headers/cabbage.jpg").into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        ImageView imageView = viewHolder.itemView.findViewById(R.id.firstImage);
                        if (imageView != null)
                            imageView.setImageDrawable(resource);
                    }
                });

                return false;
            }
        });
    }
}
