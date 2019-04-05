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
import android.widget.TextView
import com.james.status.R
import com.james.status.utils.UrlClickListener
import me.jfenn.attribouter.utils.ResourceUtils
import me.jfenn.attribouter.wedges.Wedge

class IconSourceWedge(parser: XmlResourceParser) : Wedge<IconSourceWedge.ViewHolder>(R.layout.wedge_icon_source) {

    private val name: String = parser.getAttributeValue(null, "name")
    private val description: String = parser.getAttributeValue(null, "description")
    private val url: String = parser.getAttributeValue(null, "url")

    init {
        addChildren(parser)
    }

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun bind(context: Context, viewHolder: ViewHolder) {
        viewHolder.sourceName.text = ResourceUtils.getString(context, name)
        viewHolder.sourceDesc.text = ResourceUtils.getString(context, description)
        viewHolder.itemView.setOnClickListener(UrlClickListener(url))
    }

    class ViewHolder(v: View) : Wedge.ViewHolder(v) {
        val sourceName: TextView = v.findViewById(R.id.sourceName)
        val sourceDesc: TextView = v.findViewById(R.id.sourceDesc)
    }

}
