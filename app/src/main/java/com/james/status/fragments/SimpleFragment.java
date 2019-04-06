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

package com.james.status.fragments;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class SimpleFragment extends Fragment {

    public abstract String getTitle(Context context);

    public abstract void filter(@Nullable String filter);

    public void onSelect() {
        // called when a fragment is visible
    }

    public void onEnterScroll(float offset) {
        // called when the fragment is being entered
    }

    public void onExitScroll(float offset) {
        // called when the fragment is being exited
    }
}
