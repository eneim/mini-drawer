/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eneim on 2/21/16.
 */
public class MiniDrawerLayout extends ViewGroup {

  /**
   * Indicates that any drawers are in an idle, settled state. No animation is in progress.
   */
  public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;
  /**
   * Indicates that a drawer is currently being dragged by the user.
   */
  public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;
  /**
   * Indicates that a drawer is in the process of settling to a final position.
   */
  public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;
  private static final float TOUCH_SLOP_SENSITIVITY = 1.f;
  /**
   * Minimum velocity that will be detected as a fling
   */
  private static final int MIN_FLING_VELOCITY = 400; // dips per second

  private static final int[] LAYOUT_ATTRS = new int[] {
      android.R.attr.layout_gravity
  };

  /**
   * Whether we can use NO_HIDE_DESCENDANTS accessibility importance.
   */
  private static final boolean CAN_HIDE_DESCENDANTS = Build.VERSION.SDK_INT >= 19;

  private int mCollapseWidth;
  private int mExpandWidth;
  private ViewDragHelper mDragHelper;

  private View mMenuView;
  private View mSlideView;

  private float mInitialMotionX;
  private float mInitialMotionY;
  private DrawerLayout.DrawerListener mListener;
  private boolean mInLayout;
  private boolean mFirstLayout = true;
  private int mDrawerState;

  private final ArrayList<View> mNonDrawerViews;

  public MiniDrawerLayout(Context context) {
    this(context, null);
  }

  public MiniDrawerLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MiniDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, R.style.Widget_Eneim_MiniDrawer);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MiniDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    // as recommended
    setWillNotDraw(false);
    // Custom attributes
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MiniDrawerLayout, defStyleAttr,
        defStyleRes);
    mCollapseWidth = a.getDimensionPixelSize(R.styleable.MiniDrawerLayout_collapseWidth, 0);
    mExpandWidth = a.getDimensionPixelSize(R.styleable.MiniDrawerLayout_expandWidth, 0);

    a.recycle();

    // View dragger
    final float density = getResources().getDisplayMetrics().density;
    final float minVel = MIN_FLING_VELOCITY * density;

    ViewDragCallback mDraggerCallback = new ViewDragCallback();
    mDragHelper = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mDraggerCallback);
    mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    mDragHelper.setMinVelocity(minVel);

    mDraggerCallback.setDragger(mDragHelper);

    // So that we can catch the back button
    setFocusableInTouchMode(true);

    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);

    ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate());
    ViewGroupCompat.setMotionEventSplittingEnabled(this, false);

    mNonDrawerViews = new ArrayList<>();
  }

  private static boolean includeChildForAccessibility(View child) {
    // If the child is not important for accessibility we make
    // sure this hides the entire subtree rooted at it as the
    // IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS is not
    // supported on older platforms but we want to hide the entire
    // content and not opened drawers if a drawer is opened.
    return ViewCompat.getImportantForAccessibility(child)
        != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        && ViewCompat.getImportantForAccessibility(child)
        != ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
      if (isInEditMode()) {
        // Don't crash the layout editor. Consume all of the space if specified
        // or pick a magic number from thin air otherwise.
        // TODO Better communication with tools of this bogus state.
        // It will crash on a real device.
        if (widthMode == MeasureSpec.AT_MOST) {
          widthMode = MeasureSpec.EXACTLY;
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
          widthMode = MeasureSpec.EXACTLY;
          widthSize = 300;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
          heightMode = MeasureSpec.EXACTLY;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
          heightMode = MeasureSpec.EXACTLY;
          heightSize = 300;
        }
      } else {
        throw new IllegalArgumentException(
            "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
      }
    }

    setMeasuredDimension(widthSize, heightSize);

    // Gravity value for each drawer we've seen. Only one of each permitted.
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);

      if (child.getVisibility() == GONE) {
        continue;
      }

      final LayoutParams lp = (LayoutParams) child.getLayoutParams();

      // The menu panel
      if (isContentView(child)) {
        // Measure by collapseWidth and expandWidth and its defined Width
        int childWidthSpec =
            getChildMeasureSpec(widthMeasureSpec, lp.leftMargin + lp.rightMargin, lp.width);

        final int contentWidthSpec =
            MeasureSpec.makeMeasureSpec(Math.min(MeasureSpec.getSize(childWidthSpec) -
                lp.leftMargin - lp.rightMargin, mExpandWidth), MeasureSpec.EXACTLY);
        // re-claim the expandWidth by menu panel width
        mExpandWidth = MeasureSpec.getSize(contentWidthSpec);
        // Set to parent's height spec.
        final int contentHeightSpec =
            MeasureSpec.makeMeasureSpec(heightSize - lp.topMargin - lp.bottomMargin,
                MeasureSpec.EXACTLY);
        // measure by new set of spec
        child.measure(contentWidthSpec, contentHeightSpec);
      } else if (isDrawerView(child)) { // The sliding panel
        final int drawerWidthSpec =
            getChildMeasureSpec(widthMeasureSpec, mCollapseWidth + lp.leftMargin + lp.rightMargin,
                lp.width);
        final int drawerHeightSpec =
            getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height);
        child.measure(drawerWidthSpec, drawerHeightSpec);
      } else {
        throw new IllegalStateException("Child " + child + " at index " + i +
            " is not a valid child");
      }
    }
  }

  // Actually the Menu View
  boolean isContentView(View child) {
    return ((LayoutParams) child.getLayoutParams()).gravity == GravityCompat.START;
  }

  boolean isDrawerView(View child) {
    final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
    final int absGravity =
        GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(child));
    return (absGravity & (Gravity.RIGHT)) == Gravity.RIGHT;
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    mInLayout = true;
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);

      if (child.getVisibility() == GONE) {
        continue;
      }

      final LayoutParams lp = (LayoutParams) child.getLayoutParams();

      if (isContentView(child)) {
        child.layout(lp.leftMargin, lp.topMargin, lp.leftMargin + child.getMeasuredWidth(),
            lp.topMargin + child.getMeasuredHeight());
      } else { // Drawer, if it wasn't onMeasure would have thrown an exception.
        final int childWidth = child.getMeasuredWidth();
        final int childHeight = child.getMeasuredHeight();
        int childLeft;

        final float newOffset;
        childLeft = mCollapseWidth + (int) (lp.onScreen * (mExpandWidth - mCollapseWidth));
        // (float) (left - mCollapseWidth) / range
        newOffset = (float) (childLeft - mCollapseWidth) / (mExpandWidth - mCollapseWidth);

        final boolean changeOffset = newOffset != lp.onScreen;

        final int vgrav = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;

        switch (vgrav) {
          default:
          case Gravity.TOP: {
            child.layout(childLeft, lp.topMargin, childLeft + childWidth,
                lp.topMargin + childHeight);
            break;
          }

          case Gravity.BOTTOM: {
            final int height = b - t;
            child.layout(childLeft, height - lp.bottomMargin - child.getMeasuredHeight(),
                childLeft + childWidth, height - lp.bottomMargin);
            break;
          }

          case Gravity.CENTER_VERTICAL: {
            final int height = b - t;
            int childTop = (height - childHeight) / 2;

            // Offset for margins. If things don't fit right because of
            // bad measurement before, oh well.
            if (childTop < lp.topMargin) {
              childTop = lp.topMargin;
            } else if (childTop + childHeight > height - lp.bottomMargin) {
              childTop = height - lp.bottomMargin - childHeight;
            }
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            break;
          }
        }

        if (changeOffset) {
          setDrawerViewOffset(child, newOffset);
        }
      }
    }
    mInLayout = false;
    mFirstLayout = false;
  }

  @Override protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams ? new LayoutParams((LayoutParams) p)
        : p instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((MarginLayoutParams) p)
            : new LayoutParams(p);
  }

  @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams && super.checkLayoutParams(p);
  }

  @Override public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
    if (getDescendantFocusability() == FOCUS_BLOCK_DESCENDANTS) {
      return;
    }

    // Only the views in the open drawers are focusables. Add normal child views when
    // no drawers are opened.
    final int childCount = getChildCount();
    boolean isDrawerOpen = false;
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (isDrawerView(child)) {
        if (isDrawerOpen(child)) {
          isDrawerOpen = true;
          child.addFocusables(views, direction, focusableMode);
        }
      } else {
        mNonDrawerViews.add(child);
      }
    }

    if (!isDrawerOpen) {
      final int nonDrawerViewsCount = mNonDrawerViews.size();
      for (int i = 0; i < nonDrawerViewsCount; ++i) {
        final View child = mNonDrawerViews.get(i);
        if (child.getVisibility() == View.VISIBLE) {
          child.addFocusables(views, direction, focusableMode);
        }
      }
    }

    mNonDrawerViews.clear();
  }

  void setDrawerViewOffset(View drawerView, float slideOffset) {
    final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
    if (slideOffset == lp.onScreen) {
      return;
    }

    lp.onScreen = slideOffset;
    dispatchOnDrawerSlide(drawerView, slideOffset);
  }

  void dispatchOnDrawerSlide(View drawerView, float slideOffset) {
    if (mListener != null) {
      mListener.onDrawerSlide(drawerView, slideOffset);
    }

    if (slideOffset >= 1.f) {
      updateChildrenImportantForAccessibility(drawerView, false);
    }
  }

  private void updateChildrenImportantForAccessibility(View drawerView, boolean isDrawerOpen) {
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (!isDrawerOpen && !isDrawerView(child) || isDrawerOpen && child == drawerView) {
        // Drawer is closed and this is a content view or this is an
        // open drawer view, so it should be visible.
        ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
      } else {
        ViewCompat.setImportantForAccessibility(child,
            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
      }
    }
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    final int action = MotionEventCompat.getActionMasked(ev);

    // "|" used deliberately here; both methods should be invoked.
    final boolean interceptForDrag = mDragHelper.shouldInterceptTouchEvent(ev);

    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();
        mInitialMotionX = x;
        mInitialMotionY = y;
        // final View child = mDragHelper.findTopChildUnder((int) x, (int) y);
        break;
      }

      case MotionEvent.ACTION_MOVE: {
        // If we cross the touch slop, don't perform the delayed peek for an edge touch.
        if (mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_ALL)) {
        }
        break;
      }

      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP: {

      }
    }

    return interceptForDrag;
  }

  @Override protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    final int restoreCount = canvas.save();
    final boolean result = super.drawChild(canvas, child, drawingTime);
    canvas.restoreToCount(restoreCount);
    return result;
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mFirstLayout = true;
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    mFirstLayout = true;
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    mDragHelper.processTouchEvent(ev);

    final int action = ev.getAction();
    boolean wantTouchEvents = true;

    switch (action & MotionEventCompat.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();
        mInitialMotionX = x;
        mInitialMotionY = y;
        break;
      }

      case MotionEvent.ACTION_UP: {
        final float x = ev.getX();
        final float y = ev.getY();
        final View touchedView = mDragHelper.findTopChildUnder((int) x, (int) y);
        if (touchedView != null && isContentView(touchedView)) {
          final float dx = x - mInitialMotionX;
          final float dy = y - mInitialMotionY;
          final int slop = mDragHelper.getTouchSlop();
          if (dx * dx + dy * dy < slop * slop) {
            // Taps close a dimmed open drawer but only if it isn't locked open.
          }
        }
        break;
      }

      case MotionEvent.ACTION_CANCEL: {
        break;
      }
    }

    return wantTouchEvents;
  }

  /**
   * Open the specified drawer view by animating it into view.
   */
  public void openDrawer() {
    if (mSlideView == null) {
      throw new IllegalArgumentException("Drawer not found");
    }

    if (mFirstLayout) {
      final LayoutParams lp = (LayoutParams) mSlideView.getLayoutParams();
      lp.onScreen = 1.f;
      lp.knownOpen = true;

      updateChildrenImportantForAccessibility(mSlideView, true);
    } else {
      mDragHelper.smoothSlideViewTo(mSlideView, mExpandWidth, mSlideView.getTop());
    }

    invalidate();
  }

  /**
   * Close the specified drawer view by animating it into view.
   */
  public void closeDrawer() {
    if (mSlideView == null) {
      throw new IllegalArgumentException("Drawer not found");
    }

    if (mFirstLayout) {
      final LayoutParams lp = (LayoutParams) mSlideView.getLayoutParams();
      lp.onScreen = 0.f;
      lp.knownOpen = false;
    } else {
      mDragHelper.smoothSlideViewTo(mSlideView, mCollapseWidth, mSlideView.getTop());
    }

    invalidate();
  }

  @Override public void computeScroll() {
    if (mDragHelper.continueSettling(true)) {
      ViewCompat.postInvalidateOnAnimation(this);
    }
    super.computeScroll();
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    int childCount = getChildCount();
    if (childCount != 2) {
      throw new IllegalArgumentException("This layout requires exactly 2 children Views");
    }

    for (int i = 0; i < childCount; i++) {
      View view = getChildAt(i);
      if (isContentView(view)) {
        mMenuView = view;
      } else if (isDrawerView(view)) {
        mSlideView = view;
      }

      if (mMenuView != null && mSlideView != null) {
        // 1. Reset Menu elevation. This View should lay on bottom of the View Groups
        ViewCompat.setElevation(mMenuView, 0);
        // 2. Now set a good-looking elevation for Sliding view
        float additionElevation =
            getContext().getResources().getDimensionPixelSize(R.dimen.addition_elevation);
        ViewCompat.setElevation(mSlideView, additionElevation);
        break;
      }
    }
  }

  @Override public void requestLayout() {
    if (!mInLayout) {
      super.requestLayout();
    }
  }

  /**
   * Check if the given drawer view is currently in an open state.
   * To be considered "open" the drawer must have settled into its fully
   * visible state. To check for partial visibility use
   * {@link #isDrawerVisible(android.view.View)}.
   *
   * @param drawer Drawer view to check
   * @return true if the given drawer view is in an open state
   * @see #isDrawerVisible(android.view.View)
   */
  public boolean isDrawerOpen(View drawer) {
    if (!isDrawerView(drawer)) {
      throw new IllegalArgumentException("View " + drawer + " is not a drawer");
    }
    return ((LayoutParams) drawer.getLayoutParams()).knownOpen;
  }

  public boolean isDrawerOpen() {
    return isDrawerView(mSlideView);
  }

  float getDrawerViewOffset(View drawerView) {
    return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
  }

  /**
   * Set a listener to be notified of drawer events.
   *
   * @param listener Listener to notify when drawer events occur
   * @see DrawerLayout.DrawerListener
   */
  public void setDrawerListener(DrawerLayout.DrawerListener listener) {
    mListener = listener;
  }

  private View findVisibleDrawer() {
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (isDrawerView(child) && isDrawerVisible(child)) {
        return child;
      }
    }
    return null;
  }

  public boolean isDrawerVisible(View drawer) {
    if (!isDrawerView(drawer)) {
      throw new IllegalArgumentException("View " + drawer + " is not a drawer");
    }
    return ((LayoutParams) drawer.getLayoutParams()).onScreen > 0;
  }

  public boolean isDrawerVisible() {
    return isDrawerVisible(mSlideView);
  }

  /**
   * Resolve the shared state of all drawers from the component ViewDragHelpers.
   * Should be called whenever a ViewDragHelper's state changes.
   */
  void updateDrawerState(@State int activeState, View activeDrawer) {
    final int leftState = mDragHelper.getViewDragState();

    final int state;
    if (leftState == STATE_DRAGGING) {
      state = STATE_DRAGGING;
    } else if (leftState == STATE_SETTLING) {
      state = STATE_SETTLING;
    } else {
      state = STATE_IDLE;
    }

    if (activeDrawer != null && activeState == STATE_IDLE) {
      final LayoutParams lp = (LayoutParams) activeDrawer.getLayoutParams();
      if (lp.onScreen == 0) {
        dispatchOnDrawerClosed(activeDrawer);
      } else if (lp.onScreen == 1) {
        dispatchOnDrawerOpened(activeDrawer);
      }
    }

    if (state != mDrawerState) {
      mDrawerState = state;

      if (mListener != null) {
        mListener.onDrawerStateChanged(state);
      }
    }
  }

  void dispatchOnDrawerClosed(View drawerView) {
    final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
    if (lp.knownOpen) {
      lp.knownOpen = false;
      if (mListener != null) {
        mListener.onDrawerClosed(drawerView);
      }

      updateChildrenImportantForAccessibility(drawerView, false);

      // Only send WINDOW_STATE_CHANGE if the host has window focus. This
      // may change if support for multiple foreground windows (e.g. IME)
      // improves.
      if (hasWindowFocus()) {
        final View rootView = getRootView();
        if (rootView != null) {
          rootView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
      }
    }
  }

  void dispatchOnDrawerOpened(View drawerView) {
    final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
    if (!lp.knownOpen) {
      lp.knownOpen = true;
      if (mListener != null) {
        mListener.onDrawerOpened(drawerView);
      }

      updateChildrenImportantForAccessibility(drawerView, true);

      // Only send WINDOW_STATE_CHANGE if the host has window focus.
      if (hasWindowFocus()) {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
      }

      drawerView.requestFocus();
    }
  }

  @IntDef({ STATE_IDLE, STATE_DRAGGING, STATE_SETTLING }) @Retention(RetentionPolicy.SOURCE)
  private @interface State {
  }

  public static class LayoutParams extends ViewGroup.MarginLayoutParams {

    public int gravity = Gravity.NO_GRAVITY;
    float onScreen;
    boolean knownOpen;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);

      final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
      this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int gravity) {
      this(width, height);
      this.gravity = gravity;
    }

    public LayoutParams(LayoutParams source) {
      super(source);
      this.gravity = source.gravity;
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(ViewGroup.MarginLayoutParams source) {
      super(source);
    }
  }

  private class ViewDragCallback extends ViewDragHelper.Callback {

    private ViewDragHelper mDragger;

    public ViewDragCallback() {
    }

    public void setDragger(ViewDragHelper dragger) {
      mDragger = dragger;
    }

    @Override public void onViewDragStateChanged(int state) {
      updateDrawerState(state, mDragger.getCapturedView());
    }

    @Override
    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
      int range = mMenuView.getWidth() - mCollapseWidth;
      if (range == 0) {
        return;
      }

      float offset;
      offset = (float) (left - mCollapseWidth) / range;
      setDrawerViewOffset(changedView, offset);
    }

    @Override public void onViewCaptured(View capturedChild, int activePointerId) {
      super.onViewCaptured(capturedChild, activePointerId);
    }

    @Override public void onViewReleased(View releasedChild, float xvel, float yvel) {
      // Offset is how open the drawer is, therefore left/right values
      // are reversed from one another.
      final float offset = getDrawerViewOffset(releasedChild);
      int left;
      left = xvel > 0 || xvel == 0 && offset > 0.5f ? mMenuView.getWidth() : mCollapseWidth;
      mDragger.settleCapturedViewAt(left, releasedChild.getTop());
      invalidate();
    }

    @Override public int getViewHorizontalDragRange(View child) {
      return isDrawerView(child) ? //
          Math.max(child.getWidth() - (getWidth() - mMenuView.getWidth()), 0) : 0;
    }

    @Override public boolean tryCaptureView(View child, int pointerId) {
      // Only capture views where the gravity matches what we're looking for.
      // This lets us use two ViewDragHelpers, one for each side drawer.
      return isDrawerView(child);
    }

    @Override public int clampViewPositionHorizontal(View child, int left, int dx) {
      final int width = getWidth();
      return Math.max(width - child.getWidth(), Math.min(left, mMenuView.getRight()));
    }

    @Override public int clampViewPositionVertical(View child, int top, int dy) {
      return child.getTop();
    }
  }

  // Copy paste from DrawerLayout
  class AccessibilityDelegate extends AccessibilityDelegateCompat {
    private final Rect mTmpRect = new Rect();

    @Override
    public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
      // Special case to handle window state change events. As far as
      // accessibility services are concerned, state changes from
      // DrawerLayout invalidate the entire contents of the screen (like
      // an Activity or Dialog) and they should announce the title of the
      // new content.
      if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        final List<CharSequence> eventText = event.getText();
        final View visibleDrawer = findVisibleDrawer();
        if (visibleDrawer != null) {
          // Hmm
        }

        return true;
      }

      return super.dispatchPopulateAccessibilityEvent(host, event);
    }

    @Override public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
      super.onInitializeAccessibilityEvent(host, event);

      event.setClassName(DrawerLayout.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
      if (CAN_HIDE_DESCENDANTS) {
        super.onInitializeAccessibilityNodeInfo(host, info);
      } else {
        // Obtain a node for the host, then manually generate the list
        // of children to only include non-obscured views.
        final AccessibilityNodeInfoCompat superNode = AccessibilityNodeInfoCompat.obtain(info);
        super.onInitializeAccessibilityNodeInfo(host, superNode);

        info.setSource(host);
        final ViewParent parent = ViewCompat.getParentForAccessibility(host);
        if (parent instanceof View) {
          info.setParent((View) parent);
        }
        copyNodeInfoNoChildren(info, superNode);
        superNode.recycle();

        addChildrenForAccessibility(info, (ViewGroup) host);
      }

      info.setClassName(DrawerLayout.class.getName());

      // This view reports itself as focusable so that it can intercept
      // the back button, but we should prevent this view from reporting
      // itself as focusable to accessibility services.
      info.setFocusable(false);
      info.setFocused(false);
      info.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_FOCUS);
      info.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLEAR_FOCUS);
    }

    /**
     * This should really be in AccessibilityNodeInfoCompat, but there unfortunately
     * seem to be a few elements that are not easily cloneable using the underlying API.
     * Leave it private here as it's not general-purpose useful.
     */
    private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat dest,
        AccessibilityNodeInfoCompat src) {
      final Rect rect = mTmpRect;

      src.getBoundsInParent(rect);
      dest.setBoundsInParent(rect);

      src.getBoundsInScreen(rect);
      dest.setBoundsInScreen(rect);

      dest.setVisibleToUser(src.isVisibleToUser());
      dest.setPackageName(src.getPackageName());
      dest.setClassName(src.getClassName());
      dest.setContentDescription(src.getContentDescription());

      dest.setEnabled(src.isEnabled());
      dest.setClickable(src.isClickable());
      dest.setFocusable(src.isFocusable());
      dest.setFocused(src.isFocused());
      dest.setAccessibilityFocused(src.isAccessibilityFocused());
      dest.setSelected(src.isSelected());
      dest.setLongClickable(src.isLongClickable());

      dest.addAction(src.getActions());
    }

    private void addChildrenForAccessibility(AccessibilityNodeInfoCompat info, ViewGroup v) {
      final int childCount = v.getChildCount();
      for (int i = 0; i < childCount; i++) {
        final View child = v.getChildAt(i);
        if (includeChildForAccessibility(child)) {
          info.addChild(child);
        }
      }
    }

    @Override public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
        AccessibilityEvent event) {
      if (CAN_HIDE_DESCENDANTS || includeChildForAccessibility(child)) {
        return super.onRequestSendAccessibilityEvent(host, child, event);
      }
      return false;
    }
  }

}
