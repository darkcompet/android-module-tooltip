package tool.compet.tooltip;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

/**
 * Ref: https://github.com/florent37/ViewTooltip
 * Others:
 * 	- https://github.com/ViHtarb/Tooltip
 * 	- https://github.com/jayrambhia/Tooltip
 * 	- https://github.com/tomergoldst/tooltips
 */
public class DkTooltip {
	public enum Position {
		LEFT,
		RIGHT,
		TOP,
		BOTTOM,
	}

	public enum Align {
		START,
		CENTER,
		END
	}

	public interface TooltipAnimation {
		void animateEnter(View view, Animator.AnimatorListener animatorListener);

		void animateExit(View view, Animator.AnimatorListener animatorListener);
	}

	public interface ListenerDisplay {
		void onDisplay(View view);
	}

	public interface ListenerHide {
		void onHide(View view);
	}

	private final View view;
	private final TooltipView tooltipView;
	private View rootView;

	private DkTooltip(MyContext myContext, View view) {
		this.view = view;
		this.tooltipView = new TooltipView(myContext.getContext());

		final NestedScrollView scrollParent = findScrollParent(view);
		if (scrollParent != null) {
			scrollParent.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
				tooltipView.setTranslationY(tooltipView.getTranslationY() - (scrollY - oldScrollY))
			);
		}
	}

	private DkTooltip(MyContext myContext, View rootView, View view) {
		this.rootView = rootView;
		this.view = view;
		this.tooltipView = new TooltipView(myContext.getContext());

		final NestedScrollView scrollParent = findScrollParent(view);
		if (scrollParent != null) {
			scrollParent.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) ->
				tooltipView.setTranslationY(tooltipView.getTranslationY() - (scrollY - oldScrollY))
			);
		}
	}

	private DkTooltip(View view) {
		this(new MyContext(getActivityContext(view.getContext())), view);
	}

	public static DkTooltip on(final View view) {
		return new DkTooltip(new MyContext(getActivityContext(view.getContext())), view);
	}

	public static DkTooltip on(Fragment fragment, final View view) {
		return new DkTooltip(new MyContext(fragment), view);
	}

	public static DkTooltip on(Activity activity, final View view) {
		return new DkTooltip(new MyContext(getActivityContext(activity)), view);
	}

	public static DkTooltip on(Activity activity, final View rootView, final View view) {
		return new DkTooltip(new MyContext(getActivityContext(activity)), rootView, view);
	}

	private static Activity getActivityContext(Context context) {
		while (context instanceof ContextWrapper) {
			if (context instanceof Activity) {
				return (Activity) context;
			}
			context = ((ContextWrapper) context).getBaseContext();
		}
		return null;
	}

	private NestedScrollView findScrollParent(View view) {
		if (view.getParent() == null || !(view.getParent() instanceof View)) {
			return null;
		}
		else if (view.getParent() instanceof NestedScrollView) {
			return ((NestedScrollView) view.getParent());
		}
		else {
			return findScrollParent(((View) view.getParent()));
		}
	}

	public DkTooltip position(Position position) {
		this.tooltipView.setPosition(position);
		return this;
	}

	public DkTooltip withShadow(boolean withShadow) {
		this.tooltipView.setWithShadow(withShadow);
		return this;
	}

	public DkTooltip shadowColor(@ColorInt int shadowColor) {
		this.tooltipView.setShadowColor(shadowColor);
		return this;
	}

	public DkTooltip customView(View customView) {
		this.tooltipView.setCustomView(customView);
		return this;
	}

	public DkTooltip customView(int viewId) {
		this.tooltipView.setCustomView(((Activity) view.getContext()).findViewById(viewId));
		return this;
	}

	public DkTooltip arrowWidth(int arrowWidth) {
		this.tooltipView.setArrowWidth(arrowWidth);
		return this;
	}

	public DkTooltip arrowHeight(int arrowHeight) {
		this.tooltipView.setArrowHeight(arrowHeight);
		return this;
	}

	public DkTooltip arrowSourceMargin(int arrowSourceMargin) {
		this.tooltipView.setArrowSourceMargin(arrowSourceMargin);
		return this;
	}

	public DkTooltip arrowTargetMargin(int arrowTargetMargin) {
		this.tooltipView.setArrowTargetMargin(arrowTargetMargin);
		return this;
	}

	public DkTooltip align(Align align) {
		this.tooltipView.setAlign(align);
		return this;
	}

	public TooltipView show() {
		final Context activityContext = tooltipView.getContext();
		if (activityContext instanceof Activity) {
			final ViewGroup decorView = rootView != null ?
				(ViewGroup) rootView :
				(ViewGroup) ((Activity) activityContext).getWindow().getDecorView();

			view.postDelayed(new Runnable() {
				@Override
				public void run() {
					final Rect rect = new Rect();
					view.getGlobalVisibleRect(rect);

					final Rect rootGlobalRect = new Rect();
					final Point rootGlobalOffset = new Point();
					decorView.getGlobalVisibleRect(rootGlobalRect, rootGlobalOffset);

					int[] location = new int[2];
					view.getLocationOnScreen(location);

					rect.left = location[0];
					rect.top -= rootGlobalOffset.y;
					rect.bottom -= rootGlobalOffset.y;
					rect.left -= rootGlobalOffset.x;
					rect.right -= rootGlobalOffset.x;

					decorView.addView(tooltipView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

					tooltipView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {

							tooltipView.setup(rect, decorView.getWidth());

							tooltipView.getViewTreeObserver().removeOnPreDrawListener(this);

							return false;
						}
					});
				}
			}, 100);
		}
		return tooltipView;
	}

	public void close() {
		tooltipView.close();
	}

	public DkTooltip duration(long duration) {
		this.tooltipView.setDuration(duration);
		return this;
	}

	public DkTooltip color(int color) {
		this.tooltipView.setColor(color);
		return this;
	}

	public DkTooltip color(Paint paint) {
		this.tooltipView.setPaint(paint);
		return this;
	}

	public DkTooltip onDisplay(ListenerDisplay listener) {
		this.tooltipView.setListenerDisplay(listener);
		return this;
	}

	public DkTooltip onHide(ListenerHide listener) {
		this.tooltipView.setListenerHide(listener);
		return this;
	}

	public DkTooltip padding(int left, int top, int right, int bottom) {
		this.tooltipView.paddingTop = top;
		this.tooltipView.paddingBottom = bottom;
		this.tooltipView.paddingLeft = left;
		this.tooltipView.paddingRight = right;
		return this;
	}

	public DkTooltip animation(TooltipAnimation tooltipAnimation) {
		this.tooltipView.setTooltipAnimation(tooltipAnimation);
		return this;
	}

	public DkTooltip text(String text) {
		this.tooltipView.setText(text);
		return this;
	}

	public DkTooltip text(@StringRes int text) {
		this.tooltipView.setText(text);
		return this;
	}

	public DkTooltip corner(int corner) {
		this.tooltipView.setCorner(corner);
		return this;
	}

	public DkTooltip textColor(int textColor) {
		this.tooltipView.setTextColor(textColor);
		return this;
	}

	public DkTooltip textTypeFace(Typeface typeface) {
		this.tooltipView.setTextTypeFace(typeface);
		return this;
	}

	public DkTooltip textSize(int unit, float textSize) {
		this.tooltipView.setTextSize(unit, textSize);
		return this;
	}

	public DkTooltip margin(int left, int top, int right, int bottom) {
		this.tooltipView.setMargin(left, top, right, bottom);
		return this;
	}

	public DkTooltip setTextGravity(int textGravity) {
		this.tooltipView.setTextGravity(textGravity);
		return this;
	}

	public DkTooltip clickToHide(boolean clickToHide) {
		this.tooltipView.setClickToHide(clickToHide);
		return this;
	}

	public DkTooltip autoHide(boolean autoHide) {
		this.tooltipView.setAutoHide(autoHide);
		return this;
	}

	public DkTooltip autoHide(boolean autoHide, long duration) {
		this.tooltipView.setAutoHide(autoHide);
		this.tooltipView.setDuration(duration);
		return this;
	}

	public DkTooltip distanceWithView(int distance) {
		this.tooltipView.setDistanceWithView(distance);
		return this;
	}

	public DkTooltip border(int color, float width) {
		Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setColor(color);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(width);
		this.tooltipView.setBorderPaint(borderPaint);
		return this;
	}
}