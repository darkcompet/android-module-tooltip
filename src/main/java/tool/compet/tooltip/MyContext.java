package tool.compet.tooltip;

import android.app.Activity;
import android.content.Context;
import android.view.Window;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

class MyContext {
	private Fragment fragment;
	private Context context;
	private Activity activity;

	MyContext(Activity activity) {
		this.activity = activity;
	}

	MyContext(Fragment fragment) {
		this.fragment = fragment;
	}

	MyContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		if (activity != null) {
			return activity;
		}
		else {
			return fragment.getActivity();
		}
	}

	public Activity getActivity() {
		if (activity != null) {
			return activity;
		}
		else {
			return fragment.getActivity();
		}
	}

	public Window getWindow() {
		if (activity != null) {
			return activity.getWindow();
		}
		else {
			if (fragment instanceof DialogFragment) {
				return ((DialogFragment) fragment).getDialog().getWindow();
			}
			return fragment.getActivity().getWindow();
		}
	}
}