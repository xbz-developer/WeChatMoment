package com.thoughtworks.wechatmoment.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thoughtworks.wechatmoment.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RefreshListView extends ListView {

    private static final String TAG = "RefreshListView";
    private static final int PULLDOWN_STATE = 1;// 下拉刷新
    private static final int RELEASE_STATE = 2; // 松开刷新
    private static final int REFRSHING_STATE = 3; // 正在刷新
    private int refreshState = PULLDOWN_STATE;// 初始状态为下拉刷新

    private LinearLayout mHeadRoot;
    private View mViewFoot;
    private LinearLayout mRefreshHeadView;
    private int mRefreshHeadHeight;
    private int mViewFootHeight;
    private float mDownY = -1;
    private View mTitleView;
    private ImageView mArrowImage;
    private ProgressBar mLoadingBar;
    private TextView mHeadStateTextView;
    private TextView mRefreshTimeTextView;
    private RotateAnimation mRotateDownAnim;
    private RotateAnimation mRotateUpAnim;
    private boolean isLoadingMore = false;
    private OnRefreshDataListener mOnRefreshDataListener;

    public void setOnRefreshDataListener(OnRefreshDataListener listener) {
        mOnRefreshDataListener = listener;
    }

    public interface OnRefreshDataListener {
        void freshData();
        void loadMore();
    }

    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHead();
        initFoot();
        initAnimation();
        initEvent();
    }

    private void initEvent() {

        this.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: " + getLastVisiblePosition() + " " + getAdapter().getCount());
                    int lastPosition = getLastVisiblePosition();
                    if (lastPosition == getAdapter().getCount() - 1 && !isLoadingMore) {
                        Log.d(TAG, "onScrollStateChanged: load more");
                        isLoadingMore = true;
                        mViewFoot.setPadding(0, 0, 0, 0);
                        setSelection(getAdapter().getCount());
                        if (mOnRefreshDataListener != null) {
                            mOnRefreshDataListener.loadMore();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void initHead() {
        mHeadRoot = (LinearLayout) View.inflate(getContext(), R.layout.listview_head, null);
        mRefreshHeadView = (LinearLayout) mHeadRoot.findViewById(R.id.ll_listview_head_refreshview);

        mArrowImage = (ImageView) mHeadRoot.findViewById(R.id.iv_listview_head_arrow);
        mLoadingBar = (ProgressBar) mHeadRoot.findViewById(R.id.pb_listview_head_loading);
        mHeadStateTextView = (TextView) mHeadRoot.findViewById(R.id.tv_listview_head_statedesc);
        mRefreshTimeTextView = (TextView) mHeadRoot.findViewById(R.id.tv_listview_head_time);

        mRefreshHeadView.measure(0, 0);
        mRefreshHeadHeight = mRefreshHeadView.getMeasuredHeight();
        // hide HeadView
        mRefreshHeadView.setPadding(0, -mRefreshHeadHeight, 0, 0);
        addHeaderView(mHeadRoot);
    }

    private void initFoot() {
        mViewFoot = View.inflate(getContext(), R.layout.listview_foot, null);
        // hide FootVIew
        mViewFoot.measure(0, 0);
        mViewFootHeight = mViewFoot.getMeasuredHeight();
        mViewFoot.setPadding(0, -mViewFootHeight, 0, 0);
        addFooterView(mViewFoot);
    }

    private void initAnimation() {
        mRotateUpAnim = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(500);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(500);
        mRotateDownAnim.setFillAfter(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (refreshState == REFRSHING_STATE) {
                    return true;
                }
                if (!isTitleViewShow()) {
                    break;
                }
                if (mDownY == -1) {
                    mDownY = ev.getY();
                }
                float moveY = ev.getY();
                float dy = moveY - mDownY;
                if (getFirstVisiblePosition() == 0 && dy > 0) {
                    float hiddenHeight = -mRefreshHeadHeight + dy;
                    if (hiddenHeight >= 0 && refreshState != RELEASE_STATE) {
                        refreshState = RELEASE_STATE;
                        processState();
                    } else if (hiddenHeight < 0 && refreshState != PULLDOWN_STATE) {
                        refreshState = PULLDOWN_STATE;
                        processState();
                    }
                    mRefreshHeadView.setPadding(0, (int) hiddenHeight, 0, 0);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (refreshState == PULLDOWN_STATE) {
                    mRefreshHeadView.setPadding(0, -mRefreshHeadHeight, 0, 0);
                } else if (refreshState == RELEASE_STATE) {
                    refreshState = REFRSHING_STATE;
                    processState();
                    // load fresh data
                    if (mOnRefreshDataListener != null) {
                        mOnRefreshDataListener.freshData();
                    }
                    mRefreshHeadView.setPadding(0, 0, 0, 0);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void updateState() {
        Log.d(TAG, "updateState: isLoadingMore " + isLoadingMore);
        if (isLoadingMore) {
            mViewFoot.setPadding(0, -mViewFootHeight, 0, 0);
            isLoadingMore = false;
        } else {
            updateRefreshState();
        }
    }

    public void updateRefreshState() {
        Log.d(TAG, "updateRefreshState: ");
        refreshState = PULLDOWN_STATE;
        mArrowImage.setVisibility(View.VISIBLE);
        mLoadingBar.setVisibility(View.GONE);
        mHeadStateTextView.setText("下拉刷新");
        mRefreshTimeTextView.setText(getCurrentTime());
        mRefreshHeadView.setPadding(0, -mRefreshHeadHeight, 0, 0);
    }

    private String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    private void processState() {
        switch (refreshState) {
            case PULLDOWN_STATE:
                mArrowImage.startAnimation(mRotateDownAnim);
                mHeadStateTextView.setText("下拉刷新");
                break;
            case RELEASE_STATE:
                mArrowImage.startAnimation(mRotateUpAnim);
                mHeadStateTextView.setText("松开刷新");
                break;
            case REFRSHING_STATE:
                mArrowImage.clearAnimation();
                mArrowImage.setVisibility(View.GONE);
                mLoadingBar.setVisibility(View.VISIBLE);
                mHeadStateTextView.setText("正在刷新");
                break;
            default:
                break;
        }
    }

    public void addTitleView(View titleView) {
        mTitleView = titleView;
        mHeadRoot.addView(titleView);
    }

    public boolean isTitleViewShow() {
        int[] location = new int[2];
        this.getLocationInWindow(location);
        int lv_y = location[1];
        mTitleView.getLocationInWindow(location);
        int lunbo_y = location[1];
        if (lunbo_y >= lv_y) {
            return true;
        } else {
            return false;
        }
    }
}
