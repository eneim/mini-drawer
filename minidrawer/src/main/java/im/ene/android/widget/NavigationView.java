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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.internal.NavigationMenu;
import android.support.design.internal.NavigationMenuItemView;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.internal.NavigationMenuView;
import android.support.design.internal.ScrimInsetsFrameLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by eneim on 2/23/16.
 */
public class NavigationView extends ScrimInsetsFrameLayout {

  private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
  private static final int[] DISABLED_STATE_SET = { -android.R.attr.state_enabled };

  private static final int PRESENTER_NAVIGATION_VIEW_ID = 1;

  private final NavigationMenu mMenu;
  private final NavigationMenuPresenter mPresenter = new NavigationMenuPresenter();
  private final NavigationMenuView mMenuView;
  private final LinearLayoutManager mLayoutManager;

  private OnNavigationItemSelectedListener mListener;
  private int mMaxWidth;

  private MenuInflater mMenuInflater;

  public NavigationView(Context context) {
    this(context, null);
  }

  public NavigationView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ThemeUtils.checkAppCompatTheme(context);

    // Create the menu
    mMenu = new NavigationMenu(context);

    // Custom attributes
    TypedArray a =
        context.obtainStyledAttributes(attrs, android.support.design.R.styleable.NavigationView,
            defStyleAttr, android.support.design.R.style.Widget_Design_NavigationView);

    //noinspection deprecation
    setBackgroundDrawable(
        a.getDrawable(android.support.design.R.styleable.NavigationView_android_background));
    if (a.hasValue(android.support.design.R.styleable.NavigationView_elevation)) {
      ViewCompat.setElevation(this,
          a.getDimensionPixelSize(android.support.design.R.styleable.NavigationView_elevation, 0));
    }
    ViewCompat.setFitsSystemWindows(this,
        a.getBoolean(android.support.design.R.styleable.NavigationView_android_fitsSystemWindows,
            false));

    mMaxWidth =
        a.getDimensionPixelSize(android.support.design.R.styleable.NavigationView_android_maxWidth,
            0);

    final ColorStateList itemIconTint;
    if (a.hasValue(android.support.design.R.styleable.NavigationView_itemIconTint)) {
      itemIconTint =
          a.getColorStateList(android.support.design.R.styleable.NavigationView_itemIconTint);
    } else {
      itemIconTint = createDefaultColorStateList(android.R.attr.textColorSecondary);
    }

    boolean textAppearanceSet = false;
    int textAppearance = 0;
    if (a.hasValue(android.support.design.R.styleable.NavigationView_itemTextAppearance)) {
      textAppearance =
          a.getResourceId(android.support.design.R.styleable.NavigationView_itemTextAppearance, 0);
      textAppearanceSet = true;
    }

    ColorStateList itemTextColor = null;
    if (a.hasValue(android.support.design.R.styleable.NavigationView_itemTextColor)) {
      itemTextColor =
          a.getColorStateList(android.support.design.R.styleable.NavigationView_itemTextColor);
    }

    if (!textAppearanceSet && itemTextColor == null) {
      // If there isn't a text appearance set, we'll use a default text color
      itemTextColor = createDefaultColorStateList(android.R.attr.textColorPrimary);
    }

    final Drawable itemBackground =
        a.getDrawable(android.support.design.R.styleable.NavigationView_itemBackground);

    mMenu.setCallback(new MenuBuilder.Callback() {
      @Override public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        return mListener != null && mListener.onNavigationItemSelected(item);
      }

      @Override public void onMenuModeChange(MenuBuilder menu) {
      }
    });
    mPresenter.setId(PRESENTER_NAVIGATION_VIEW_ID);
    mPresenter.initForMenu(context, mMenu);
    mPresenter.setItemIconTintList(itemIconTint);
    if (textAppearanceSet) {
      mPresenter.setItemTextAppearance(textAppearance);
    }
    mPresenter.setItemTextColor(itemTextColor);
    mPresenter.setItemBackground(itemBackground);
    mMenu.addMenuPresenter(mPresenter);
    mMenuView = (NavigationMenuView) mPresenter.getMenuView(this);
    addView(mMenuView);

    mLayoutManager = (LinearLayoutManager) mMenuView.getLayoutManager();

    if (a.hasValue(android.support.design.R.styleable.NavigationView_menu)) {
      inflateMenu(a.getResourceId(android.support.design.R.styleable.NavigationView_menu, 0));
    }

    if (a.hasValue(android.support.design.R.styleable.NavigationView_headerLayout)) {
      inflateHeaderView(
          a.getResourceId(android.support.design.R.styleable.NavigationView_headerLayout, 0));
    }

    a.recycle();
  }

  @Override protected void onDetachedFromWindow() {
    mItemHelpers.clear();
    super.onDetachedFromWindow();
  }

  @Override protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState state = new SavedState(superState);
    state.menuState = new Bundle();
    mMenu.savePresenterStates(state.menuState);
    return state;
  }

  @Override protected void onRestoreInstanceState(Parcelable savedState) {
    SavedState state = (SavedState) savedState;
    super.onRestoreInstanceState(state.getSuperState());
    mMenu.restorePresenterStates(state.menuState);
  }

  /**
   * Set a listener that will be notified when a menu item is clicked.
   *
   * @param listener The listener to notify
   */
  public void setNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
    mListener = listener;
  }

  public final void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset) {
    if (mLayoutManager == null) {
      return;
    }

    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
    int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
    for (int i = firstVisibleItem; i <= lastVisibleItem; i++) {
      RecyclerView.ViewHolder viewHolder = mMenuView.findViewHolderForAdapterPosition(i);
      int itemId = -1;
      int groupId = -1;
      if (viewHolder != null) {
        if (viewHolder.itemView instanceof NavigationMenuItemView) {
          MenuItemImpl item = ((NavigationMenuItemView) viewHolder.itemView).getItemData();
          itemId = item.getItemId();
          groupId = item.getGroupId();
        }
        getItemViewHelperInternal(viewHolder).onDrawerOffset(groupId, itemId, offset);
      }
    }
  }

  private static final String TAG = "NavigationView";

  /* From NavigationMenuPresenter$NavigationMenuAdapter */
  public static final int VIEW_TYPE_NORMAL = 0;
  public static final int VIEW_TYPE_SUBHEADER = 1;
  public static final int VIEW_TYPE_SEPARATOR = 2;
  public static final int VIEW_TYPE_HEADER = 3;

  @IntDef({ VIEW_TYPE_NORMAL, VIEW_TYPE_SUBHEADER, VIEW_TYPE_SEPARATOR, VIEW_TYPE_HEADER })
  @Retention(RetentionPolicy.SOURCE) protected @interface ItemViewType {
  }

  private final Map<Integer, MenuItemHelper> mItemHelpers = new HashMap<>();

  // Client should override this and give their custom Helper
  protected MenuItemHelper getItemViewHelper(@ItemViewType int viewType, View holder) {
    return null;
  }

  @NonNull final MenuItemHelper getItemViewHelperInternal(RecyclerView.ViewHolder viewHolder) {
    MenuItemHelper helper = mItemHelpers.get(viewHolder.getAdapterPosition());
    if (helper != null) {
      return helper;
    }

    @ItemViewType int viewType = viewHolder.getItemViewType();
    helper = getItemViewHelper(viewType, viewHolder.itemView);
    if (helper == null) {
      switch (viewType) {
        case VIEW_TYPE_NORMAL:
          helper = new NormalNavItemHelper(viewHolder.itemView);
          break;
        case VIEW_TYPE_SUBHEADER:
          helper = new SubHeaderNavItemHelper(viewHolder.itemView);
          break;
        case VIEW_TYPE_SEPARATOR:
          helper = new SeparatorNavViewHelper(viewHolder.itemView);
          break;
        case VIEW_TYPE_HEADER:
          helper = new HeaderNavViewHelper(viewHolder.itemView);
          break;
        default:
          throw new IllegalArgumentException("Non-supported navigation item type");
      }
    }

    mItemHelpers.put(viewHolder.getAdapterPosition(), helper);
    return helper;
  }

  @Override protected void onMeasure(int widthSpec, int heightSpec) {
    switch (View.MeasureSpec.getMode(widthSpec)) {
      case View.MeasureSpec.EXACTLY:
        // Nothing to do
        break;
      case View.MeasureSpec.AT_MOST:
        widthSpec = View.MeasureSpec.makeMeasureSpec(
            Math.min(View.MeasureSpec.getSize(widthSpec), mMaxWidth), View.MeasureSpec.EXACTLY);
        break;
      case View.MeasureSpec.UNSPECIFIED:
        widthSpec = View.MeasureSpec.makeMeasureSpec(mMaxWidth, View.MeasureSpec.EXACTLY);
        break;
    }
    // Let super sort out the height
    super.onMeasure(widthSpec, heightSpec);
  }

  /**
   * Inflate a menu resource into this navigation view.
   *
   * <p>Existing items in the menu will not be modified or removed.</p>
   *
   * @param resId ID of a menu resource to inflate
   */
  public void inflateMenu(int resId) {
    mPresenter.setUpdateSuspended(true);
    getMenuInflater().inflate(resId, mMenu);
    mPresenter.setUpdateSuspended(false);
    mPresenter.updateMenuView(false);
  }

  /**
   * Returns the {@link Menu} instance associated with this navigation view.
   */
  public Menu getMenu() {
    return mMenu;
  }

  /**
   * Inflates a View and add it as a header of the navigation menu.
   *
   * @param res The layout resource ID.
   * @return a newly inflated View.
   */
  public View inflateHeaderView(@LayoutRes int res) {
    return mPresenter.inflateHeaderView(res);
  }

  /**
   * Adds a View as a header of the navigation menu.
   *
   * @param view The view to be added as a header of the navigation menu.
   */
  public void addHeaderView(@NonNull View view) {
    mPresenter.addHeaderView(view);
  }

  /**
   * Removes a previously-added header view.
   *
   * @param view The view to remove
   */
  public void removeHeaderView(@NonNull View view) {
    mPresenter.removeHeaderView(view);
  }

  /**
   * Gets the number of headers in this NavigationView.
   *
   * @return A positive integer representing the number of headers.
   */
  public int getHeaderCount() {
    return mPresenter.getHeaderCount();
  }

  /**
   * Gets the header view at the specified position.
   *
   * @param index The position at which to get the view from.
   * @return The header view the specified position or null if the position does not exist in this
   * NavigationView.
   */
  public View getHeaderView(int index) {
    return mPresenter.getHeaderView(index);
  }

  /**
   * Returns the tint which is applied to our item's icons.
   *
   * @attr ref R.styleable#NavigationView_itemIconTint
   * @see #setItemIconTintList(ColorStateList)
   */
  @Nullable public ColorStateList getItemIconTintList() {
    return mPresenter.getItemTintList();
  }

  /**
   * Set the tint which is applied to our item's icons.
   *
   * @param tint the tint to apply.
   * @attr ref R.styleable#NavigationView_itemIconTint
   */
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    mPresenter.setItemIconTintList(tint);
  }

  /**
   * Returns the tint which is applied to our item's icons.
   *
   * @attr ref R.styleable#NavigationView_itemTextColor
   * @see #setItemTextColor(ColorStateList)
   */
  @Nullable public ColorStateList getItemTextColor() {
    return mPresenter.getItemTextColor();
  }

  /**
   * Set the text color which is text to our items.
   *
   * @attr ref R.styleable#NavigationView_itemTextColor
   * @see #getItemTextColor()
   */
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    mPresenter.setItemTextColor(textColor);
  }

  /**
   * Returns the background drawable for the menu items.
   *
   * @attr ref R.styleable#NavigationView_itemBackground
   * @see #setItemBackgroundResource(int)
   */
  public Drawable getItemBackground() {
    return mPresenter.getItemBackground();
  }

  /**
   * Set the background of the menu items to the given resource.
   *
   * @param resId The identifier of the resource.
   * @attr ref R.styleable#NavigationView_itemBackground
   */
  public void setItemBackgroundResource(@DrawableRes int resId) {
    setItemBackground(ContextCompat.getDrawable(getContext(), resId));
  }

  /**
   * Set the background of the menu items to a given resource. The resource should refer to
   * a Drawable object or 0 to use the background background.
   *
   * @attr ref R.styleable#NavigationView_itemBackground
   */
  public void setItemBackground(Drawable itemBackground) {
    mPresenter.setItemBackground(itemBackground);
  }

  /**
   * Sets the currently checked item in this navigation menu.
   *
   * @param id The item ID of the currently checked item.
   */
  public void setCheckedItem(@IdRes int id) {
    MenuItem item = mMenu.findItem(id);
    if (item != null) {
      mPresenter.setCheckedItem((MenuItemImpl) item);
    }
  }

  /**
   * Set the text appearance of the menu items to a given resource.
   *
   * @attr ref R.styleable#NavigationView_itemTextAppearance
   */
  public void setItemTextAppearance(@StyleRes int resId) {
    mPresenter.setItemTextAppearance(resId);
  }

  private MenuInflater getMenuInflater() {
    if (mMenuInflater == null) {
      mMenuInflater = new SupportMenuInflater(getContext());
    }
    return mMenuInflater;
  }

  private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = getResources().getColorStateList(value.resourceId);
    if (!getContext().getTheme()
        .resolveAttribute(android.support.design.R.attr.colorPrimary, value, true)) {
      return null;
    }
    int colorPrimary = value.data;
    int defaultColor = baseColor.getDefaultColor();
    return new ColorStateList(new int[][] {
        DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET
    }, new int[] {
        baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor
    });
  }

  /**
   * Listener for handling events on navigation items.
   */
  public interface OnNavigationItemSelectedListener {

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    public boolean onNavigationItemSelected(MenuItem item);
  }

  /**
   * User interface state that is stored by NavigationView for implementing
   * onSaveInstanceState().
   */
  public static class SavedState extends View.BaseSavedState {
    public Bundle menuState;

    public SavedState(Parcel in, ClassLoader loader) {
      super(in);
      menuState = in.readBundle(loader);
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeBundle(menuState);
    }

    public static final Creator<SavedState> CREATOR =
        ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
          @Override public SavedState createFromParcel(Parcel parcel, ClassLoader loader) {
            return new SavedState(parcel, loader);
          }

          @Override public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        });
  }
}
