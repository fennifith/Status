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

package com.james.status.activities

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.james.status.BuildConfig
import com.james.status.R
import com.james.status.Status
import com.james.status.data.PreferenceData
import me.jfenn.attribouter.Attribouter
import me.jfenn.attribouter.fragments.AboutFragment

class AttribouterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(Status.Theme.ACTIVITY_ATTRIBOUTER.getTheme(this))
        setContentView(R.layout.activity_attribouter_about)

        val color = if (PreferenceData.PREF_DARK_THEME.getValue(this)) Color.WHITE else Color.BLACK

        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            setTitleTextColor(color)
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

            VectorDrawableCompat.create(resources, R.drawable.ic_attribouter_arrow_back, theme)?.let { drawable ->
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                setHomeAsUpIndicator(drawable)
            }
        }

        val bundle = Bundle().apply { putString(Attribouter.EXTRA_GITHUB_OAUTH_TOKEN, BuildConfig.GITHUB_TOKEN) }
        val fragment = AboutFragment().apply { arguments = bundle }

        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().add(R.id.fragment, fragment).commit()
        else
            supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(Bundle(), outPersistentState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(Bundle())
    }
}
