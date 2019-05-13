package com.thoughtworks.wechatmoment.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thoughtworks.wechatmoment.R;
import com.thoughtworks.wechatmoment.bean.TweetInfo;
import com.thoughtworks.wechatmoment.view.MutilImageView;

import java.util.ArrayList;
import java.util.List;


public class WeChatAdapter extends BaseAdapter {
    private static final String TAG = "WeChatAdapter";
    private List<TweetInfo> mTweetInfoList;
    private Context mContext;

    public WeChatAdapter(Context context, List<TweetInfo> tweetInfoList) {
        mContext = context;
        mTweetInfoList = tweetInfoList;
    }

    @Override
    public int getCount() {
        if (mTweetInfoList != null) {
            return mTweetInfoList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mTweetInfoList != null) {
            return mTweetInfoList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NormalViewHolder normalHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.tweet_info_item, parent, false);
            normalHolder = new NormalViewHolder();
            normalHolder.senderIcon = (ImageView) convertView.findViewById(R.id.sender_icon);
            normalHolder.senderName = (TextView) convertView.findViewById(R.id.sender_username);
            normalHolder.senderNick = (TextView) convertView.findViewById(R.id.sender_nick);
            normalHolder.contents = (TextView) convertView.findViewById(R.id.tweet_contents);
            normalHolder.mutilImageView = convertView.findViewById(R.id.tweet_images);
            normalHolder.comments = (LinearLayout) convertView.findViewById(R.id.tweet_comments);
            convertView.setTag(normalHolder);
        } else {
            normalHolder = (NormalViewHolder) convertView.getTag();
        }
        TweetInfo tweetInfo = mTweetInfoList.get(position);
        // UserName
        normalHolder.senderName.setText(tweetInfo.getSender().getUsername());
        // Nick
        normalHolder.senderNick.setText(tweetInfo.getSender().getNick());
        // Icon
        normalHolder.senderIcon.setImageResource(R.drawable.ic_launcher);
//        myself Image Loader
         /*
        ImageLoader loader = ImageLoader.getInstance();
        if (loader != null) {
            loader.displayImage(tweetInfo.getSender().getAvatar(), holder.senderIcon);
        }
        */
//         3rd lib Image Loader
        /*
        Glide.with(getContext()).load(tweetInfo.getSender().getAvatar()).into(holder.senderIcon);
         */
        // Content
        if (!(tweetInfo.getContent() == null || tweetInfo.getContent().isEmpty())) {
            normalHolder.contents.setText("");
            normalHolder.contents.setVisibility(View.VISIBLE);
            normalHolder.contents.setBackgroundColor(Color.GRAY);
            normalHolder.contents.setText(tweetInfo.getContent());
        }
        // Images
        if (tweetInfo.getImages() != null && tweetInfo.getImages().size() > 0) {
            normalHolder.mutilImageView.setVisibility(View.VISIBLE);
            normalHolder.mutilImageView.setList(tweetInfo.getImages());
        }
        // Comments
        ArrayList<TweetInfo.CommentInfo> commentInfoList = (ArrayList<TweetInfo.CommentInfo>) tweetInfo.getComments();
        if (!(commentInfoList == null || commentInfoList.size() == 0)) {
            int count = commentInfoList.size();
            for (int i = 0; i < count; i++) {
                TweetInfo.CommentInfo commentInfo = commentInfoList.get(i);
                TextView textView = new TextView(mContext);
                SpannableStringBuilder builder = new SpannableStringBuilder();
                String commentName = commentInfo.getSender().getUsername();
                String commentCotents = commentInfo.getContent();
                builder.append(commentName).append(" : ").append(commentCotents);
                textView.setText(builder);
                normalHolder.comments.addView(textView);
            }
            normalHolder.comments.setBackgroundColor(Color.GRAY);
            normalHolder.comments.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    class NormalViewHolder {
        ImageView senderIcon;
        TextView senderName;
        TextView senderNick;
        TextView contents;
        MutilImageView mutilImageView;
        LinearLayout comments;
    }
}
