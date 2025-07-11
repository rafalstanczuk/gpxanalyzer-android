package com.itservices.gpxanalyzer.core.ui.components.fab;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * A custom Floating Action Button component that manages a primary FAB and
 * reveals/hides secondary FABs that are already defined in the parent layout.
 * <p>
 * This version does not inflate its own layout but instead uses existing FABs.
 */
public class SpeedDialFabView extends FloatingActionButton {
    private static final String TAG = "SpeedDialFabView";

    // Animation constants
    private static final long ANIMATION_DURATION = 800; // milliseconds
    private static final float SECONDARY_FAB_ELEVATION = 11f;

    // Static registry of all instances to ensure only one menu is open at a time
    private static final WeakHashMap<SpeedDialFabView, Boolean> ALL_INSTANCES = new WeakHashMap<>();
    private static SpeedDialFabView currentlyOpenInstance = null;

    // Private state
    private final List<FloatingActionButton> secondaryFabs = new ArrayList<>();
    private final PublishSubject<Integer> actionClicks = PublishSubject.create();
    private boolean isMenuOpen = false;
    private UnfoldDirection unfoldDirection = UnfoldDirection.UP;
    private float translationDistance = 180f; // Default distance in dp

    private float mainFabRotateDegrees = 45f;
    private float secondaryFabsTargetAlpha = 1.0f;

    // Constructors
    public SpeedDialFabView(@NonNull Context context) {
        super(context);
        init();
    }

    public SpeedDialFabView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedDialFabView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Register this instance in the static registry
        ALL_INSTANCES.put(this, Boolean.TRUE);

        // Listen for clicks on this main FAB
        setOnClickListener(v -> toggleMenu());

        // Set default translation distance
        translationDistance = dpToPx(60f);

        Log.d(TAG, "SpeedDialFabView initialized");
    }

    // Public Configuration Methods

    /**
     * Sets the icon for the main FAB.
     *
     * @param resId Resource ID of the icon drawable
     */
    public void setMainFabIconResource(@DrawableRes int resId) {
        Log.d(TAG, "Setting main FAB icon resource: " + resId);
        setImageResource(resId);
    }

    /**
     * Sets the unfold direction for secondary FABs.
     *
     * @param direction The direction to unfold FABs (UP, DOWN, LEFT, RIGHT)
     */
    public void setUnfoldDirection(UnfoldDirection direction) {
        Log.d(TAG, "Setting unfold direction: " + direction);
        this.unfoldDirection = direction;
    }

    /**
     * Sets the distance each secondary FAB travels from the main FAB.
     *
     * @param distanceDp Distance in Density-independent Pixels (dp)
     */
    public void setTranslationDistanceDp(float distanceDp) {
        Log.d(TAG, "Setting translation distance (dp): " + distanceDp);
        this.translationDistance = dpToPx(distanceDp);
    }

    public void bindActionsToExistingFabs(List<FloatingActionButton> fabActionList) {
        Log.d(TAG, "Binding actions to existing FABs, count: " + fabActionList.size());

        // First, clear any existing bindings
        secondaryFabs.clear();

        // Find parent ViewGroup to locate the FABs
        ViewGroup parent = findParentConstraintLayout();

        if (parent == null) {
            Log.e(TAG, "Could not find parent ConstraintLayout to locate FABs");
            return;
        }

        for (FloatingActionButton fab : fabActionList) {

            // Store reference to FAB
            secondaryFabs.add(fab);

            // Configure the FAB
            fab.setVisibility(View.GONE);
            fab.setAlpha(0f);
            fab.setScaleX(0f);
            fab.setScaleY(0f);

            // Make sure the click ripple effect is visible
            fab.setRippleColor(getResources().getColorStateList(android.R.color.darker_gray, null));
        }

        Log.d(TAG, "Successfully bound " + secondaryFabs.size() + " FABs to actions");
    }

    /**
     * Returns an Observable that emits action IDs when secondary FABs are clicked.
     *
     * @return Observable of action IDs
     */
    public Observable<Integer> observeActionClicks() {
        return actionClicks.hide();
    }

    // Animation Logic

    /**
     * Toggles the menu open or closed.
     */
    public void toggleMenu() {
        Log.d(TAG, "Toggling menu, current state: " + (isMenuOpen ? "open" : "closed"));
        if (isMenuOpen) {
            closeMenu();
        } else {
            closeAllOtherInstances();
            openMenu();
        }
    }

    private void openMenu() {
        isMenuOpen = true;
        currentlyOpenInstance = this;
        ensureProperZOrder();

        // Rotate this main FAB
        rotateMainFab(true);

        // Animate secondary FABs
        animateSecondaryFabs(true);
    }

    private void closeAllOtherInstances() {
        if (currentlyOpenInstance != null && currentlyOpenInstance != this && currentlyOpenInstance.isMenuOpen) {
            Log.d(TAG, "Closing another open SpeedDialFabView instance");
            currentlyOpenInstance.closeMenu();
        }
    }

    private void ensureProperZOrder() {
        // Bring this view to front
        bringToFront();

        // Find parent ViewGroup where multiple SpeedDialFabViews might be present
        ViewGroup rootView = findRootViewGroup();
        if (rootView != null) {
            bringToFront();
        }
    }

    private ViewGroup findRootViewGroup() {
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup viewGroup) {
            while (viewGroup.getParent() instanceof ViewGroup) {
                viewGroup = (ViewGroup) viewGroup.getParent();
                if (viewGroup.getClass().getSimpleName().contains("RecyclerView")) {
                    return viewGroup;
                }
            }
            return viewGroup;
        }
        return null;
    }

    private ConstraintLayout findParentConstraintLayout() {
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof ConstraintLayout) {
                return (ConstraintLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Closes the menu, collapsing all secondary FABs.
     */
    public void closeMenu() {
        if (!isMenuOpen) {
            return;
        }

        isMenuOpen = false;
        if (currentlyOpenInstance == this) {
            currentlyOpenInstance = null;
        }

        // Rotate main FAB back
        rotateMainFab(false);

        // Animate secondary FABs back
        animateSecondaryFabs(false);
    }

    private void rotateMainFab(boolean opening) {
        float targetRotation = opening ? mainFabRotateDegrees : 0f;

        this.animate()
                .rotation(targetRotation)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    private void animateSecondaryFabs(boolean opening) {
        for (int i = 0; i < secondaryFabs.size(); i++) {
            FloatingActionButton fab = secondaryFabs.get(i);

            // Determine position for staggered animation
            int position = i + 1;

            if (opening) {
                animateFabOpening(fab, position);
            } else {
                animateFabClosing(fab);
            }
        }
    }

    private void animateFabOpening(FloatingActionButton fab, int position) {
        float targetRotation = 0f;

        // Calculate position
        float[] translations = calculateTranslation(position);
        float targetTranslationX = translations[0];
        float targetTranslationY = translations[1];

        // Make visible and prepare for animation
        fab.setVisibility(View.VISIBLE);
        fab.setAlpha(0f);
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.setTranslationX(0f);
        fab.setTranslationY(0f);
        fab.setCompatElevation(SECONDARY_FAB_ELEVATION);

        // Enable interaction
        setFabEnabled(fab, true);

        // Animate
        fab.animate()
                .translationX(targetTranslationX)
                .translationY(targetTranslationY)
                .rotation(targetRotation)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(secondaryFabsTargetAlpha)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> setFabEnabled(fab, true))
                .start();
    }

    private void animateFabClosing(FloatingActionButton fab) {
        float targetRotation = -90.0f;


        fab.animate()
                .translationX(0f)
                .translationY(0f)
                .rotation(targetRotation)
                .scaleX(0.0f)
                .scaleY(0.0f)
                .alpha(0.0f)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> {
                    fab.setVisibility(View.GONE);
                    setFabEnabled(fab, false);
                })
                .start();
    }

    private float[] calculateTranslation(int position) {
        float[] result = new float[2];
        float targetTranslationX = 0f;
        float targetTranslationY = 0f;
        float staggeredDistance = translationDistance * position;

        switch (unfoldDirection) {
            case UP:
                targetTranslationY = -staggeredDistance;
                break;
            case DOWN:
                targetTranslationY = staggeredDistance;
                break;
            case LEFT:
                targetTranslationX = -staggeredDistance;
                break;
            case RIGHT:
                targetTranslationX = staggeredDistance;
                break;
        }

        result[0] = targetTranslationX;
        result[1] = targetTranslationY;
        return result;
    }

    private void setFabEnabled(FloatingActionButton fab, boolean enabled) {
        fab.setClickable(enabled);
        fab.setFocusable(enabled);
        fab.setEnabled(enabled);
    }

    /**
     * Convert dp value to pixels
     */
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // Lifecycle Methods

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Re-register in case of recycling
        ALL_INSTANCES.put(this, Boolean.TRUE);

        // Request that parent doesn't clip
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup parentView) {
            parentView.setClipChildren(false);
            parentView.setClipToPadding(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Ensure menu is closed
        if (isMenuOpen) {
            closeMenu();
        }

        // Clean up registry
        ALL_INSTANCES.remove(this);
        if (currentlyOpenInstance == this) {
            currentlyOpenInstance = null;
        }

        super.onDetachedFromWindow();
    }

    public void setMainFabRotateDegrees(float degrees) {
        mainFabRotateDegrees = degrees;
    }

    public void setSecondaryFabsTargetAlpha(float secondaryFabsTargetAlpha) {
        this.secondaryFabsTargetAlpha = secondaryFabsTargetAlpha;
    }
}