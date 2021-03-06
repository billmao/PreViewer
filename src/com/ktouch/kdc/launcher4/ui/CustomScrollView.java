package com.ktouch.kdc.launcher4.ui;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import com.ktouch.kdc.launcher4.R;
import com.ktouch.kdc.launcher4.app.AlbumActivityDataAdapter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;

public class CustomScrollView extends FrameLayout 
        implements OnScrollListener, OnHierarchyChangeListener {

	// how much transparency to use for the fast scroll thumb
    private static final int ALPHA_MAX = 255;
    
    // how long before the fast scroll thumb disappears
    private static final long FADE_DURATION = 200;
	public static boolean fastScrollStatus = false;
    private Drawable mCurrentThumb;
    private Drawable mOverlayDrawable;

    private int mThumbH;
    private int mThumbW;
    private float mThumbY;

    private RectF mOverlayPos;

    // custom values I defined
    private int mOverlayWidth;
    private int mOverlayHeight;
    private float mOverlayTextSize;
    private int mOverlayScrollThumbWidth;

    private boolean mDragging;
    private se.emilsjolander.stickylistheaders.StickyListHeadersListView mList;
    private boolean mScrollCompleted;
    private boolean mThumbVisible;
    private boolean mOverlayVisible; 
    private int mVisibleItem =0;
    private Paint mPaint;

    private Object [] mSections;
    private String mSectionText;
    private boolean mDrawOverlay;
    private ScrollFade mScrollFade;
    private boolean isOverlayDrawed=false;

    private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1){
				invalidate();
			}
			return;
		}
    	
    };

    private AlbumActivityDataAdapter mListAdapter;

    private boolean mChangedBounds;
    private int lastTop = 0;
    private float scrollY = 0;
    private boolean dragFinishFlag = false;
    private int listCount = 0;
    private boolean thumbAndScroll = false;
    private boolean isStart300ms = false;
    private boolean isScrollListView = false;  //是否手动滑动listview控件，如果滑动开始滚动状态为1，结束为0；
    private boolean mSwitchFolder = false;     //表示是否切换首页的图片集合
    private int totalTop =0;
    private int totalHeight= 0;
    private boolean scrollBottom = false;
    private float density=0;
    private boolean upScroll=false;
    private boolean startAuim = false;
    private boolean endAuim = false;
    private int translateLen = 0;
    private boolean animFinish = true;
    private boolean isDrawingsOverlayAnim= false;
    private int topHeight =0;  //记录position为0的view移动高度
    public static interface SectionIndexer {
        Object[] getSections();

        int getPositionForSection(int section);

        int getSectionForPosition(int position);
    }


    public CustomScrollView(Context context) {
        super(context);

        init(context, null);
    }


    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs);
    }

    private void useThumbDrawable(Drawable drawable) {
        mCurrentThumb = drawable;
        mThumbW = mOverlayScrollThumbWidth;//mCurrentThumb.getIntrinsicWidth();
        mThumbH = mOverlayHeight;
        mChangedBounds = true;
    }

    private void init(Context context, AttributeSet attrs) {

        // set all attributes from xml
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.CustomFastScrollView);
            mOverlayHeight = typedArray.getDimensionPixelSize(
                    R.styleable.CustomFastScrollView_overlayHeight, 0);
            mOverlayWidth = typedArray.getDimensionPixelSize(
                    R.styleable.CustomFastScrollView_overlayWidth, 0);
            mOverlayTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.CustomFastScrollView_overlayTextSize, 0);
            mOverlayScrollThumbWidth = typedArray.getDimensionPixelSize(
                    R.styleable.CustomFastScrollView_overlayScrollThumbWidth, 0);

        }

        // Get both the scrollbar states drawables
        final Resources res = context.getResources();
        Drawable thumbDrawable = res.getDrawable(R.drawable.smoothbutton);
        useThumbDrawable(thumbDrawable);
        //mOverlayDrawable = res.getDrawable(android.R.drawable.alert_dark_frame);
        mOverlayDrawable = res.getDrawable(R.drawable.smoothuttontime);

        mScrollCompleted = true;
        setWillNotDraw(false);

        // Need to know when the ListView is added
        setOnHierarchyChangeListener(this);

        mOverlayPos = new RectF();
        mScrollFade = new ScrollFade();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mOverlayTextSize);
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		DisplayMetrics dm = new DisplayMetrics();  
		dm = context.getResources().getDisplayMetrics();  
		density  = dm.density;	
    }

    private void removeThumb() {
        mThumbVisible = false;
        // Draw one last time to remove thumb
        mDrawOverlay = false;  
        mOverlayVisible = false;
        invalidate();
    }
    public void dispalyOverlayAnim(Canvas canvas){
        final RectF pos = mOverlayPos;
        final int viewWidth = getWidth();
        pos.left = (viewWidth - translateLen);
        pos.right = pos.left + mOverlayWidth;
        pos.top = 0;
        pos.bottom = pos.top + mOverlayHeight;
        mOverlayDrawable.setBounds((int) pos.left, (int) pos.top,
                (int) pos.right, (int) pos.bottom);
       mOverlayDrawable.draw(canvas);
       final Paint paint = mPaint;
       float descent = paint.descent();
       final RectF rectF = mOverlayPos;
       if(mSectionText!=null){
           canvas.drawText(mSectionText, (int) (rectF.left + rectF.right) / 2+5,
                   (int) (rectF.bottom + rectF.top) / 2 + descent, paint); 
       }
    }
    public void displayOverlayDrawable(Canvas canvas){
    	
        final RectF pos = mOverlayPos;
        final int viewWidth = getWidth();
        if(!mThumbVisible){
            pos.left = (viewWidth - mOverlayWidth);
            pos.right = pos.left + mOverlayWidth;
        }
        else{
        	pos.left = (viewWidth - mOverlayWidth-translateLen);
            pos.right = pos.left + mOverlayWidth;  
        }
        pos.top = 0;
        pos.bottom = pos.top + mOverlayHeight;
        mOverlayDrawable.setBounds((int) pos.left, (int) pos.top,
                (int) pos.right, (int) pos.bottom);
       mOverlayDrawable.draw(canvas);
       final Paint paint = mPaint;
       float descent = paint.descent();
       final RectF rectF = mOverlayPos;
       if(mSectionText!=null){
           canvas.drawText(mSectionText, (int) (rectF.left + rectF.right) / 2+5,
                   (int) (rectF.bottom + rectF.top) / 2 + descent, paint); 
       }
       //拖动按钮滑动停止后，300s后Thumb图标消失
       if(mThumbVisible==true &&dragFinishFlag==true){
    	    //拖动按钮滑动停止后,在滑动listveiw Thumb图标不消失
    	    if(thumbAndScroll && !isStart300ms){
 	           mHandler.removeCallbacks(mScrollFade);
 	           mHandler.postDelayed(mScrollFade, 2000);
 	           isStart300ms = true;
    	    }
    	    else if(!thumbAndScroll){
    	       mHandler.removeCallbacks(mScrollFade);
  	           mHandler.postDelayed(mScrollFade, 2000);
    	    }
       }
    }
    public void displayDrawable(Canvas canvas){
        final RectF pos = mOverlayPos;
        final int viewWidth = getWidth();
    	pos.left = (viewWidth -translateLen);
        pos.right = pos.left + mOverlayWidth;  
        pos.top = 0;
        pos.bottom = pos.top + mOverlayHeight;
        mOverlayDrawable.setBounds((int) pos.left, (int) pos.top,
                (int) pos.right, (int) pos.bottom);
       mOverlayDrawable.draw(canvas);
       final Paint paint = mPaint;
       float descent = paint.descent();
       final RectF rectF = mOverlayPos;
       if(mSectionText!=null){
           canvas.drawText(mSectionText, (int) (rectF.left + rectF.right) / 2+5,
                   (int) (rectF.bottom + rectF.top) / 2 + descent, paint); 
       }
       if(translateLen==0){
    	   removeThumb();
       }
    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (!mOverlayVisible) {
          // No need to draw the rest
          return;
      }
        
        final int y = (int)mThumbY;
        final int viewWidth = getWidth();
        final CustomScrollView.ScrollFade scrollFade = mScrollFade;

        int alpha = -1;
        if(mThumbVisible){
        	//长按图片启动动画
 //       	if(startAuim){
        		mCurrentThumb.setBounds((viewWidth-translateLen), 0, viewWidth+mThumbW, mThumbH);
//        	}
//        	else
//        	{
//                mCurrentThumb.setBounds(viewWidth-mThumbW, 0, viewWidth+mThumbW, mThumbH);
//        	}
        }
        canvas.translate(0, y);
        if(mThumbVisible){
	        mCurrentThumb.draw(canvas); 
        }
        if (mDrawOverlay) 
        {
        	 if (scrollFade.mStarted) {
        		 if(mThumbVisible){
        			 displayOverlayDrawable(canvas);
        		 }
        		 else{
        			 displayDrawable(canvas);   //Overlay动画消失
        		 }
             }
        	 else
        	 {
        		 if(dragFinishFlag==true && mThumbVisible ==false){
                    mDragging = false;
                    mHandler.removeCallbacks(mScrollFade);
                    mHandler.postDelayed(mScrollFade, 2000);
        		 }
        		 //弹出Overlay动画
        		 if(isOverlayDrawed==false){
        			 dispalyOverlayAnim(canvas);
        		 }
        		 else{
        			 displayOverlayDrawable(canvas);
        		 }
        	 }
        }
        else if (alpha == 0) {
            scrollFade.mStarted = false;
            removeThumb();
        } else {
            invalidate(viewWidth - mThumbW, y, viewWidth, y + mThumbH);            
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCurrentThumb != null) {
            mCurrentThumb.setBounds(w - mThumbW, 0, w, mThumbH);
        }
        final RectF pos = mOverlayPos;
        pos.left = (w - mOverlayWidth);
        pos.right = pos.left + mOverlayWidth;
        pos.top = 0;
        pos.bottom = pos.top + mOverlayHeight;
        mOverlayDrawable.setBounds((int) pos.left, (int) pos.top,
                (int) pos.right, (int) pos.bottom);
    }
    
    public int getSectionIndex(float position,Object[] sections,int count){
        if (sections != null && sections.length > 1) {
            final int nSections = sections.length;
            int section = (int) (position * nSections);
            //int section=mListAdapter.getCurrentSection(position);
            if (section >= nSections) {
                section = nSections - 1;
            }
            int index = mListAdapter.getPositionForSection(section);
            int nextIndex = count;
            int prevIndex = index;
            int prevSection = section;
            int nextSection = section + 1;
            // Assume the next section is unique
            if (section < nSections - 1) {
                nextIndex = mListAdapter.getPositionForSection(section + 1);
            }

            // Find the previous index if we're slicing the previous section
            if (nextIndex == index) {
                // Non-existent letter
                while (section > 0) {
                    section--;
                     prevIndex = mListAdapter.getPositionForSection(section);
                     if (prevIndex != index) {
                         prevSection = section;
                        // sectionIndex = section;
                         break;
                     }
                }
            }

            int nextNextSection = nextSection + 1;
            while (nextNextSection < nSections &&
            		mListAdapter.getPositionForSection(nextNextSection) == nextIndex) {
                nextNextSection++;
                nextSection++;
            }

            float fPrev = (float) prevSection / nSections;
            float fNext = (float) nextSection / nSections;
            index = prevIndex + (int) ((nextIndex - prevIndex) * (position - fPrev) 
                    / (fNext - fPrev));
            // Don't overflow
            if (index > count - 1) index = count - 1;
            return index;
            //return section;
        }
        return -1;
    }
    
    public String getSectionText(float position){
    	String text = "";
    	mScrollCompleted = false;
    	if(mListAdapter!=null){
        	mSectionText = mListAdapter.getTimeText((int)position);
        }
    	text = mSectionText;
	   
    	mDrawOverlay = true;
    	return text;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    	
    	if(scrollState==1 && mSwitchFolder){
    		mSwitchFolder = false;
    		scrollBottom = false;
    		mThumbY = 0;
    		totalTop = 0; //解决切换相册时时间控件不在顶头位置
    	} 
      if(OnScrollListener.SCROLL_STATE_IDLE ==scrollState) {  
    	  View v=(mList).getListChildAt(0);
      	  int top = (v == null) ? 0 : v.getTop();  
//      	  View v2=(mList).getListChildAt(view.getCount()-1);
//     	  int bottom = (v2 == null) ? 0 : v2.getBottom();  
	    	// 判断滚动到顶部部    
	    	if (view.getFirstVisiblePosition() == 0 && view.getCount()>2 && upScroll && (-top)<10) {
	    		mThumbY = 1*density;
	    		invalidate();
	        }
	    	// 判断滚动到底部    
	    	else if (view.getLastVisiblePosition() == (view.getCount() - 1)&& view.getCount()>2 && !upScroll){
	    			//&& (-bottom)<10) {
	    		scrollBottom = true;
	    		mThumbY = getHeight() - mThumbH;
	    		invalidate();
	        }
  		}
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, 
            int totalItemCount) {
    	if(mSwitchFolder){
    		return;
    	}
    	int top = 0;
        if(totalItemCount - visibleItemCount >= 0 && !mDragging) 
        {
        	View v=(mList).getListChildAt(0);
        	top = (v == null) ? 0 : v.getTop();  
        	topHeight= top;  //bill 20150119
        	if(top<lastTop && listCount>0 && firstVisibleItem == mVisibleItem){
        		mThumbY=(float) ((totalTop+(-top))*(getHeight() - mThumbH)/(totalHeight-getHeight()));
         		if(mThumbY>(getHeight() - mThumbH)){
        			mThumbY =(getHeight() - mThumbH);
        		}
         		upScroll =false;
    		}else if(top>lastTop && listCount>0 && firstVisibleItem == mVisibleItem){
    			if(scrollBottom){
					scrollBottom = false;
				}
        		mThumbY=(float) ((totalTop+(-top))*(getHeight() - mThumbH)/(totalHeight-getHeight()));
        		if(mThumbY<0){
        			mThumbY =1*density;
        		}
        		upScroll = true;
    		}
    		else if(top>lastTop && listCount>0 && firstVisibleItem != mVisibleItem){

    			if(firstVisibleItem>mVisibleItem){
    				totalTop+=mListAdapter.getItemViewHeight(mVisibleItem);
    			}
    		}
    		else if(top<lastTop && listCount>0 && firstVisibleItem != mVisibleItem){
    			if(firstVisibleItem<mVisibleItem){
    				if(scrollBottom){
    					scrollBottom = false;
    				}
    				if(firstVisibleItem==0){
        				totalTop = 0;
        			}
    				else{
    					totalTop-=mListAdapter.getItemViewHeight(mVisibleItem);
    				}
    			}
    		}
    		lastTop = top;
            if(firstVisibleItem != mVisibleItem){
            	//mThumbY = totalTop;
            	mThumbY = (float) ((totalTop)*(getHeight() - mThumbH)/(totalHeight-getHeight()));
            }
           // if (mChangedBounds) 
            {
                final int viewWidth = getWidth();
                mCurrentThumb.setBounds(viewWidth - mThumbW, 0, viewWidth, mThumbH);
               // mChangedBounds = false;
                final RectF pos = mOverlayPos;
                if(!mThumbVisible){
	                pos.left = (viewWidth - mOverlayWidth);
	                pos.right = pos.left + mOverlayWidth;
                }
                else{
                    pos.left = (viewWidth - mOverlayWidth-mThumbW);
	                pos.right = pos.left + mOverlayWidth;  
                }
                pos.top = 0;
                pos.bottom = pos.top + mOverlayHeight;
                mOverlayDrawable.setBounds((int) pos.left, (int) pos.top,
                        (int) pos.right, (int) pos.bottom);
                getSections();  
                mSectionText = getSectionText(firstVisibleItem);
            }
        }
        mScrollCompleted = true;
        //滑动listview时启动Overlay动画
        if(!isOverlayDrawed && !isDrawingsOverlayAnim){
	        startAuim = true;
	        isDrawingsOverlayAnim =true;
			translateLen = 0;
	        Thread thread = new Thread(){
	        	public void run() {
	        		boolean flag = true;
	        		animFinish = false;
	                while(flag){
	                	translateLen+=1;
	                	if(translateLen>density*76){
	                		translateLen =(int) (density*76);
	                	}
	                	mHandler.sendEmptyMessage(1);
	                	try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                	flag = translateLen>=(density*76)?false:true;
	                }
	                translateLen = 0;
		    	    animFinish = true;
		    	    startAuim = false;
		    	    isDrawingsOverlayAnim = false;
		    	    isOverlayDrawed= true;
		    	    //动画结束后启动2s定时
		            if (!mDragging) {
		            	//mHandler.removeCallbacks(mScrollFade);
		                mHandler.postDelayed(mScrollFade, 2000);
		               // mScrollFade.mStarted = true;
		            }
				}
	        };
	        thread.start();
        }
        if(isOverlayDrawed){
	        if (firstVisibleItem == mVisibleItem) {
	        	invalidate();
	        }
        }
        mVisibleItem = firstVisibleItem;
        if (!mOverlayVisible || mScrollFade.mStarted) {
            mOverlayVisible = true;
            mOverlayDrawable.setAlpha(ALPHA_MAX);
        }
        //1218 begin在滑动listveiw Thumb图标不消失
        if(mThumbVisible!=true){
        	mHandler.removeCallbacks(mScrollFade);
        	mScrollFade.mStarted = false;
        }
        else if(mThumbVisible){
        	thumbAndScroll = true;
        	mScrollFade.mStarted = true;
        }
        //1218 end
        if (isOverlayDrawed&&!mDragging) {
            mHandler.postDelayed(mScrollFade, 2000);
        }

    }


    private void getSections() {
    	StickyListHeadersAdapter adapter = mList.getAdapter();
        mListAdapter = (AlbumActivityDataAdapter) adapter;
        if(adapter!=null){
            listCount = mListAdapter.getCount();
            mSections = ((AlbumActivityDataAdapter)adapter).getSections();
        }
		if(mListAdapter!=null){
			totalHeight=mListAdapter.getTotalHeight();
		}

    }

    public void onChildViewAdded(View parent, View child) {
        if (child instanceof se.emilsjolander.stickylistheaders.StickyListHeadersListView) {
            mList = (se.emilsjolander.stickylistheaders.StickyListHeadersListView)child;
            mList.setOnScrollListener(this);
            getSections();
        }
    }

    public void onChildViewRemoved(View parent, View child) {
        if (child == mList) {
            mList = null;
            mListAdapter = null;
            mSections = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	//点击事件被listview接收
    	//(mThumbVisible && dragFinishFlag)条件，当时间滑块快速滑动结束后，但是还没有执行mThumb消失动画时，此时又点按了overlay，
    	//则取消原来的动画，可继续快速拖动
    	View v=(mList).getListChildAt(0);
    	int top = (v == null) ? 0 : v.getTop();  
    	if( top==0 && !mOverlayVisible){
    		 return false;
    	}
    	if (!mThumbVisible||(mThumbVisible && dragFinishFlag) && ev.getAction() == MotionEvent.ACTION_DOWN) {

            if (ev.getX() > getWidth() - mOverlayWidth && ev.getY() >= mThumbY &&
                    ev.getY() <= mThumbY + mOverlayHeight) {
            	//mThumbVisible = true;
            	mThumbVisible = false;
                fastScrollStatus = true;
            	mCurrentThumb.setAlpha(ALPHA_MAX);
            	mHandler.removeCallbacks(mScrollFade);
            	//mDragging = true;
            	dragFinishFlag = false;   //1218 时间控件快速滑动结束后，在滑动listveiw Thumb图标不消失
            	final int viewHeight = getHeight();
  
                // If the previous scrollTo is still pending
            	if (mScrollCompleted) {
            		//scrollTo((float) mThumbY / (viewHeight - mThumbH));
            		scrollTo(mThumbY);
            	}
                return true;
            }   
        }
        return false;
    }

    private void scrollTo(float position) {
    	//该位置距离顶部的高度为
    	mScrollCompleted = false;
    	float height=(float) ((position*((236+230+27)*density*1.5*listCount-getHeight()))/(getHeight() - mThumbH));
    	int index1 = (int) (height/((236+230+27)*density*1.5));
        mList.setSelectionFromTop(index1, topHeight);
        if(mListAdapter!=null){
    		mSectionText = mListAdapter.getTimeText((int)index1);
    	}
//        int count = mList.getCount();
//        mScrollCompleted = false;
//        final Object[] sections = mSections;
//        View v=(mList).getListChildAt(0);
//    	  int top = (v == null) ? 0 : v.getTop();  
//        if (sections != null && sections.length > 1) {
//        	int index =getSectionIndex(position,sections,count);
//            mList.setSelectionFromTop(index, 0);
//         	if(mListAdapter!=null){
//        		mSectionText = mListAdapter.getTimeText((int)index);
//        	}
//        }
//        else {
//        
//            int index = (int) (position * count);
//         	if(mListAdapter!=null){
//        		mSectionText = mListAdapter.getTimeText((int)index);
//        	}
//            mList.setSelectionFromTop(index, 0);
//        }
        mDrawOverlay = true;
    }

    private void cancelFling() {
        // Cancel the list fling
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            if (me.getX() > getWidth() - mOverlayWidth-mThumbW
                    && me.getY() >= mThumbY 
                    && me.getY() <= mThumbY + mOverlayHeight) {
            	if(!animFinish){
                    if(fastScrollStatus==true){
                    	fastScrollStatus = false;
                    }
            		return true;
            	}
            	if(mThumbVisible && dragFinishFlag){
            		//当时间滑块快速滑动结束后，但是还没有执行mThumb消失动画时，此时又点按了overlay，则取消原来的动画，可继续快速拖动
            		mThumbVisible = true;
            		if(isStart300ms==true){
            			isStart300ms = false;
            		}
	                if (mListAdapter == null && mList != null) {
	                    getSections();
	                }
	
	                cancelFling();
	                if (mScrollCompleted) {
	                    //scrollTo((float) mThumbY / (getHeight() - mThumbH));
	                	scrollTo( mThumbY);
	                }
	                mDragging = true;
	                return true;
            	}
            	else if(mThumbVisible||(!mThumbVisible && fastScrollStatus)){
	                
            		mThumbVisible = true;
            		startAuim = true;
	                if (mListAdapter == null && mList != null) {
	                    getSections();
	                }
	
	                cancelFling();
	                if (mScrollCompleted) {
	                    //scrollTo((float) mThumbY / (getHeight() - mThumbH));
	                	scrollTo( mThumbY);
	                }
	                //点按overlay图片，mThumb动画开始从右侧动画弹出
	                startAuim = true;
	                Thread thread = new Thread(){
	                	public void run() {
	                		boolean flag = true;
	                		animFinish = false;
	    	                while(flag){
	    	                	translateLen+=1;
	    	                	if(translateLen>density*43){
	    	                		translateLen =(int) (density*43);
	    	                	}
	    	                	mHandler.sendEmptyMessage(1);
	    	                	try {
	    							Thread.sleep(1);
	    						} catch (InterruptedException e) {
	    							// TODO Auto-generated catch block
	    							e.printStackTrace();
	    						}
	    	                	flag = translateLen>=(density*43)?false:true;
	    	                }
	    	                animFinish= true;
						}
	                };
	                thread.start();
	                startAuim = false;
	                mDragging = true;
	                return true;
            	}
            	
            }
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            if (mDragging) {
            	dragFinishFlag = true;
                fastScrollStatus = false;
        		if(mThumbVisible==true){
        	        mDrawOverlay = true;  
        	        mOverlayVisible = true;
        		}
        		mDragging = false;  //1226速滑up后，mOverlay图片不会随上下滑动
        	   	View v=(mList).getListChildAt(0);
              	int top = (v == null) ? 0 : v.getTop(); 
        		totalTop=(int)((mThumbY*((236+230+27)*density*listCount-getHeight()))/(getHeight() - mThumbH))+top;

        		mScrollFade.mStarted = true; //1218 时间控件快速滑动结束后，在滑动listveiw Thumb图标不消失
        		invalidate();
                return true;
            }
            if(fastScrollStatus==true){
            	fastScrollStatus = false;
            	return true;
            }
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            if (mDragging) 
            {
            	mThumbVisible = true;
                final int viewHeight = getHeight();
                mThumbY = (int) me.getY() - mThumbH + 10;
                if (mThumbY < 0) {
                    mThumbY = 0;
                } else if (mThumbY + mThumbH > viewHeight) {
                    mThumbY = viewHeight - mThumbH;
                }
                // If the previous scrollTo is still pending
                if (mScrollCompleted) {
                    //scrollTo((float) mThumbY / (viewHeight - mThumbH));
                	scrollTo(mThumbY);
                }
                return true;
            }
        } 

        return super.onTouchEvent(me);
    }

    public class ScrollFade implements Runnable {

        long mStartTime;
        long mFadeDuration;
        boolean mStarted;

        void startFade() {
            mFadeDuration = FADE_DURATION;
            mStartTime = SystemClock.uptimeMillis();
            mStarted = true;
        }

        int getAlpha() {
            if (!mStarted) {
                return ALPHA_MAX;
            }
            int alpha;
            long now = SystemClock.uptimeMillis();
            if (now > mStartTime + mFadeDuration) {
                alpha = 0;
            } else {
                alpha = (int) (ALPHA_MAX - ((now - mStartTime) * ALPHA_MAX) / mFadeDuration); 
            }
            return alpha;
        }

        public void run() {
            if (!mStarted) {
                startFade();
                invalidate();
            }

            if (getAlpha() > 0) {
            	//mThumb图片消失2s后，执行Overlay图片开始执行消失动画
            	if(mThumbVisible==false && !mDragging){
	                final float y = mThumbY;
	                final int viewWidth = getWidth();
	                startAuim = true;
	        		translateLen = (int) (density*43);
	                Thread thread = new Thread(){
	               	public void run() {
	               		boolean flag = true;
	               		animFinish = false;
	                       while(flag){
	                       	translateLen-=1;
	                       	if(translateLen<0){
	                       		translateLen =0;
	                       	}
	                       	mHandler.sendEmptyMessage(1);
	                       	try {
	        						Thread.sleep(1);
	        					} catch (InterruptedException e) {
	        						// TODO Auto-generated catch block
	        						e.printStackTrace();
	        					}

	                       	flag = translateLen<=0?false:true;
	                       }
	                       animFinish = true;
	                       translateLen = 0;
	                	   isOverlayDrawed = false;
	        			}
	               };
	               thread.start();
            	}
                //invalidate(viewWidth - mThumbW, (int)y, viewWidth, (int)y + mThumbH);
            } else {
        		mStarted = false;
        		if(mThumbVisible == false && mDragging ==false){
        			dragFinishFlag = false;
        		}
    	        if(mThumbVisible==true &&dragFinishFlag==true){
    	    	   // mThumbVisible = false;
    	    	    //1218 在滑动listveiw Thumb图标不消失
    	        	if(!animFinish){
    	        		thumbAndScroll = false;
          	    	    isStart300ms = false;
                        mThumbVisible = false;
          	    	    //1218 end 在滑动listveiw Thumb图标不消失
          	    	    mDragging = false;
          	    	    return;
    	        	}
    	        	//快速滚动滑块结束2s后，mThumb图片开始动画消失
            		startAuim = true;
            		translateLen = (int) (density*43);
                    Thread thread = new Thread(){
                    	public void run() {
                    		boolean flag = true;
                    		animFinish = false;
        	                while(flag){
        	                	translateLen-=1;
        	                	if(translateLen<0){
        	                		translateLen =0;
        	                	}
        	                	mHandler.sendEmptyMessage(1);
        	                	try {
        							Thread.sleep(1);
        						} catch (InterruptedException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}

        	                	flag = translateLen<=0?false:true;
        	                }

            	    	    thumbAndScroll = false;
            	    	    isStart300ms = false;
                            translateLen = 0;
                            startAuim = false;
                            mThumbVisible = false;
            	    	    //1218 end 在滑动listveiw Thumb图标不消失
            	    	    
            	    	    animFinish = true;
    					}
                    };
                    thread.start();
    	    	    mHandler.removeCallbacks(mScrollFade);
                    mHandler.postDelayed(mScrollFade, 2000);
    	    	    invalidate();
    	    	    return;
    	        }
                removeThumb();
            }
        }
    }
    
    /**
     * Call when the list's items have changed
     */
	public void listItemsChanged() {
		getSections();
	}
	 //解决时间控件总是在切换图片集合时显示,mSwitchFolder为true时不显示，为false时显示，切换时置为true
	public void setSwitchFolderFlag(){
		mSwitchFolder = true;
		mHandler.removeCallbacks(mScrollFade);
		mScrollFade.mStarted = false;
		removeThumb();
	}
	
}