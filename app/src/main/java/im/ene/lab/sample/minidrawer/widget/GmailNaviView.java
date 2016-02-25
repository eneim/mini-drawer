package im.ene.lab.sample.minidrawer.widget;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import im.ene.android.widget.AnimationUtils;
import im.ene.android.widget.HeaderNavViewHelper;
import im.ene.android.widget.NavItemViewHelper;
import im.ene.android.widget.NavigationView;
import im.ene.lab.sample.minidrawer.R;

/**
 * Created by eneim on 2/24/16.
 */
public class GmailNaviView extends NavigationView {
  public GmailNaviView(Context context) {
    super(context);
  }

  public GmailNaviView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public GmailNaviView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected NavItemViewHelper getItemViewHelper(int viewType, View holder) {
    if (viewType == VIEW_TYPE_HEADER) {
      return new HeaderViewHelper(holder);
    }

    return super.getItemViewHelper(viewType, holder);
  }

  static class HeaderViewHelper extends HeaderNavViewHelper {

    @Nullable final ImageView mIcon;
    @Nullable final View mTitleContainer;

    private final int itemHeight;
    private final int titleContainerHeight;
    private final int iconMaxSize;
    private final int iconMinSize;

    public HeaderViewHelper(View itemView) {
      super(itemView);
      mIcon = (ImageView) itemView.findViewById(R.id.icon);
      mTitleContainer = itemView.findViewById(R.id.title_container);

      itemHeight = itemView.getHeight();
      titleContainerHeight = mTitleContainer.getHeight();

      ViewCompat.setPivotX(mIcon, 0);
      ViewCompat.setPivotY(mIcon, mIcon.getHeight());
      iconMaxSize = mIcon.getWidth();
      iconMinSize = itemView.getResources().getDimensionPixelSize(R.dimen.header_icon_size_min);
    }

    @Override protected void onDrawerOffset(@FloatRange(from = 0.f, to = 1.f) float offset) {
      super.onDrawerOffset(offset);
      float newValue = AnimationUtils.ACCELERATE_INTERPOLATOR.getInterpolation(offset);
      // offset = 0 --> width = minSize;
      // offset = 1 --> width = maxSize;
      float scale = (iconMaxSize * newValue + iconMinSize * (1.f - newValue)) / iconMaxSize;
      if (mIcon != null) {
        ViewCompat.setScaleX(mIcon, scale);
        ViewCompat.setScaleY(mIcon, scale);
      }

      if (mTitleContainer != null) {
        ViewCompat.setAlpha(mTitleContainer, newValue);
      }

      ViewGroup.LayoutParams params = itemView.getLayoutParams();
      params.height = itemHeight - (int) (titleContainerHeight * (1.f - newValue));
      itemView.setLayoutParams(params);
    }
  }
}
