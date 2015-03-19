/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ktouch.kdc.launcher4.photoeditor.actions;

import android.content.Context;
import android.util.AttributeSet;

import com.ktouch.kdc.launcher4.photoeditor.filters.PosterizeFilter;

/**
 * An action handling the "Posterize" effect.
 */
public class PosterizeAction extends EffectAction {

    public PosterizeAction(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void doBegin() {
        notifyFilterChanged(new PosterizeFilter(), true);
        notifyDone();
    }

    @Override
    public void doEnd() {
    }
}