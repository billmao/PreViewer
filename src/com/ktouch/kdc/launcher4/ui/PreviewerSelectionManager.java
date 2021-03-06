package com.ktouch.kdc.launcher4.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import com.ktouch.kdc.launcher4.data.MediaItem;
import com.ktouch.kdc.launcher4.data.MediaSet;
import com.ktouch.kdc.launcher4.data.Path;
import com.ktouch.kdc.launcher4.ui.SelectionManager.SelectionListener;
//bill create
public class PreviewerSelectionManager {
	 @SuppressWarnings("unused")
	    private static final String TAG = "SelectionManager";

	    public static final int ENTER_SELECTION_MODE = 1;
	    public static final int LEAVE_SELECTION_MODE = 2;
	    public static final int SELECT_ALL_MODE = 3;

	    private Set<Path> mClickedSet;
	    private MediaSet mSourceMediaSet;
	    private SelectionListener mListener;
	    //private DataManager mDataManager;
	    private boolean mInverseSelection;
	    private boolean mIsAlbumSet;
	    private boolean mInSelectionMode;
	    private boolean mAutoLeave = true;
	    private int mTotal;
	    private Path mPressedPath;

	    public interface SelectionListener {
	        public void onSelectionModeChange(int mode);
	        public void onSelectionChange(Path path, boolean selected);
	    }
	    public interface AddAllItemToList {
	        public void onAddAllItemToList(Set<Path> mClickedSet);
	    }
	    public PreviewerSelectionManager(Context context, boolean isAlbumSet) {
//	        mDataManager = context.getApplicationContext().getAppDataManager();
	        mClickedSet = new HashSet<Path>();
	        mIsAlbumSet = isAlbumSet;
	        mTotal = -1;
	    }

	    // Whether we will leave selection mode automatically once the number of
	    // selected items is down to zero.
	    public void setAutoLeaveSelectionMode(boolean enable) {
	        mAutoLeave = enable;
	    }

	    public void setSelectionListener(SelectionListener listener) {
	        mListener = listener;
	    }

	    public void selectAll() {
	        mInverseSelection = true;
	        mClickedSet.clear();
	        enterSelectionMode();
	        if (mListener != null) mListener.onSelectionModeChange(SELECT_ALL_MODE);
	    }

	    public void deSelectAll() {
	        leaveSelectionMode();
	        mInverseSelection = false;
	        mClickedSet.clear();
	    }

	    public boolean inSelectAllMode() {
	        return mInverseSelection;
	    }

	    public boolean inSelectionMode() {
	        return mInSelectionMode;
	    }

	    public void enterSelectionMode() {
	        if (mInSelectionMode) return;

	        mInSelectionMode = true;
	        if (mListener != null) mListener.onSelectionModeChange(ENTER_SELECTION_MODE);
	    }

	    public void leaveSelectionMode() {
	        if (!mInSelectionMode) return;

	        mInSelectionMode = false;
	        mInverseSelection = false;
	        mClickedSet.clear();
	        if (mListener != null) mListener.onSelectionModeChange(LEAVE_SELECTION_MODE);
	    }

	    public boolean isItemSelected(Path itemId) {
	        return mInverseSelection ^ mClickedSet.contains(itemId);
	    }

	    public int getSelectedCount() {
	        int count = mClickedSet.size();
	        if (mInverseSelection) {
	            if (mTotal < 0) {
	                mTotal = mIsAlbumSet
	                        ? mSourceMediaSet.getSubMediaSetCount()
	                        : mSourceMediaSet.getMediaItemCount();
	            }
	            count = mTotal - count;
	        }
	        return count;
	    }

	    public void toggle(Path path) {
	        if (mClickedSet.contains(path)) {
	            mClickedSet.remove(path);
	        } else {
	            enterSelectionMode();
	            mClickedSet.add(path);
	        }

	        if (mListener != null) mListener.onSelectionChange(path, isItemSelected(path));
	        if (getSelectedCount() == 0 && mAutoLeave) {
	            leaveSelectionMode();
	        }
	    }

	    public void setPressedPath(Path path) {
	        mPressedPath = path;
	    }

	    public boolean isPressedPath(Path path) {
	        return path != null && path == mPressedPath;
	    }

	    private static void expandMediaSet(ArrayList<Path> items, MediaSet set) {
	        int subCount = set.getSubMediaSetCount();
	        for (int i = 0; i < subCount; i++) {
	            expandMediaSet(items, set.getSubMediaSet(i));
	        }
	        int total = set.getMediaItemCount();
	        int batch = 50;
	        int index = 0;

	        while (index < total) {
	            int count = index + batch < total
	                    ? batch
	                    : total - index;
	            ArrayList<MediaItem> list = set.getMediaItem(index, count);
	            for (MediaItem item : list) {
	                items.add(item.getPath());
	            }
	            index += batch;
	        }
	    }

	    public ArrayList<Path> getSelected(boolean expandSet) {
	        ArrayList<Path> selected = new ArrayList<Path>();
	        if (mIsAlbumSet) {
	            if (mInverseSelection) {
	                int max = mSourceMediaSet.getSubMediaSetCount();
	                for (int i = 0; i < max; i++) {
	                    MediaSet set = mSourceMediaSet.getSubMediaSet(i);
	                    Path id = set.getPath();
	                    if (!mClickedSet.contains(id)) {
	                        if (expandSet) {
	                            expandMediaSet(selected, set);
	                        } else {
	                            selected.add(id);
	                        }
	                    }
	                }
	            } else {
	                for (Path id : mClickedSet) {
	                    if (expandSet) {
	                        //expandMediaSet(selected, mDataManager.getMediaSet(id));
	                    } else {
	                        selected.add(id);
	                    }
	                }
	            }
	        } else {
	            if (mInverseSelection) {

	                int total = mSourceMediaSet.getMediaItemCount();
	                int index = 0;
	                while (index < total) {
	                    int count = Math.min(total - index, MediaSet.MEDIAITEM_BATCH_FETCH_COUNT);
	                    ArrayList<MediaItem> list = mSourceMediaSet.getMediaItem(index, count);
	                    for (MediaItem item : list) {
	                        Path id = item.getPath();
	                        if (!mClickedSet.contains(id)) selected.add(id);
	                    }
	                    index += count;
	                }
	            } else {
	                for (Path id : mClickedSet) {
	                    selected.add(id);
	                }
	            }
	        }
	        return selected;
	    }

	    public void setSourceMediaSet(MediaSet set) {
	        mSourceMediaSet = set;
	        mTotal = -1;
	    }

	    public MediaSet getSourceMediaSet() {
	        return mSourceMediaSet;
	    }
	    
	    public Set<Path> getSelectItems(){
	    	return mClickedSet;
	    }
}
