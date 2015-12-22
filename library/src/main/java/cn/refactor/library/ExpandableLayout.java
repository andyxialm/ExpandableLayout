package cn.refactor.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

/**
 * 作者 : andy
 * 日期 : 15/12/11 15:22
 * 邮箱 : andyxialm@gmail.com
 * 描述 : 可展开/关闭视图
 */
public class ExpandableLayout extends LinearLayout {

    private static final int DEFAULT_DURATION   = 300;
    private static final int TYPE_INIT_EXPAND   = 1;
    private static final int TYPE_INIT_COLLAPSE = 2;

    private int mWidth;
    private int mHeight;
    private int mDuration;
    private int mExpandDuration;
    private int mCollapseDuration;

    private boolean mIsInited;
    private boolean mIsExpand;
    private boolean mIsExecuting;
    private boolean mIsClickToToggle;

    private View mSwitcher;
    private OnStateChangedListener mListener;
    private Interpolator mExpandInterpolator;
    private Interpolator mCollapseInterpolator;

    public ExpandableLayout(Context context) {
        this(context, null);
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public ExpandableLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout);
            mDuration = ta.getInt(R.styleable.ExpandableLayout_duration, DEFAULT_DURATION);
            mIsClickToToggle = ta.getBoolean(R.styleable.ExpandableLayout_clickToToggle, false);
            mIsExpand = ta.getInteger(R.styleable.ExpandableLayout_initialState, TYPE_INIT_COLLAPSE) == TYPE_INIT_EXPAND;
            ta.recycle();
        }

        initValues();
        setOnClickListener(mIsClickToToggle ? new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        } : null);
    }

    public void expand() {
        if (mIsExpand || mIsExecuting) {
            return;
        }
        executeExpand(this);
        startSwitcherAnimation();
    }

    public void collapse() {
        if (!mIsExpand || mIsExecuting) {
            return;
        }
        executeCollapse(this);
        startSwitcherAnimation();
    }

    public void toggle() {
        if (mIsExpand) {
            collapse();
        } else {
            expand();
        }
    }

    public void setClickToToggle(boolean isClickToToggle) {
        mIsClickToToggle = isClickToToggle;
    }

    public void setOnStateChangedListener(OnStateChangedListener l) {
        mListener = l;
    }

    public void setDuration(int duration) {
        mDuration = duration;
        this.setExpandDuration(duration);
        this.setCollapseDuration(duration);
    }

    public void setExpandDuration(int expandDuration) {
        mExpandDuration = expandDuration;
    }

    public void setCollapseDuration(int collapseDuration) {
        mCollapseDuration = collapseDuration;
    }

    public void setExpand(boolean isExpand) {
        mIsExpand = isExpand;
        requestLayout();
    }

    public void setSwitcher(View switcher) {
        mSwitcher = switcher;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.setExpandInterpolator(interpolator);
        this.setCollapseInterpolator(interpolator);
    }
    
    public void setExpandInterpolator(Interpolator expandInterpolator) {
        mExpandInterpolator = expandInterpolator;
    }
    
    public void setCollapseInterpolator(Interpolator collapseInterpolator) {
        mCollapseInterpolator = collapseInterpolator;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureDimension(measureChildWidth(), widthMeasureSpec);
        int height = measureDimension(measureChildHeight(), heightMeasureSpec);
        mWidth = Math.max(mWidth, width);
        mHeight = Math.max(mHeight, height);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!changed) {
            return;
        }
        if (!mIsInited) {
            setVisibility(mIsExpand ? VISIBLE : GONE);
            mIsInited = true;
        }
    }

    private void initValues() {
        mExpandDuration = (mDuration == 0 ? DEFAULT_DURATION : mDuration);
        mCollapseDuration = (mDuration == 0 ? DEFAULT_DURATION : mDuration);
    }

    private void startSwitcherAnimation() {
        if (mSwitcher != null) {
            int duration = (mDuration == 0 ? DEFAULT_DURATION : (mIsExpand ? mExpandDuration : mCollapseDuration));
            Animation roateAnimation = createRotateAnimation(mSwitcher, duration);
            mSwitcher.startAnimation(roateAnimation);
        }
    }

    private void setExecuting(boolean isExecuting) {
        mIsExecuting = isExecuting;
    }

    private int measureChildWidth() {
        int width = 0;
        int cnt = getChildCount();
        for (int i = 0; i < cnt; i++) {
            width += getChildAt(i).getMeasuredWidth();
        }
        return width;
    }

    private int measureChildHeight() {
        int height = 0;
        int cnt = getChildCount();
        for (int i = 0; i < cnt; i++) {
            height += getChildAt(i).getMeasuredHeight();
        }
        return height;
    }

    private int measureDimension(int defaultSize, int measureSpec) {
        int result;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(defaultSize, specSize);
                break;
            default:
                result = specSize;
                break;
        }

        return result;
    }

    private RotateAnimation createRotateAnimation(final View view, int duration) {
        int pivotX = view.getWidth() >> 1;
        int pivotY = view.getHeight() >> 1;
        RotateAnimation animation = new RotateAnimation(mIsExpand ? 0 : -180, mIsExpand ? -180 : 0, pivotX, pivotY);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        return animation;
    }

    private ValueAnimator createAnimator(final View view, int startPos, int endPos) {
        boolean isExpand = startPos < endPos;
        int duration = (mDuration == 0 ? DEFAULT_DURATION : (isExpand ? mExpandDuration : mCollapseDuration));
        return this.createAnimator(view, startPos, endPos, duration);
    }

    private ValueAnimator createAnimator(final View view, int startPos, int endPos, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(startPos, endPos);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newPos = (Integer) animation.getAnimatedValue();
                int orientation = ((ExpandableLayout) view).getOrientation();
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (LinearLayout.HORIZONTAL == orientation) {
                    params.width = newPos;
                    params.height = getMeasuredHeight();
                    view.setLayoutParams(params);
                    return;
                }
                if (LinearLayout.VERTICAL == orientation) {
                    params.width = getMeasuredWidth();
                    params.height = newPos;
                    view.setLayoutParams(params);
                }
            }
        });
        return animator;
    }

    private void executeExpand(final View view) {
        mIsExpand = !mIsExpand;
        setVisibility(View.VISIBLE);
        int newPos = (getOrientation() == HORIZONTAL ? mWidth : mHeight);
        Animator animator = createAnimator(view, 0, newPos);
        animator.setInterpolator(mExpandInterpolator);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setExecuting(true);
                if (mListener != null) {
                    mListener.onPreExpand();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setExecuting(false);
                if (mListener != null) {
                    mListener.onExpanded();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void executeCollapse(final View view) {
        mIsExpand = !mIsExpand;
        int newPos = (getOrientation() == HORIZONTAL ? mWidth : mHeight);
        ValueAnimator animator = createAnimator(view, newPos, 0);
        animator.setInterpolator(mCollapseInterpolator);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setExecuting(true);
                if (mListener != null) {
                    mListener.onPreCollapse();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setExecuting(false);
                setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onCollapsed();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public interface OnStateChangedListener {
        void onPreExpand();
        void onPreCollapse();
        void onExpanded();
        void onCollapsed();
    }
}
