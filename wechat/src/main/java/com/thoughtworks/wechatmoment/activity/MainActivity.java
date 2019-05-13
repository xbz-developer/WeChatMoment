package com.thoughtworks.wechatmoment.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.thoughtworks.wechatmoment.R;
import com.thoughtworks.wechatmoment.adapter.WeChatAdapter;
import com.thoughtworks.wechatmoment.bean.TweetInfo;
import com.thoughtworks.wechatmoment.bean.TweetTitle;
import com.thoughtworks.wechatmoment.utils.Constants;
import com.thoughtworks.wechatmoment.utils.SpUtils;
import com.thoughtworks.wechatmoment.view.RefreshListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";
    private static final int SUCCESS_COMPLETE_TITLE = 1001;
    private static final int SUCCESS_COMPLETE_BODY = 1002;
    private static final int FIRST_SHOW_LIST_ITEM_COUNT = 5;
    private static final int SECOND_LOAD_MORE_COUNT = 10;
    private OkHttpClient mOkHttpClient = new OkHttpClient();
    private List<TweetInfo> mTweetInfosData;
    private List<TweetInfo> mTempTweetInfosData;
    private RefreshListView mListView;
    private WeChatAdapter mWeChatAdapter;
    private ImageView mHeadProfileImage;
    private TextView mHeadUserName;
    private TextView mHeadNick;
    private ImageView mHeadIcon;
    private View mTitleView;
    private boolean isRefresh = false;

    Handler myHanlder = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS_COMPLETE_TITLE: {
                    Log.d(TAG, "handleMessage: SUCCESS_COMPLETE_TITLE");
                    TweetTitle tweetTitle = (TweetTitle) msg.obj;
                    refreshTitleUI(tweetTitle);
                    break;
                }
                case SUCCESS_COMPLETE_BODY: {
                    ArrayList<TweetInfo> bodyResultArray = (ArrayList<TweetInfo>) msg.obj;
                    Log.d(TAG, "handleMessage: SUCCESS_COMPLETE_BODY " + bodyResultArray.size());
                    mTweetInfosData = checkDatas(bodyResultArray);
                    mListView.updateState();
                    mTempTweetInfosData = getDataAfterFresh(mTweetInfosData);
                    if (isRefresh) {
                        Toast.makeText(getApplicationContext(), "刷新数据成功", Toast.LENGTH_LONG).show();
                        isRefresh = false;
                        mWeChatAdapter.notifyDataSetChanged();
                    } else {
                        refreshBodyUI();
                    }
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.wechat_moment_list_view);
        mListView.setFastScrollEnabled(true);
        // HeadView
        mTitleView = View.inflate(this, R.layout.title_item, null);
        mHeadProfileImage = mTitleView.findViewById(R.id.profile_image);
        mHeadUserName = mTitleView.findViewById(R.id.host_username);
        mHeadNick = mTitleView.findViewById(R.id.host_nick);
        mHeadIcon = mTitleView.findViewById(R.id.host_icon);
        mListView.addTitleView(mTitleView);
        // load data
        LoadData();
        mListView.setOnRefreshDataListener(new RefreshListView.OnRefreshDataListener() {
            @Override
            public void freshData() {
                Log.d(TAG, "freshData: ");
                isRefresh = true;
                LoadData();
            }

            @Override
            public void loadMore() {
                Log.d(TAG, "loadMore: ");
                LoadMoreData();
            }
        });
    }

    private void refreshBodyUI() {
        mWeChatAdapter = new WeChatAdapter(this, mTempTweetInfosData);
        mListView.setAdapter(mWeChatAdapter);
    }

    private void refreshTitleUI(TweetTitle tweetTitle) {
        mHeadUserName.setText(tweetTitle.getUsername());
        mHeadNick.setText(tweetTitle.getNick());
        mHeadIcon.setImageResource(R.drawable.ic_launcher_web);
        mHeadProfileImage.setImageResource(R.drawable.profile);
        // myself Image Loader
         /*
        ImageLoader loader = ImageLoader.getInstance();
        if (loader != null) {
            loader.displayImage(tweetInfo.getSender().getAvatar(), holder.senderIcon);
        }
        */

        // 3rd lib Image Loader
        /*
        Glide.with(getContext()).load(tweetInfo.getSender().getAvatar()).into(holder.senderIcon);
         */
    }

    // TITLEURL = "http://thoughtworks-ios.herokuapp.com/user/jsmith"
    // BODYURL = "http://thoughtworks-ios.herokuapp.com/user/jsmith/tweets"
    public void LoadData() {
        Log.d(TAG, "LoadData: ");
        // load data from loacal
        if (!isRefresh) {
            LoadDataFromSp();
        }

        final Request bodyRequest = new Request.Builder().url(Constants.URLS.BODYURL).build();
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);    // socket timeout
        mOkHttpClient.newCall(bodyRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: NO Response");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = new String(response.body().bytes(), "utf-8");
                SpUtils.putString(getApplicationContext(), Constants.SP_BODY_KEY_NAME, json);
                List<TweetInfo> bodyResultArray = parseBodyData(json);
                Log.d(TAG, "onResponse: " + bodyResultArray.size());
                //Update UI
                myHanlder.obtainMessage(SUCCESS_COMPLETE_BODY, bodyResultArray).sendToTarget();
            }
        });

        final Request titleRequest = new Request.Builder().url(Constants.URLS.TITLEURL).build();
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);    // socket timeout
        mOkHttpClient.newCall(titleRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG, "onFailure: NO Response");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = new String(response.body().bytes(), "utf-8");
                SpUtils.putString(getApplicationContext(), Constants.SP_TITLE_KEY_NAME, json);
                TweetTitle titleResult = parseTitleData(json);
                //Update UI
                myHanlder.obtainMessage(SUCCESS_COMPLETE_TITLE, titleResult).sendToTarget();
            }
        });
    }

    private void LoadMoreData() {
        // 使用Hanlder的postDelayed延时1秒模拟加载更多操作
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int count = mTempTweetInfosData.size();
                Log.d(TAG, "LoadMoreData: count " + count);
                if (count <= SECOND_LOAD_MORE_COUNT) {
                    for (int i = count; i < count + 5; i++) {
                        mTempTweetInfosData.add(mTweetInfosData.get(count));
                    }
                    Toast.makeText(getApplicationContext(), "已加载更多", Toast.LENGTH_LONG).show();
                    mListView.updateState();
                    mWeChatAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "没有更多数据", Toast.LENGTH_LONG).show();
                    mListView.updateState();
                }
            }
        }, 1000);
    }

    private void LoadDataFromSp() {
        // Title
        String localTitleJson = SpUtils.getString(getApplicationContext(), Constants.SP_TITLE_KEY_NAME, null);
        if (!TextUtils.isEmpty(localTitleJson)) {
            TweetTitle titleResult = parseTitleData(localTitleJson);
            refreshTitleUI(titleResult);
        }
        // Body
        String localBodyJson = SpUtils.getString(getApplicationContext(), Constants.SP_BODY_KEY_NAME, null);
        if (!TextUtils.isEmpty(localBodyJson)) {
            List<TweetInfo> bodyResultArray = parseBodyData(localBodyJson);
            mTweetInfosData = checkDatas(bodyResultArray);
            ;
            mTempTweetInfosData = getDataAfterFresh(mTweetInfosData);
            refreshBodyUI();
        }
    }

    private TweetTitle parseTitleData(String json) {
        Gson gson = new Gson();
        TweetTitle titleResult = gson.fromJson(json, TweetTitle.class);
        return titleResult;
    }

    private List<TweetInfo> parseBodyData(String json) {
        Gson gson = new Gson();
        List<TweetInfo> bodyResultArray = gson.fromJson(json, new TypeToken<List<TweetInfo>>() {
        }.getType());
        return bodyResultArray;
    }

    private List<TweetInfo> checkDatas(List<TweetInfo> tweetInfoList) {
        if (tweetInfoList == null || tweetInfoList.isEmpty()) {
            return tweetInfoList;
        }
        List<TweetInfo> tempTweetInfoList = new ArrayList<>();
        int count = tweetInfoList.size();
        for (int i = 0; i < count; i++) {
            TweetInfo tweetInfo = tweetInfoList.get(i);
            if (tweetInfo.getContent() != null || tweetInfo.getImages() != null) {
                tempTweetInfoList.add(tweetInfo);
            }
        }
        Log.d(TAG, "checkDatas: " + tempTweetInfoList.size());
        return tempTweetInfoList;
    }

    private List<TweetInfo> getDataAfterFresh(List<TweetInfo> tweetInfoList) {
        List<TweetInfo> tempTweetInfoList = new ArrayList<>();
        for (int i = 0; i < FIRST_SHOW_LIST_ITEM_COUNT; i++) {
            tempTweetInfoList.add(tweetInfoList.get(i));
        }
        return tempTweetInfoList;
    }
}
