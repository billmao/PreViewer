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

package com.ktouch.kdc.launcher4.app;

import com.ktouch.kdc.launcher4.data.DataManager;
import com.ktouch.kdc.launcher4.data.DownloadCache;
import com.ktouch.kdc.launcher4.data.ImageCacheService;
import com.ktouch.kdc.launcher4.util.ThreadPool;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;

public interface GalleryApp {
    public DataManager getDataManager();
    public ImageCacheService getImageCacheService();
    public DownloadCache getDownloadCache();
    public ThreadPool getThreadPool();

    public Context getAndroidContext();
    public Looper getMainLooper();
    public ContentResolver getContentResolver();
    public Resources getResources();
}
