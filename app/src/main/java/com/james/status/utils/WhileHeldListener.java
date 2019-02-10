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

package com.james.status.utils;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public abstract class WhileHeldListener implements View.OnTouchListener, Runnable {

    private Handler handler;
    private int interval;

    public WhileHeldListener() {
        interval = 100;
    }

    public WhileHeldListener(int interval) {
        this.interval = interval;
    }

    public abstract void onHeld();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (handler != null)
                    return true;
                handler = new Handler();
                handler.post(this);
                break;
            case MotionEvent.ACTION_UP:
                if (handler == null)
                    return true;
                handler.removeCallbacks(this);
                handler = null;
                break;
        }
        return false;
    }

    @Override
    public void run() {
        onHeld();
        if (handler != null)
            handler.postDelayed(this, interval);
    }

}
