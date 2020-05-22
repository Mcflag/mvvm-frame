package cn.lcsw.diningpos.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class BaseDialogFragment extends DialogFragment {

    private static final String TAG = "BaseDialogFragment";

//    protected ToolScanner scanner;
//    protected OnScanSuccessListener scanListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printLifeCycle("onCreate(@Nullable Bundle savedInstanceState)", savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        printLifeCycle("onSaveInstanceState(Bundle outState)", outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        printLifeCycle("onActivityCreated(Bundle savedInstanceState)", savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        printLifeCycle("onAttach(Context context)");
    }

    @Override
    public void onStart() {
        super.onStart();
        printLifeCycle("onStart()");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        printLifeCycle("onViewCreated(View view, @Nullable Bundle savedInstanceState)", savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        printLifeCycle("onHiddenChanged(boolean hidden)", hidden);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        printLifeCycle("onCreateDialog(Bundle savedInstanceState)", savedInstanceState);
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        printLifeCycle("onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)", savedInstanceState);

//        scanner = new ToolScanner();
//        scanner.setOnScanSuccessListener(scanListener);
//        this.getDialog().setOnKeyListener((dialog, keyCode, event) -> {
//            scanner.analysisKeyEvent(event);
//            return false;
//        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        printLifeCycle("onActivityResult(int requestCode, int resultCode, Intent data)", requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        printLifeCycle("onDestroy()");
//        scanner.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        printLifeCycle("onDetach()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        printLifeCycle("onDestroyView()");
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        printLifeCycle("onAttachFragment(Fragment childFragment)");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        printLifeCycle("onDismiss(DialogInterface dialog)");
    }

    @Override
    public void onPause() {
        super.onPause();
        printLifeCycle("onPause()");
    }

    @Override
    public void onResume() {
        super.onResume();
        printLifeCycle("onResume()");
    }

    @Override
    public void onStop() {
        super.onStop();
        printLifeCycle("onStop()");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        printLifeCycle("onCancel(DialogInterface dialog)");
    }

    private void printLifeCycle(String lifeCycle, Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName() + "-->");
        stringBuilder.append(lifeCycle);
    }

    public void showAllowingStateLoss(FragmentManager fragmentManager, String tag) {
        if (isAdded() || fragmentManager.getFragments().contains(this)) {
            return;
        }
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            fragment.dismissAllowingStateLoss();
        }
        fragmentManager.beginTransaction().add(this, tag).commitAllowingStateLoss();
    }
}
