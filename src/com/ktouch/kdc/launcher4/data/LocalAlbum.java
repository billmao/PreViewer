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

package com.ktouch.kdc.launcher4.data;

import com.ktouch.kdc.launcher4.app.GalleryApp;
import com.ktouch.kdc.launcher4.common.Utils;
import com.ktouch.kdc.launcher4.util.GalleryUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.view.WindowManager;

import java.util.ArrayList;

// LocalAlbumSet lists all media items in one bucket on local storage.
// The media items need to be all images or all videos, but not both.
public class LocalAlbum extends MediaSet {
    private static final String TAG = "LocalAlbum";
    private static final String[] COUNT_PROJECTION = { "count(*)" };

    private static final int INVALID_COUNT = -1;
    private final String mWhereClause;
    private final String mOrderClause;
    private final Uri mBaseUri;
    private final String[] mProjection;

    private final GalleryApp mApplication;
    private final ContentResolver mResolver;
    private final int mBucketId;
    private final String mBucketName;
    private final boolean mIsImage;
    private final ChangeNotifier mNotifier;
    private final Path mItemPath;
    private int mCachedCount = INVALID_COUNT;
    //bill add begin
    private WindowManager window;
    private int screenwidth = 0;
    private int screenHeight = 0;
    //bill add end
    public LocalAlbum(Path path, GalleryApp application, int bucketId,
            boolean isImage, String name) {
        super(path, nextVersionNumber());
        mApplication = application;
        mResolver = application.getContentResolver();
        mBucketId = bucketId;
        mBucketName = name;
        mIsImage = isImage;
		//初始化屏幕宽度和高度
		window = (WindowManager) application.getAndroidContext().getSystemService(Context.WINDOW_SERVICE);
		screenwidth = window.getDefaultDisplay().getWidth();
		screenHeight = window.getDefaultDisplay().getHeight();
        if (isImage) {
       	    //bill add begin  按屏幕大小过滤图片
       	    mWhereClause = ImageColumns.BUCKET_ID + " = ?";
//       	 	mWhereClause = ImageColumns.BUCKET_ID + " = ?"+" and "+MediaColumns.WIDTH +"" +
//      	 			" >="+screenwidth+" and "+MediaColumns.HEIGHT+" >="+screenHeight; //bill 暂时注释图片过滤
        	//mOrderClause = ImageColumns.DATE_TAKEN + " DESC, "
       	    mOrderClause = PlaylistsColumns.DATE_MODIFIED + " DESC, "	
                    + ImageColumns._ID + " DESC";
       	    //bill add end
            mBaseUri = Images.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalImage.PROJECTION;
            mItemPath = LocalImage.ITEM_PATH;
        } else {
            mWhereClause = VideoColumns.BUCKET_ID + " = ?";
            mOrderClause = VideoColumns.DATE_TAKEN + " DESC, "
                    + VideoColumns._ID + " DESC";
            mBaseUri = Video.Media.EXTERNAL_CONTENT_URI;
            mProjection = LocalVideo.PROJECTION;
            mItemPath = LocalVideo.ITEM_PATH;
        }

        mNotifier = new ChangeNotifier(this, mBaseUri, application);
    }

    public LocalAlbum(Path path, GalleryApp application, int bucketId,
            boolean isImage) {
        this(path, application, bucketId, isImage,
                LocalAlbumSet.getBucketName(application.getContentResolver(),
                bucketId));
    }

    @Override
    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        DataManager dataManager = mApplication.getDataManager();
        Uri uri = mBaseUri.buildUpon()
                .appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> list = new ArrayList<MediaItem>();
        GalleryUtils.assertNotInRenderThread();
        //bill add begin 图片过滤
        Cursor cursor = mResolver.query(
                uri, mProjection, mWhereClause,
               // mBaseUri, mProjection, mWhereClause,  //暂时注释掉图片过滤 bill 20150106
                new String[]{String.valueOf(mBucketId)},
                mOrderClause);
        if (cursor == null) {
            Log.w(TAG, "query fail: " + uri);
            return list;
        }

        try {
        	int num = 0;
        	int addSum = 0;
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column
                Path childPath = mItemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor,
                        dataManager, mApplication, mIsImage);
//bill add 片过滤代码暂时注释掉 20150106 begin
//				float scale = (float)((float)item.getHeight()/(float)item.getWidth());
//				float screenScale = (float)((float)screenHeight/(float)screenwidth);
//				if((screenScale*0.84<= scale && scale <=screenScale*1.15)){
//					num++;
//					if(num > start && num <start+count+1){
//						list.add(item);
//					}
//				}
//            //bill add end 图片过滤
//图片过滤暂时注释 20150106 end
                list.add(item);//原来代码
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    private static MediaItem loadOrUpdateItem(Path path, Cursor cursor,
            DataManager dataManager, GalleryApp app, boolean isImage) {
        LocalMediaItem item = (LocalMediaItem) dataManager.peekMediaObject(path);
        if (item == null) {
            if (isImage) {
                item = new LocalImage(path, app, cursor);
            } else {
                item = new LocalVideo(path, app, cursor);
            }
        } else {
            item.updateContent(cursor);
        }
        return item;
    }

    // The pids array are sorted by the (path) id.
    public static MediaItem[] getMediaItemById(
            GalleryApp application, boolean isImage, ArrayList<Integer> ids) {
        // get the lower and upper bound of (path) id
        MediaItem[] result = new MediaItem[ids.size()];
        if (ids.isEmpty()) return result;
        int idLow = ids.get(0);
        int idHigh = ids.get(ids.size() - 1);

        // prepare the query parameters
        Uri baseUri;
        String[] projection;
        Path itemPath;
        if (isImage) {
            baseUri = Images.Media.EXTERNAL_CONTENT_URI;
            projection = LocalImage.PROJECTION;
            itemPath = LocalImage.ITEM_PATH;
        } else {
            baseUri = Video.Media.EXTERNAL_CONTENT_URI;
            projection = LocalVideo.PROJECTION;
            itemPath = LocalVideo.ITEM_PATH;
        }

        ContentResolver resolver = application.getContentResolver();
        DataManager dataManager = application.getDataManager();
        Cursor cursor = resolver.query(baseUri, projection, "_id BETWEEN ? AND ?",
                new String[]{String.valueOf(idLow), String.valueOf(idHigh)},
                "_id");
        if (cursor == null) {
            Log.w(TAG, "query fail" + baseUri);
            return result;
        }
        try {
            int n = ids.size();
            int i = 0;

            while (i < n && cursor.moveToNext()) {
                int id = cursor.getInt(0);  // _id must be in the first column

                // Match id with the one on the ids list.
                if (ids.get(i) > id) {
                    continue;
                }

                while (ids.get(i) < id) {
                    if (++i >= n) {
                        return result;
                    }
                }

                Path childPath = itemPath.getChild(id);
                MediaItem item = loadOrUpdateItem(childPath, cursor, dataManager,
                        application, isImage);
                result[i] = item;
                ++i;
            }
            return result;
        } finally {
            cursor.close();
        }
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri,
            String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?",
                new String[]{String.valueOf(id)}, null);
    }

    @Override
    public int getMediaItemCount() {
    	//bill modify begin 图片过滤
    	DataManager dataManager = mApplication.getDataManager();
        if (mCachedCount == INVALID_COUNT) {
            Cursor cursor = mResolver.query(
                    mBaseUri, COUNT_PROJECTION, mWhereClause,
                    new String[]{String.valueOf(mBucketId)}, null);
            if (cursor == null) {
                Log.w(TAG, "query fail");
                return 0;
            }
            try {
                Utils.assertTrue(cursor.moveToNext());
                mCachedCount = cursor.getInt(0);
            } finally {
                cursor.close();
            }
        }
//bill add 以下是图片过滤，后来要求不再过滤图片暂时注释掉该代码
//        	 Cursor cursor = null;
//             if (mCachedCount == INVALID_COUNT) 
//             {
//                 cursor = mResolver.query(
//                 		mBaseUri, mProjection, mWhereClause,new String[]{String.valueOf(mBucketId)},
//                         mOrderClause);
//                 try {
//                 mCachedCount = 0;
//             	 while (cursor.moveToNext()) {
//                      int id = cursor.getInt(0);  // _id must be in the first column
//                      Path childPath = mItemPath.getChild(id);
//                      MediaItem item = loadOrUpdateItem(childPath, cursor,
//                              dataManager, mApplication, mIsImage);
//      				float scale = (float)((float)item.getHeight()/(float)item.getWidth());
//    				float screenScale = (float)((float)screenHeight/(float)screenwidth);
//    				if((screenScale*0.84<= scale && scale <=screenScale*1.15)){
//                    	  mCachedCount++;
//                      }
//                  }
//                 } finally {
//                     cursor.close();
//                 }
//             }
           //bill modify end 图片过滤
        return mCachedCount;
    }

    @Override
    public String getName() {
        return mBucketName;
    }

    @Override
    public long reload() {
        if (mNotifier.isDirty()) {
            mDataVersion = nextVersionNumber();
            mCachedCount = INVALID_COUNT;
        }
        return mDataVersion;
    }

    @Override
    public int getSupportedOperations() {
        return SUPPORT_DELETE | SUPPORT_SHARE | SUPPORT_INFO;
    }

    @Override
    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        mResolver.delete(mBaseUri, mWhereClause,
                new String[]{String.valueOf(mBucketId)});
    }

    @Override
    public boolean isLeafAlbum() {
        return true;
    }
}