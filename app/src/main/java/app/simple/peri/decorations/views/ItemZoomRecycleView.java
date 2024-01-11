package app.simple.peri.decorations.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 支持在里面的条目图片放大缩小的RecyclerView,
 * 条目中图片的id叫R.id.basedetail_imageview, 现在是写死的, 后续可以改成方法设置
 * 给这个view设置activity即可激活放大缩小
 */
public class ItemZoomRecycleView extends RecyclerView {
    private ViewGroup decorView;//用来承载view的最上层界面
    private View oriView; //从原布局拿出来的view
    private ViewGroup.LayoutParams mOriLp; //原view的参数lp
    private int oriIndex;
    private ViewGroup oriParent; //原view的父布局
    private int mOriId = 0;
    private View placeHolderView;//用来放到原来的位置, 占位用的view
    
    private int[] oriTopLeft = new int[2];
    
    private float downX, downY; //手指按下的点
    private Activity activity;
    private int state; //响应自定义手势的几个状态, 0 默认状态, 系统处理, 1 准备状态  2拦截掉自己处理图片的状态 3回复到原位置的动画状态
    private PointF lastCenter = new PointF(); //双指中心点
    private double lastDistance; //双指距离
    
    public ItemZoomRecycleView(@NonNull Context context) {
        super(context);
    }
    
    public ItemZoomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public ItemZoomRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setActivity(Activity activity) {
        this.activity = activity;
        placeHolderView = new View(activity);
    }
    
    public void setOriId(int oriId) {
        mOriId = oriId;
    }
    
    private void backToOri() {
        if (state == 3) {
            return;
        }
        state = 3;
        final float translationX = oriView.getTranslationX();
        final float translationY = oriView.getTranslationY();
        final float scaleX = oriView.getScaleX();
        final float scaleY = oriView.getScaleY();
        
        ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 0f);
        animator.setDuration(200);
        animator.addUpdateListener(animation -> {
            float f = (float) animation.getAnimatedValue();
            //从初始位置到0的过程
            oriView.setTranslationX(translationX * f);
            oriView.setTranslationY(translationY * f);
            //从初始位置到1的过程
            oriView.setScaleX(1.0f + (scaleX - 1.0f) * f);
            oriView.setScaleY(1.0f + (scaleY - 1.0f) * f);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //mImageView.setVisibility(GONE);
                //mOriImageView.setVisibility(VISIBLE);
                //把占位view拿出来, 把原来的view放回去
                decorView.removeView(oriView);
                oriParent.removeView(placeHolderView);
                oriParent.addView(oriView, oriIndex, mOriLp);
                state = 0;
            }
        });
        animator.start();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (activity != null && mOriId != 0) {
            int action = event.getActionMasked();
            if (state == 1) {//准备
                int pointerCount = event.getPointerCount();
                if (pointerCount <= 1 ||
                        (pointerCount == 2 && (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL))) {
                    state = 0;
                } else {
                    if (action == MotionEvent.ACTION_MOVE && oriView != null) {
                        float x1 = event.getX(0);
                        float y1 = event.getY(0);
                        float x2 = event.getX(1);
                        float y2 = event.getY(1);
                        
                        double distance = getDistence(x1, y1, x2, y2);
                        if (Math.abs(distance - lastDistance) >= 10) {
                            decorView = (ViewGroup) activity.getWindow().getDecorView();
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(oriView.getWidth(), oriView.getHeight());
                            lp.topMargin = oriTopLeft[1];
                            lp.leftMargin = oriTopLeft[0];
                            lp.gravity = Gravity.START | Gravity.TOP;
                            
                            //新建view的方案, 只支持imageview
                            //if (mImageView == null) {
                            //    mImageView = new ImageView(mActivity);
                            //    mImageView.setElevation(4);
                            //}
                            //mImageView.setImageDrawable(mOriImageView.getDrawable());
                            //ViewParent parent = mImageView.getParent();
                            //if (parent != null) {
                            //    mImageView.setLayoutParams(lp);
                            //} else {
                            //    group.addView(mImageView, lp);
                            //}
                            //mImageView.setTranslationX(0);
                            //mImageView.setTranslationY(0);
                            //mImageView.setScaleX(1.0f);
                            //mImageView.setScaleY(1.0f);
                            //mImageView.setVisibility(VISIBLE);
                            //mOriImageView.setVisibility(INVISIBLE);
                            
                            //把原来的view拿出来, 把占位view放进去
                            mOriLp = oriView.getLayoutParams();
                            oriParent = (ViewGroup) oriView.getParent();
                            oriIndex = oriParent.indexOfChild(oriView);
                            oriParent.removeView(oriView);
                            placeHolderView.setId(oriView.getId());
                            oriParent.addView(placeHolderView, oriIndex, mOriLp);
                            decorView.addView(oriView, lp);
                            
                            //修复因为阈值引起的第一帧跳变
                            downX = x1;
                            downY = y1;
                            lastCenter.x = (x1 + x2) * 0.5f;
                            lastCenter.y = (y1 + y2) * 0.5f;
                            lastDistance = getDistence(x1, y1, x2, y2);
                            
                            state = 2;//进入拖动状态
                            
                            //禁用父布局事件拦截
                            getParent().requestDisallowInterceptTouchEvent(true);
                            //有些控价, 如谷歌的SwipeRefreshLayout, 实现的requestDisallowInterceptTouchEvent有问题, 不会向上传递
                            //所以需要循环向上调用
                            //ViewParent par = getParent();
                            //while (par != null) {
                            //    par.requestDisallowInterceptTouchEvent(true);
                            //    par = par.getParent();
                            //}
                        }
                    }
                    return true;
                }
            } else if (state == 2) { //自己处理手势
                //LogHelper.d("wangzixu", "detailpage onTouch x  = " + x + ", y = " + y);
                int pointerCount = event.getPointerCount();
                if (pointerCount <= 1 ||
                        (pointerCount == 2 && (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL))) {
                    backToOri();
                } else {
                    oriView.getParent().requestDisallowInterceptTouchEvent(true);
                    
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);
                    
                    //处理图片的移动
                    float halfX = (x1 + x2) * 0.5f - lastCenter.x;
                    float halfY = (y1 + y2) * 0.5f - lastCenter.y;
                    oriView.setTranslationX(halfX);
                    oriView.setTranslationY(halfY);
                    
                    //处理图片的放大缩小
                    double distance = getDistence(x1, y1, x2, y2);
                    double scaleFactor = distance / lastDistance;
                    oriView.setScaleX((float) scaleFactor);
                    oriView.setScaleY((float) scaleFactor);
                }
                return true;
            } else if (state == 3) {//返回原位置中
                //nothing
                return true;
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    downX = event.getX();
                    downY = event.getY();
                } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    float x1 = event.getX();
                    float y1 = event.getY();
                    if (Math.abs(x1 - downX) < 8 && Math.abs(y1 - downY) < 8) { //手指抖动的阈值
                        float x2 = event.getX(1);
                        float y2 = event.getY(1);
                        View view1 = findChildViewUnder(x1, y1);
                        View view2 = findChildViewUnder(x2, y2);
                        //LogHelper.d("wangzixu", "detailpage onTouch view1 = " + view1 + ", view2 = " + view2);
                        if (view1 != null && view1 == view2) {
                            int raw_x1 = (int) event.getRawX();
                            int raw_y1 = (int) event.getRawY();
                            int raw_x2 = (int) (raw_x1 + x2 - x1);
                            int raw_y2 = (int) (raw_y1 + y2 - y1);
                            
                            View view = findUnderImageView((ViewGroup) view1, raw_x1, raw_y1, raw_x2, raw_y2);
                            //ImageView imageView = findUnderImageView2((ViewGroup) view1);
                            
                            if (view != null) {
                                oriView = view;
                                oriView.getParent().requestDisallowInterceptTouchEvent(true);
                                //LogHelper.d("wangzixu", "detailpage onTouch view1.findViewById mOriLocation  = " + mOriLocation + ", " + mOriTopLeft[0] + "," + mOriTopLeft[1]);
                                state = 1; //双指按下, 并且双指按下的条目中有imageview, 并且双指的点都在imageview的区域中
                            }
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
    
    private boolean viewContainXY(View view, int[] location, int x, int y) {
        return x >= location[0] && x <= location[0] + view.getWidth()
                && y >= location[1] && y <= location[1] + view.getHeight();
    }
    
    //算法一, 依赖正确的x,y, xy是相对屏幕的坐标,
    private View findUnderImageView(ViewGroup parent, int x1, int y1, int x2, int y2) {
        for (int i = parent.getChildCount(); i >= 0; i--) {
            View child = parent.getChildAt(i);
            if (child != null && child.getId() == mOriId) {
                child.getLocationOnScreen(oriTopLeft);
                
                boolean contain1 = viewContainXY(child, oriTopLeft, x1, y1);
                boolean contain2 = viewContainXY(child, oriTopLeft, x2, y2);
                if (contain1 && contain2) {
                    return child;
                }
                if (contain1 ^ contain2) { //如果view包含其中一个点, 说明两个手指落在不同的view上了, 直接返回null
                    return null;
                }
            } else if (child instanceof ViewGroup) {
                View view = findUnderImageView((ViewGroup) child, x1, y1, x2, y2);
                if (view != null) {
                    return view;
                }
            }
        }
        return null;
    }
    
    //算法2
    //private View findUnderImageView2(ViewGroup parent) {
    //    ArrayList<View> changeIdlist = new ArrayList<>();
    //    View view = parent.findViewById(mOriId);
    //    while (view != null) {
    //        //找到的imageview有可能出在viewpager中, 在屏幕外了, 需要纠正找到屏幕中的
    //        view.getLocationOnScreen(mOriTopLeft);
    //        if (mOriTopLeft[0] == 0) {
    //            break;
    //        } else {
    //            view.setId(R.id.zan_redpoint);//随便设置一个id, 不要和原来的重复就好, 最好设置一个这界面都找不到的id
    //            changeIdlist.add(view);
    //            view = parent.findViewById(mOriId);
    //        }
    //    }
    //    for (int i = 0; i < changeIdlist.size(); i++) {
    //        changeIdlist.get(i).setId(mOriId); //一定需要把id恢复
    //    }
    //    return view;
    //}
    
    private double getDistence(float x1, float y1, float x2, float y2) {
        float deltaX = Math.abs(x1 - x2);
        float deltaY = Math.abs(y1 - y2);
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}