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

package com.james.status.wedges

import android.content.Context
import android.content.res.XmlResourceParser
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.james.status.R
import me.jfenn.attribouter.adapters.InfoAdapter
import me.jfenn.attribouter.wedges.Wedge

class IconSourcesWedge(parser: XmlResourceParser) : Wedge<IconSourcesWedge.ViewHolder>(R.layout.wedge_icon_sources) {

    init {
        addChildren(parser)
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun bind(context: Context, viewHolder: ViewHolder) {
        viewHolder.recycler.layoutManager = LinearLayoutManager(context)
        viewHolder.recycler.adapter = InfoAdapter(children)
    }

    class ViewHolder(v: View) : Wedge.ViewHolder(v) {
        val recycler: RecyclerView = v.findViewById(R.id.recycler)
    }

}
