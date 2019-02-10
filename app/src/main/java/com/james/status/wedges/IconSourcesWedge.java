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
import android.view.View;

import com.james.status.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.jfenn.attribouter.adapters.InfoAdapter;
import me.jfenn.attribouter.wedges.Wedge;

public class IconSourcesWedge extends Wedge<IconSourcesWedge.ViewHolder> {

    public IconSourcesWedge(XmlResourceParser parser) {
        super(R.layout.wedge_icon_sources);

        try {
            Method method = Wedge.class.getDeclaredMethod("addChildren", XmlResourceParser.class);
            method.setAccessible(true);
            method.invoke(this, parser);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public void bind(Context context, ViewHolder viewHolder) {
        viewHolder.recycler.setLayoutManager(new LinearLayoutManager(context));
        viewHolder.recycler.setAdapter(new InfoAdapter(getChildren()));
    }

    public static class ViewHolder extends Wedge.ViewHolder {

        private RecyclerView recycler;

        public ViewHolder(View v) {
            super(v);
            recycler = v.findViewById(R.id.recycler);
        }
    }

}
