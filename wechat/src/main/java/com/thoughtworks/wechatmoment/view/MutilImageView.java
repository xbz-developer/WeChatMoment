package com.thoughtworks.wechatmoment.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.thoughtworks.wechatmoment.R;
import com.thoughtworks.wechatmoment.bean.TweetInfo;
import com.thoughtworks.wechatmoment.utils.Densityutil;

import java.util.ArrayList;
import java.util.List;

public class MutilImageView extends LinearLayout {

    public static int MAX_WIDTH = 0;
    private static int SINGLE_IMAGE_WIDTH = 200;   // width of single image
    private static int SINGLE_IMAGE_HEIGHT = 150;  // height of single image
    private static int IMAGE_PADDING = 2;          // padding between images
    private static int MAX_PER_ROW_COUNT = 3;      // max num of images per row

    //width and height in single and mutil image
    private int singleImageWidth = Densityutil.dp2px(getContext(), SINGLE_IMAGE_WIDTH);
    private int singleImageHeight = Densityutil.dp2px(getContext(), SINGLE_IMAGE_HEIGHT);
    private int imagePadding = Densityutil.dp2px(getContext(), IMAGE_PADDING);
    private int mutilImageWidthAndHeight = 0;

    // image urls list to load
    private ArrayList<TweetInfo.ImageUrl> loadImageList;
    private LayoutParams singleLp;
    private LayoutParams mutilLp;
    private LayoutParams rowPara;

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public MutilImageView(Context context) {
        super(context);
    }

    public MutilImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setList(List<TweetInfo.ImageUrl> lists) throws IllegalArgumentException {
        if (lists == null) {
            throw new IllegalArgumentException("loadImageList is null");
        }
        loadImageList = (ArrayList<TweetInfo.ImageUrl>) lists;

        if (MAX_WIDTH > 0) {
            mutilImageWidthAndHeight = MAX_WIDTH / 3 - imagePadding;
            initImageLayoutParams();
        }
        initView();
    }

    private void initView() {
        this.setOrientation(VERTICAL);
        this.removeAllViews();
        if (MAX_WIDTH == 0) {
            addView(new View(getContext()));
            return;
        }
        if (loadImageList == null || loadImageList.size() == 0) {
            return;
        }
        if (loadImageList.size() == 1) {
            for (final TweetInfo.ImageUrl imageUrl : loadImageList) {
                String url = imageUrl.getUrl();
                final ImageView imageView = new ImageView(getContext());
                imageView.setId(url.hashCode()); // 指定id
                imageView.setLayoutParams(singleLp);
                imageView.setMinimumWidth(mutilImageWidthAndHeight);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // can not load image from url,use default image instead
//                Glide.with(getContext()).load(url).into(imageView);
                imageView.setImageResource(R.drawable.ic_launcher_background);
                imageView.setOnClickListener(mImageViewOnClickListener);
                addView(imageView);
            }
        } else {
            int allCount = loadImageList.size();
            if (allCount == 4) {
                MAX_PER_ROW_COUNT = 2;
            } else {
                MAX_PER_ROW_COUNT = 3;
            }
            int rowCount = allCount / MAX_PER_ROW_COUNT + (allCount % MAX_PER_ROW_COUNT > 0 ? 1 : 0);// 行数
            for (int rowCursor = 0; rowCursor < rowCount; rowCursor++) {
                LinearLayout rowLayout = new LinearLayout(getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);

                rowLayout.setLayoutParams(rowPara);
                if (rowCursor == 0) {
                    rowLayout.setPadding(0, imagePadding, 0, 0);
                }

                int columnCount = allCount % MAX_PER_ROW_COUNT == 0 ? MAX_PER_ROW_COUNT : allCount % MAX_PER_ROW_COUNT;// 每行的列数
                if (rowCursor != rowCount - 1) {
                    columnCount = MAX_PER_ROW_COUNT;
                }
                addView(rowLayout);

                int rowOffset = rowCursor * MAX_PER_ROW_COUNT;
                for (int columnCursor = 0; columnCursor < columnCount; columnCursor++) {
                    int position = columnCursor + rowOffset;
                    String thumbUrl = loadImageList.get(position).getUrl();
                    final ImageView imageView = new ImageView(getContext());
                    imageView.setId(thumbUrl.hashCode());
                    imageView.setLayoutParams(mutilLp);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setTag(R.string.app_name, position);
                    // can not load image from url,use default image instead
//                    Glide.with(getContext()).load(thumbUrl).into(imageView);
                    imageView.setImageResource(R.drawable.ic_launcher_background);
                    imageView.setOnClickListener(mImageViewOnClickListener);
                    rowLayout.addView(imageView);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MAX_WIDTH == 0) {
            int width = measureWidth(widthMeasureSpec);
            if (width > 0) {
                MAX_WIDTH = width;
                if (loadImageList != null && loadImageList.size() > 0) {
                    setList(loadImageList);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private void initImageLayoutParams() {

        singleLp = new LayoutParams(singleImageWidth, singleImageHeight);
        singleLp.setMargins(0, imagePadding, 0, imagePadding);

        mutilLp = new LayoutParams(mutilImageWidthAndHeight, mutilImageWidthAndHeight);
        mutilLp.setMargins(0, 0, imagePadding, 0);

        int wrap = LayoutParams.WRAP_CONTENT;
        int match = LayoutParams.MATCH_PARENT;
        rowPara = new LayoutParams(match, wrap);
        rowPara.setMargins(0, 0, 0, imagePadding);
    }

    private View.OnClickListener mImageViewOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, view.getTag() + "");
            }
        }
    };

    public interface OnItemClickListener {
        void onItemClick(View view, String position);
    }
}