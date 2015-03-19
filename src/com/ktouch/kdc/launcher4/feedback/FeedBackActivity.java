package com.ktouch.kdc.launcher4.feedback;

import java.util.List;

import com.ktouch.kdc.launcher4.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FeedBackActivity extends Activity {

	private ListView mListView;
	private FeedbackAgent mAgent;
	private Conversation mComversation;
	private Context mContext;
	private ReplyAdapter adapter;
	private Button sendBtn;
	private EditText inputEdit;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RelativeLayout modemenubar;
	private TextView title;
	private ImageView albumsetback;
	private RelativeLayout actionBar;
	private String strContent;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
			mListView.setSelection(adapter.getCount());
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_layout);
		mContext = this;

		initView();
		mAgent = new FeedbackAgent(this);
		mComversation = mAgent.getDefaultConversation();
		adapter = new ReplyAdapter();
		mListView.setAdapter(adapter);
		sync();

	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.fb_reply_list);
		sendBtn = (Button) findViewById(R.id.fb_send_btn);
		inputEdit = (EditText) findViewById(R.id.fb_send_content);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.fb_reply_refresh);
		modemenubar = (RelativeLayout) findViewById(R.id.modemenubar);
		albumsetback = (ImageView) findViewById(R.id.albumsetback);
		title = (TextView) findViewById(R.id.modetitle);
		actionBar = (RelativeLayout) findViewById(R.id.actionbar);
		actionBar.setVisibility(View.GONE);
		modemenubar.setVisibility(View.VISIBLE);
		title.setText(R.string.previewer_feedback_title);
		TextWatcher watcher = new TextWatcher() {
			int textLen = 0;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				textLen = s.length();
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				int len = s.length();
				if ((textLen == 0 && len > 0)) {
					sendBtn.setBackgroundResource(R.drawable.feedback_sendout_normal);
				} else if (textLen > 0 && len == 0) {
					sendBtn.setBackgroundResource(R.drawable.feedback_sendout_no);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}

		};
		inputEdit.addTextChangedListener(watcher);
		StatusListener listener = new StatusListener();
		sendBtn.setOnTouchListener(listener);
		albumsetback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FeedBackActivity.this.finish();
			}

		});

		// 下拉刷新
		mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				sync();
			}
		});
	}

	// 数据同步
	private void sync() {

		mComversation.sync(new SyncListener() {

			@Override
			public void onSendUserReply(List<Reply> replyList) {

			}

			@Override
			public void onReceiveDevReply(List<Reply> replyList) {
				mSwipeRefreshLayout.setRefreshing(false);
				if (replyList == null || replyList.size() < 1) {
					return;
				}
				mHandler.sendMessage(new Message());
			}
		});
		mListView.setSelection(adapter.getCount());
	}

	// adapter
	class ReplyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mComversation.getReplyList().size();
		}

		@Override
		public Object getItem(int arg0) {
			return mComversation.getReplyList().get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Reply reply = mComversation.getReplyList().get(position);
			ViewHolder holder = null;
			if (convertView == null) {
				// 根据Type的类型来加载不同的Item布局
				if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
					// 开发者的回复
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.feedback_item_dev, null);
				} else {
					// 用户的反馈、回复
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.feedback_item_user, null);
				}
				holder = new ViewHolder();
				holder.reply_item = (TextView) convertView
						.findViewById(R.id.textView_feedback);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String content;
			if (Reply.TYPE_DEV_REPLY.endsWith(reply.type)) {
				holder.reply_item.setGravity(Gravity.CENTER);
				content = "   " + reply.content;// 开发者回复
			} else {
				content = reply.content;// 用户发送
			}
			holder.reply_item.setText(content);
			return convertView;
		}

		class ViewHolder {
			TextView reply_item;
		}
	}

	class StatusListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.fb_send_btn) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					String mContent = inputEdit.getText().toString();
					if (!mContent.equals("")) {
						sendBtn.setBackgroundResource(R.drawable.feedback_sendout_press);
					} else {
						Toast.makeText(FeedBackActivity.this,
								R.string.previewer_feedback_content,
								Toast.LENGTH_SHORT).show();
						return true;
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					strContent = inputEdit.getText().toString();
					if (strContent.equals("")) {
						return true;
					}
					adapter.notifyDataSetInvalidated();
					InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(
							inputEdit.getWindowToken(), 0); // 隐藏
					inputEdit.getEditableText().clear();
					if (!TextUtils.isEmpty(strContent)) {
						mComversation.addUserReply(strContent);// 添加到会话列表
						mHandler.sendMessage(new Message());
						sync();

					}
					sendBtn.setBackgroundResource(R.drawable.feedback_sendout_no);
				}
			}
			return true;
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);// 友盟统计
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);// 友盟统计
	}
}
