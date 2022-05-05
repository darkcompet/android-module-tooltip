package tool.compet.tooltip;

import android.animation.Animator;
import android.view.View;

class FadeTooltipAnimation implements DkTooltip.TooltipAnimation {
	private long fadeDuration = 400;

	public FadeTooltipAnimation() {
	}

	public FadeTooltipAnimation(long fadeDuration) {
		this.fadeDuration = fadeDuration;
	}

	@Override
	public void animateEnter(View view, Animator.AnimatorListener animatorListener) {
		view.setAlpha(0);
		view.animate().alpha(1).setDuration(fadeDuration).setListener(animatorListener);
	}

	@Override
	public void animateExit(View view, Animator.AnimatorListener animatorListener) {
		view.animate().alpha(0).setDuration(fadeDuration).setListener(animatorListener);
	}
}