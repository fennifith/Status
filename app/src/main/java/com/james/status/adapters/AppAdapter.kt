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

package com.james.status.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.james.status.R
import com.james.status.data.AppPreferenceData
import com.james.status.dialogs.preference.AppPreferenceDialog
import com.james.status.utils.StringUtils
import com.james.status.utils.tasks.AppIconGetterTask
import com.james.status.views.CustomImageView

/**
 * An adapter used to show multiple app / activity items so
 * that the user can choose one to change the settings for.
 */
class AppAdapter(private val context: Context, private val apps: MutableList<AppPreferenceData>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private val packageManager: PackageManager = context.packageManager

    init {
        apps.sortWith(Comparator { lhs, rhs ->
            val label1 = lhs.getLabel(this.context)
            val label2 = rhs.getLabel(this.context)

            if (label1 != null && label2 != null)
                label1.compareTo(label2, ignoreCase = true)
            else 0
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_card, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getApp(position) ?: return

        holder.name.text = app.getLabel(holder.name.context)
        holder.packageName.text = app.name

        holder.icon.setImageDrawable(ColorDrawable(Color.TRANSPARENT))

        val iconTask = AppIconGetterTask(packageManager, object : AppIconGetterTask.OnGottenListener {
            override fun onGotten(drawable: Drawable?) {
                drawable?.let {
                    holder.icon.setImageDrawable(it);
                }
            }
        })

        getApp(holder.adapterPosition)?.let {
            iconTask.execute(it)
        }

        holder.itemView.setOnClickListener { v ->
            getApp(holder.adapterPosition)?.let {
                AppPreferenceDialog(v.context, it).show()
            }
        }
    }

    private fun getApp(position: Int): AppPreferenceData? {
        return with(apps) {
            if (position < 0 || position >= size)
            null
            else this[position]
        }
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    /**
     * Filter the items in the list by a a specified
     * string. Passing `null` to this method should
     * reset the filter and display all items in their
     * original order.
     */
    fun filter(string: String?) {
        if (string == null || string.isEmpty()) {
            apps.sortWith(Comparator { lhs, rhs ->
                val label1 = lhs.getLabel(this.context)
                val label2 = rhs.getLabel(this.context)

                if (label1 != null && label2 != null)
                   label1.compareTo(label2, ignoreCase = true)
                else 0
            })
        } else {
            apps.sortWith(Comparator { lhs, rhs ->
                var value = 0

                val label1 = lhs.getLabel(this.context)
                val label2 = rhs.getLabel(this.context)
                if (label1 != null && label2 != null) {
                    value += StringUtils.difference(label1.toLowerCase(), string).length
                    value -= StringUtils.difference(label2.toLowerCase(), string).length
                }

                value += StringUtils.difference(lhs.componentName, string).length
                value -= StringUtils.difference(rhs.componentName, string).length

                value
            })
        }

        notifyDataSetChanged()
    }

    /**
     * Class to hold the views for a particular list
     * item.
     */
    class ViewHolder(internal var v: View) : RecyclerView.ViewHolder(v) {
        internal var name: TextView = v.findViewById(R.id.appName)
        internal var packageName: TextView = v.findViewById(R.id.appPackage)
        internal var icon: CustomImageView = v.findViewById(R.id.icon)
        internal var more: View = v.findViewById(R.id.more)
    }
}
