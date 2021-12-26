package com.yc.cn.ycgallery.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class EnterScreenAnimations extends ScreenAnimation{

    private static final String TAG = EnterScreenAnimations.class.getSimpleName();

    private final View mImageTo;

    private final ImageView mAnimatedImage;

    private View mMainContainer;

    private AnimatorSet mEnteringAnimation;


    private float[] mInitThumbnailMatrixValues = new float[9];


    private int mToTop;
    private int mToLeft;
    private int mToWidth;
    private int mToHeight;

    public EnterScreenAnimations(ImageView animatedImage, View imageTo, View mainContainer) {
        super(animatedImage.getContext());
        mAnimatedImage = animatedImage;
        mImageTo = imageTo;
        mMainContainer = mainContainer;
    }

    /**
     * This method combines several animations when screen is opened.
     * 1. Animation of ImageView position and image matrix.
     * 2. Animation of main container elements - they are fading in after image is animated to position
     */
    public void playEnteringAnimation(int left, int top, int width, int height) {
        mToLeft = left;
        mToTop = top;
        mToWidth = width;
        mToHeight =  height;
        AnimatorSet imageAnimatorSet = createEnteringImageAnimation();
        Animator mainContainerFadeAnimator = createEnteringFadeAnimator();
        mEnteringAnimation = new AnimatorSet();
        mEnteringAnimation.setDuration(IMAGE_TRANSLATION_DURATION);
        mEnteringAnimation.setInterpolator(new AccelerateInterpolator());
        mEnteringAnimation.addListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
                Log.v(TAG, "onAnimationCancel, mEnteringAnimation " + mEnteringAnimation);
                mEnteringAnimation = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "onAnimationEnd, mEnteringAnimation " + mEnteringAnimation);
                if (mEnteringAnimation != null) {
                    mEnteringAnimation = null;
                    mImageTo.setVisibility(View.VISIBLE);
                    mAnimatedImage.setVisibility(View.INVISIBLE);
                }
            }
        });
        mEnteringAnimation.playTogether(imageAnimatorSet, mainContainerFadeAnimator);
        mEnteringAnimation.start();
    }

    /**
     * Animator returned form this method animates fade in of all other elements on the screen besides ImageView
     */
    private ObjectAnimator createEnteringFadeAnimator() {
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(
                mMainContainer, "alpha", 0.0f, 1.0f);
        return fadeInAnimator;
    }
    /**
     * This method creates an animator set of 2 animations:
     * 1. ImageView position animation when screen is opened
     * 2. ImageView image matrix animation when screen is opened
     */
    @NonNull
    private AnimatorSet createEnteringImageAnimation() {
        ObjectAnimator positionAnimator = createEnteringImagePositionAnimator();
        ObjectAnimator matrixAnimator = createEnteringImageMatrixAnimator();
        AnimatorSet enteringImageAnimation = new AnimatorSet();
        enteringImageAnimation.playTogether(positionAnimator, matrixAnimator);
        return enteringImageAnimation;
    }


    @NonNull
    private ObjectAnimator createEnteringImagePositionAnimator() {
        PropertyValuesHolder propertyLeft = PropertyValuesHolder.ofInt("left", mAnimatedImage.getLeft(), mToLeft);
        PropertyValuesHolder propertyTop = PropertyValuesHolder.ofInt("top", mAnimatedImage.getTop(), mToTop - getStatusBarHeight());

        PropertyValuesHolder propertyRight = PropertyValuesHolder.ofInt("right", mAnimatedImage.getRight(), mToLeft + mToWidth);
        PropertyValuesHolder propertyBottom = PropertyValuesHolder.ofInt("bottom", mAnimatedImage.getBottom(), mToTop + mToHeight - getStatusBarHeight());

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(mAnimatedImage, propertyLeft, propertyTop, propertyRight, propertyBottom);
        animator.addListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // set new parameters of animated ImageView. This will prevent blinking of view when set visibility to visible in Exit animation
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mAnimatedImage.getLayoutParams();
                layoutParams.height = mImageTo.getHeight();
                layoutParams.width = mImageTo.getWidth();
                layoutParams.setMargins(mToLeft, mToTop - getStatusBarHeight(), 0, 0);
            }
        });
        return animator;
    }

    /**
     * This method creates Animator that will animate matrix of ImageView.
     * It is needed in order to show the effect when scaling of one view is smoothly changed to the scale of the second view.
     * <p>
     * For example: first view can have scaleType: centerCrop, and the other one fitCenter.
     * The image inside ImageView will smoothly change from one to another
     */
    private ObjectAnimator createEnteringImageMatrixAnimator() {

        Matrix initMatrix = MatrixUtils.getImageMatrix(mAnimatedImage);
        // store the data about original matrix into array.
        // this array will be used later for exit animation
        initMatrix.getValues(mInitThumbnailMatrixValues);

        final Matrix endMatrix = new Matrix(mImageTo.getMatrix());
        Log.v(TAG, "createEnteringImageMatrixAnimator, mInitThumbnailMatrixValues " + Arrays.toString(mInitThumbnailMatrixValues));

        Log.v(TAG, "createEnteringImageMatrixAnimator, initMatrix " + initMatrix);
        Log.v(TAG, "createEnteringImageMatrixAnimator,  endMatrix " + endMatrix);

        mAnimatedImage.setScaleType(ImageView.ScaleType.MATRIX);

        return ObjectAnimator.ofObject(mAnimatedImage, MatrixEvaluator.ANIMATED_TRANSFORM_PROPERTY,
                new MatrixEvaluator(), initMatrix, endMatrix);
    }


    public void cancelRunningAnimations() {
        if (mEnteringAnimation != null) {
            // cancel existing animation
            mEnteringAnimation.cancel();
        }
    }
}
