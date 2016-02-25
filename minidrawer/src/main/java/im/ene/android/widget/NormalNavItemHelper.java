package im.ene.android.widget;

import android.content.res.ColorStateList;
import android.support.annotation.FloatRange;
import android.support.design.internal.NavigationMenuItemView;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.TextView;

/**
 * Created by eneim on 2/23/16.
 */
public class NormalNavItemHelper extends NavItemViewHelper {

  private static final int TEXT_VIEW_ID = R.id.design_menu_item_text;
  private static final int ACTION_VIEW_ID = R.id.design_menu_item_action_area_stub;

  // Expects a NavigationMenuPresenter$NormalViewHolder
  private final TextView textView;
  private final ColorStateList mTextColor;
  private final View actionArea;

  public NormalNavItemHelper(View itemView) {
    super(itemView);
    if (!(itemView instanceof NavigationMenuItemView)) {
      throw new IllegalArgumentException("Wrong viewHolder type");
    }

    this.textView = (TextView) itemView.findViewById(TEXT_VIEW_ID);
    this.actionArea = itemView.findViewById(ACTION_VIEW_ID);
    mTextColor = textView.getTextColors();
  }

  @Override protected void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset) {
    float newValue = AnimationUtils.ACCELERATE_INTERPOLATOR.getInterpolation(offset);

    if (this.textView != null) {
      this.textView.setTextColor(mTextColor.withAlpha((int) (newValue * 255)));
    }

    if (actionArea != null) {
      ViewCompat.setAlpha(actionArea, newValue);
    }
  }
}
