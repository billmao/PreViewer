package com.ktouch.kdc.launcher4.draglistview;

import com.ktouch.kdc.launcher4.R;
import com.mobeta.android.dslv.DragSortController;

import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mobeta.android.dslv.DragSortController;

/**
 * Sets drag init mode on DSLV controller passed into ctor.
 */
public class DragInitModeDialog extends DialogFragment {

    private DragSortController mControl;

    private int mDragInitMode;

    private DragOkListener mListener;

    public DragInitModeDialog() {
        super();
        mDragInitMode = DragSortController.ON_DOWN;
    }

    public DragInitModeDialog(int dragStartMode) {
        super();
        mDragInitMode = dragStartMode;
    }

    public interface DragOkListener {
        public void onDragOkClick(int removeMode);
    }

    public void setDragOkListener(DragOkListener l) {
        mListener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        String[] artistNames = {"drag_init_mode_labels1","drag_init_mode_labels2","drag_init_mode_labels3"};
        builder.setTitle("remove model")
                .setSingleChoiceItems(artistNames, mDragInitMode,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDragInitMode = which;
                            }
                        })
                // Set the action buttons
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onDragOkClick(mDragInitMode);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
    
        return builder.create();
    }
}
