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
