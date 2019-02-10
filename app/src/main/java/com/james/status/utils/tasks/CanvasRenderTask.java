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

package com.james.status.utils.tasks;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;

public class CanvasRenderTask extends AsyncTask<Integer, Void, Bitmap> {

    private final WeakReference<Renderable> reference;

    public CanvasRenderTask(Renderable renderable) {
        reference = new WeakReference<>(renderable);
    }

    @Override
    protected Bitmap doInBackground(Integer... integers) {
        if (integers[0] == null || integers[0] < 1 || integers[1] == null || integers[1] < 1)
            return null;

        Bitmap bitmap = Bitmap.createBitmap(integers[0], integers[1], Bitmap.Config.ARGB_8888);
        Renderable renderable = reference.get();
        if (renderable != null)
            renderable.render(new Canvas(bitmap));

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Renderable renderable = reference.get();
        if (renderable != null)
            renderable.onRendered(bitmap);
    }

    public interface Renderable {
        void render(Canvas canvas);

        void onRendered(@Nullable Bitmap bitmap);
    }

}
