package tool.compet.tooltip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;

public class TooltipView extends FrameLayout {
	private static final int MARGIN_SCREEN_BORDER_TOOLTIP = 30;
	protected View childView;
	int shadowPadding = 4;
	int shadowWidth = 8;
	private int arrowHeight = 15;
	private int arrowWidth = 15;
	private int arrowSourceMargin = 0;
	private int arrowTargetMargin = 0;
	private int color = Color.parseColor("#1F7C82");
	private Path bubblePath;
	private Paint bubblePaint;
	private Paint borderPaint;
	private DkTooltip.Position position = DkTooltip.Position.BOTTOM;
	private DkTooltip.Align align = DkTooltip.Align.CENTER;
	private boolean clickToHide;
	private boolean autoHide = true;
	private long duration = 4000; // in millis
	private DkTooltip.ListenerDisplay listenerDisplay;
	private DkTooltip.ListenerHide listenerHide;
	private DkTooltip.TooltipAnimation tooltipAnimation = new FadeTooltipAnimation();
	private int corner = 30;
	int paddingTop = 20;
	int paddingBottom = 30;
	int paddingRight = 30;
	int paddingLeft = 30;
	private int marginTop = 0;
	private int marginBottom = 0;
	private int marginRight = 0;
	private int marginLeft = 0;
	private Rect viewRect;
	private int distanceWithView = 0;
	private int shadowColor = Color.parseColor("#aaaaaa");

	public TooltipView(Context context) {
		super(context);
		setWillNotDraw(false);

		this.childView = new TextView(context);
		((TextView) childView).setTextColor(Color.WHITE);
		addView(childView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		childView.setPadding(0, 0, 0, 0);

		bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bubblePaint.setColor(color);
		bubblePaint.setStyle(Paint.Style.FILL);

		borderPaint = null;

		setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint);

		setWithShadow(true);

	}

	public void setCustomView(View customView) {
		this.removeView(childView);
		this.childView = customView;
		addView(childView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	public void setColor(int color) {
		this.color = color;
		bubblePaint.setColor(color);
		postInvalidate();
	}

	public void setShadowColor(int color) {
		this.shadowColor = color;
		postInvalidate();
	}

	public void setMargin(int left, int top, int right, int bottom) {
		this.marginLeft = left;
		this.marginTop = top;
		this.marginRight = right;
		this.marginBottom = top;

		childView.setPadding(childView.getPaddingLeft() + left, childView.getPaddingTop() + top, childView.getPaddingRight() + right, childView.getPaddingBottom() + bottom);
		postInvalidate();
	}

	public void setPaint(Paint paint) {
		bubblePaint = paint;
		setLayerType(LAYER_TYPE_SOFTWARE, paint);
		postInvalidate();
	}

	public void setPosition(DkTooltip.Position position) {
		this.position = position;
		switch (position) {
			case TOP:
				setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + arrowHeight);
				break;
			case BOTTOM:
				setPadding(paddingLeft, paddingTop + arrowHeight, paddingRight, paddingBottom);
				break;
			case LEFT:
				setPadding(paddingLeft, paddingTop, paddingRight + arrowHeight, paddingBottom);
				break;
			case RIGHT:
				setPadding(paddingLeft + arrowHeight, paddingTop, paddingRight, paddingBottom);
				break;
		}
		postInvalidate();
	}

	public void setAlign(DkTooltip.Align align) {
		this.align = align;
		postInvalidate();
	}

	public void setText(String text) {
		if (childView instanceof TextView) {
			((TextView) this.childView).setText(Html.fromHtml(text));
		}
		postInvalidate();
	}

	public void setText(int text) {
		if (childView instanceof TextView) {
			((TextView) this.childView).setText(text);
		}
		postInvalidate();
	}

	public void setTextColor(int textColor) {
		if (childView instanceof TextView) {
			((TextView) this.childView).setTextColor(textColor);
		}
		postInvalidate();
	}

	public int getArrowHeight() {
		return arrowHeight;
	}

	public void setArrowHeight(int arrowHeight) {
		this.arrowHeight = arrowHeight;
		postInvalidate();
	}

	public int getArrowWidth() {
		return arrowWidth;
	}

	public void setArrowWidth(int arrowWidth) {
		this.arrowWidth = arrowWidth;
		postInvalidate();
	}

	public int getArrowSourceMargin() {
		return arrowSourceMargin;
	}

	public void setArrowSourceMargin(int arrowSourceMargin) {
		this.arrowSourceMargin = arrowSourceMargin;
		postInvalidate();
	}

	public int getArrowTargetMargin() {
		return arrowTargetMargin;
	}

	public void setArrowTargetMargin(int arrowTargetMargin) {
		this.arrowTargetMargin = arrowTargetMargin;
		postInvalidate();
	}

	public void setTextTypeFace(Typeface textTypeFace) {
		if (childView instanceof TextView) {
			((TextView) this.childView).setTypeface(textTypeFace);
		}
		postInvalidate();
	}

	public void setTextSize(int unit, float size) {
		if (childView instanceof TextView) {
			((TextView) this.childView).setTextSize(unit, size);
		}
		postInvalidate();
	}

	public void setTextGravity(int textGravity) {
		if (childView instanceof TextView) {
			((TextView) this.childView).setGravity(textGravity);
		}
		postInvalidate();
	}

	public void setClickToHide(boolean clickToHide) {
		this.clickToHide = clickToHide;
	}

	public void setCorner(int corner) {
		this.corner = corner;
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		super.onSizeChanged(width, height, oldw, oldh);

		bubblePath = drawBubble(new RectF(shadowPadding, shadowPadding, width - shadowPadding * 2, height - shadowPadding * 2), corner, corner, corner, corner);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (bubblePath != null) {
			canvas.drawPath(bubblePath, bubblePaint);
			if (borderPaint != null) {
				canvas.drawPath(bubblePath, borderPaint);
			}
		}
	}

	public void setListenerDisplay(DkTooltip.ListenerDisplay listener) {
		this.listenerDisplay = listener;
	}

	public void setListenerHide(DkTooltip.ListenerHide listener) {
		this.listenerHide = listener;
	}

	public void setTooltipAnimation(DkTooltip.TooltipAnimation tooltipAnimation) {
		this.tooltipAnimation = tooltipAnimation;
	}

	protected void startEnterAnimation() {
		tooltipAnimation.animateEnter(this, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if (listenerDisplay != null) {
					listenerDisplay.onDisplay(TooltipView.this);
				}
			}
		});
	}

	protected void startExitAnimation(final Animator.AnimatorListener animatorListener) {
		tooltipAnimation.animateExit(this, new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				animatorListener.onAnimationEnd(animation);
				if (listenerHide != null) {
					listenerHide.onHide(TooltipView.this);
				}
			}
		});
	}

	protected void handleAutoRemove() {
		if (clickToHide) {
			setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (clickToHide) {
						remove();
					}
				}
			});
		}

		if (autoHide) {
			postDelayed(() -> remove(), duration);
		}
	}

	public void remove() {
		startExitAnimation(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				removeNow();
			}
		});
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setAutoHide(boolean autoHide) {
		this.autoHide = autoHide;
	}

	public void setupPosition(Rect rect) {

		int x, y;

		if (position == DkTooltip.Position.LEFT || position == DkTooltip.Position.RIGHT) {
			if (position == DkTooltip.Position.LEFT) {
				x = rect.left - getWidth() - distanceWithView;
			}
			else {
				x = rect.right + distanceWithView;
			}
			y = rect.top + getAlignOffset(getHeight(), rect.height());
		}
		else {
			if (position == DkTooltip.Position.BOTTOM) {
				y = rect.bottom + distanceWithView;
			}
			else { // top
				y = rect.top - getHeight() - distanceWithView;
			}
			x = rect.left + getAlignOffset(getWidth(), rect.width());
		}

		setTranslationX(x);
		setTranslationY(y);
	}

	private int getAlignOffset(int myLength, int hisLength) {
		switch (align) {
			case END:
				return hisLength - myLength;
			case CENTER:
				return (hisLength - myLength) / 2;
		}
		return 0;
	}

	private Path drawBubble(RectF myRect, float topLeftDiameter, float topRightDiameter, float bottomRightDiameter, float bottomLeftDiameter) {
		final Path path = new Path();

		if (viewRect == null)
			return path;

		topLeftDiameter = topLeftDiameter < 0 ? 0 : topLeftDiameter;
		topRightDiameter = topRightDiameter < 0 ? 0 : topRightDiameter;
		bottomLeftDiameter = bottomLeftDiameter < 0 ? 0 : bottomLeftDiameter;
		bottomRightDiameter = bottomRightDiameter < 0 ? 0 : bottomRightDiameter;

		final float spacingLeft = this.position == DkTooltip.Position.RIGHT ? arrowHeight : marginLeft;
		final float spacingTop = this.position == DkTooltip.Position.BOTTOM ? arrowHeight : marginTop;
		final float spacingRight = this.position == DkTooltip.Position.LEFT ? arrowHeight : marginRight;
		final float spacingBottom = this.position == DkTooltip.Position.TOP ? arrowHeight : marginBottom;

		final float left = spacingLeft + myRect.left;
		final float top = spacingTop + myRect.top;
		final float right = myRect.right - spacingRight;
		final float bottom = myRect.bottom - spacingBottom;
		final float centerX = viewRect.centerX() - getX();

		final float arrowSourceX = (Arrays.asList(DkTooltip.Position.TOP, DkTooltip.Position.BOTTOM).contains(this.position))
			? centerX + arrowSourceMargin
			: centerX;
		final float arrowTargetX = (Arrays.asList(DkTooltip.Position.TOP, DkTooltip.Position.BOTTOM).contains(this.position))
			? centerX + arrowTargetMargin
			: centerX;
		final float arrowSourceY = (Arrays.asList(DkTooltip.Position.RIGHT, DkTooltip.Position.LEFT).contains(this.position))
			? bottom / 2f - arrowSourceMargin
			: bottom / 2f;
		final float arrowTargetY = (Arrays.asList(DkTooltip.Position.RIGHT, DkTooltip.Position.LEFT).contains(this.position))
			? bottom / 2f - arrowTargetMargin
			: bottom / 2f;

		path.moveTo(left + topLeftDiameter / 2f, top);
		//LEFT, TOP

		if (position == DkTooltip.Position.BOTTOM) {
			path.lineTo(arrowSourceX - arrowWidth, top);
			path.lineTo(arrowTargetX, myRect.top);
			path.lineTo(arrowSourceX + arrowWidth, top);
		}
		path.lineTo(right - topRightDiameter / 2f, top);

		path.quadTo(right, top, right, top + topRightDiameter / 2);
		//RIGHT, TOP

		if (position == DkTooltip.Position.LEFT) {
			path.lineTo(right, arrowSourceY - arrowWidth);
			path.lineTo(myRect.right, arrowTargetY);
			path.lineTo(right, arrowSourceY + arrowWidth);
		}
		path.lineTo(right, bottom - bottomRightDiameter / 2);

		path.quadTo(right, bottom, right - bottomRightDiameter / 2, bottom);
		//RIGHT, BOTTOM

		if (position == DkTooltip.Position.TOP) {
			path.lineTo(arrowSourceX + arrowWidth, bottom);
			path.lineTo(arrowTargetX, myRect.bottom);
			path.lineTo(arrowSourceX - arrowWidth, bottom);
		}
		path.lineTo(left + bottomLeftDiameter / 2, bottom);

		path.quadTo(left, bottom, left, bottom - bottomLeftDiameter / 2);
		//LEFT, BOTTOM

		if (position == DkTooltip.Position.RIGHT) {
			path.lineTo(left, arrowSourceY + arrowWidth);
			path.lineTo(myRect.left, arrowTargetY);
			path.lineTo(left, arrowSourceY - arrowWidth);
		}
		path.lineTo(left, top + topLeftDiameter / 2);

		path.quadTo(left, top, left + topLeftDiameter / 2, top);

		path.close();

		return path;
	}

	public boolean adjustSize(Rect rect, int screenWidth) {

		final Rect r = new Rect();
		getGlobalVisibleRect(r);

		boolean changed = false;
		final ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if (position == DkTooltip.Position.LEFT && getWidth() > rect.left) {
			layoutParams.width = rect.left - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView;
			changed = true;
		}
		else if (position == DkTooltip.Position.RIGHT && rect.right + getWidth() > screenWidth) {
			layoutParams.width = screenWidth - rect.right - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView;
			changed = true;
		}
		else if (position == DkTooltip.Position.TOP || position == DkTooltip.Position.BOTTOM) {
			int adjustedLeft = rect.left;
			int adjustedRight = rect.right;

			if ((rect.centerX() + getWidth() / 2f) > screenWidth) {
				float diff = (rect.centerX() + getWidth() / 2f) - screenWidth;

				adjustedLeft -= diff;
				adjustedRight -= diff;

				setAlign(DkTooltip.Align.CENTER);
				changed = true;
			}
			else if ((rect.centerX() - getWidth() / 2f) < 0) {
				float diff = -(rect.centerX() - getWidth() / 2f);

				adjustedLeft += diff;
				adjustedRight += diff;

				setAlign(DkTooltip.Align.CENTER);
				changed = true;
			}

			if (adjustedLeft < 0) {
				adjustedLeft = 0;
			}

			if (adjustedRight > screenWidth) {
				adjustedRight = screenWidth;
			}

			rect.left = adjustedLeft;
			rect.right = adjustedRight;
		}

		setLayoutParams(layoutParams);
		postInvalidate();
		return changed;
	}

	private void onSetup(Rect myRect) {
		setupPosition(myRect);

		bubblePath = drawBubble(new RectF(shadowPadding, shadowPadding, getWidth() - shadowPadding * 2f, getHeight() - shadowPadding * 2f), corner, corner, corner, corner);
		startEnterAnimation();

		handleAutoRemove();
	}

	public void setup(final Rect viewRect, int screenWidth) {
		this.viewRect = new Rect(viewRect);
		final Rect myRect = new Rect(viewRect);

		final boolean changed = adjustSize(myRect, screenWidth);
		if (!changed) {
			onSetup(myRect);
		}
		else {
			getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					onSetup(myRect);
					getViewTreeObserver().removeOnPreDrawListener(this);
					return false;
				}
			});
		}
	}

	public void close() {
		remove();
	}

	public void removeNow() {
		if (getParent() != null) {
			final ViewGroup parent = ((ViewGroup) getParent());
			parent.removeView(TooltipView.this);
		}
	}

	public void closeNow() {
		removeNow();
	}

	public void setWithShadow(boolean withShadow) {
		if (withShadow) {
			bubblePaint.setShadowLayer(shadowWidth, 0, 0, shadowColor);
		}
		else {
			bubblePaint.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
		}
	}

	public void setDistanceWithView(int distanceWithView) {
		this.distanceWithView = distanceWithView;
	}

	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
		postInvalidate();
	}
}