package com.yc.cn.ycgallery.animations;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;


public class ExitScreenAnimations extends ScreenAnimation{

    private static final String TAG = ExitScreenAnimations.class.getSimpleName();


    private final ImageView mAnimatedImage;
    private final View mImageTo;
    private final View mMainContainer;

    /**
     * These values represent the final position of a Image that is translated
     */
    private int mToTop;
    private int mToLeft;
    private int mToWidth;
    private int mToHeight;

    private AnimatorSet mExitingAnimation;

    private float[] mToThumbnailMatrixValues;

    public ExitScreenAnimations(ImageView animatedImage, View imageTo, View mainContainer) {
        super(animatedImage.getContext());
        mAnimatedImage = animatedImage;
        mImageTo = imageTo;
        mMainContainer = mainContainer;
    }

    public void playExitAnimations(int toTop, int toLeft, int toWidth, int toHeight, float[] toThumbnailMatrixValues) {
        mToTop = toTop;
        mToLeft = toLeft;
        mToWidth = toWidth;
        mToHeight = toHeight;

        mToThumbnailMatrixValues = toThumbnailMatrixValues;

        Log.v(TAG, "playExitAnimations, mExitingAnimation " + mExitingAnimation);
        if (mExitingAnimation == null) {
            playExitingAnimation();
        }
    }

    private void playExitingAnimation() {
        Log.v(TAG, "playExitingAnimation");

        mAnimatedImage.setVisibility(View.VISIBLE);
        mImageTo.setVisibility(View.INVISIBLE);

        AnimatorSet imageAnimatorSet = createExitingImageAnimation();

        Animator mainContainerFadeAnimator = createExitingFadeAnimator();

        mExitingAnimation = new AnimatorSet();
        mExitingAnimation.setDuration(IMAGE_TRANSLATION_DURATION);
        mExitingAnimation.setInterpolator(new AccelerateInterpolator());
        mExitingAnimation.addListener(new SimpleAnimationListener() {

            @Override
            public void onAnimationEnd(Animator animation) {


                Log.v(TAG, "onAnimationEnd, mExitingAnimation " + mExitingAnimation);
                mExitingAnimation = null;

                // finish the activity when animation is finished
                Activity activity = (Activity) mAnimatedImage.getContext();
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        });

        mExitingAnimation.playTogether(
                imageAnimatorSet,
                mainContainerFadeAnimator
        );

        mExitingAnimation.start();
    }

    /**
     * This method creates an animator set of 2 animations:
     * 1. ImageView position animation when screen is closed
     * 2. ImageView image matrix animation when screen is closed
     */
    private AnimatorSet createExitingImageAnimation() {
        Log.v(TAG, ">> createExitingImageAnimation");

        ObjectAnimator positionAnimator = createExitingImagePositionAnimator();
        ObjectAnimator matrixAnimator = createExitingImageMatrixAnimator();

        AnimatorSet exitingImageAnimation = new AnimatorSet();
        exitingImageAnimation.playTogether(positionAnimator, matrixAnimator);

        Log.v(TAG, "<< createExitingImageAnimation");
        return exitingImageAnimation;
    }

    /**
     * This method creates an animator that changes ImageView position on the screen.
     * It will look like view is translated from its position on this screen to its position on previous screen
     */
    @NonNull
    private ObjectAnimator createExitingImagePositionAnimator() {

        // get initial location on the screen and start animation from there
        int[] locationOnScreen = new int[2];
        mAnimatedImage.getLocationOnScreen(locationOnScreen);

        PropertyValuesHolder propertyLeft = PropertyValuesHolder.ofInt("left",
                locationOnScreen[0],
                mToLeft);

        PropertyValuesHolder propertyTop = PropertyValuesHolder.ofInt("top",
                locationOnScreen[1] - getStatusBarHeight(),
                mToTop - getStatusBarHeight());

        PropertyValuesHolder propertyRight = PropertyValuesHolder.ofInt("right",
                locationOnScreen[0] + mAnimatedImage.getWidth(),
                mToLeft + mToWidth);


        PropertyValuesHolder propertyBottom = PropertyValuesHolder.ofInt("bottom",
                mAnimatedImage.getBottom(),
                mToTop + mToHeight - getStatusBarHeight());

        return ObjectAnimator.ofPropertyValuesHolder(mAnimatedImage, propertyLeft, propertyTop, propertyRight, propertyBottom);
    }


    /**
     * This method creates animator that animates Matrix of ImageView.
     * It is needed in order to show the effect when scaling of one view is smoothly changed to the scale of the second view.
     * <p>
     * For example: first view can have scaleType: centerCrop, and the other one fitCenter.
     * The image inside ImageView will smoothly change from one to another
     */
    private ObjectAnimator createExitingImageMatrixAnimator() {

        Matrix initialMatrix = MatrixUtils.getImageMatrix(mAnimatedImage);

        Matrix endMatrix = new Matrix();
        endMatrix.setValues(mToThumbnailMatrixValues);

        Log.v(TAG, "createExitingImageMatrixAnimator, initialMatrix " + initialMatrix);
        Log.v(TAG, "createExitingImageMatrixAnimator,     endMatrix " + endMatrix);

        mAnimatedImage.setScaleType(ImageView.ScaleType.MATRIX);

        return ObjectAnimator.ofObject(mAnimatedImage, MatrixEvaluator.ANIMATED_TRANSFORM_PROPERTY,
                new MatrixEvaluator(), initialMatrix, endMatrix);
    }

    private ObjectAnimator createExitingFadeAnimator() {
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(mMainContainer,
                "alpha", 1.0f, 0.0f);
        fadeInAnimator.getPropertyName();
        return fadeInAnimator;
    }

}
