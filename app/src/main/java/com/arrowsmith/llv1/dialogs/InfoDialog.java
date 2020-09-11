package com.arrowsmith.llv1.dialogs;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.TextView;

import com.arrowsmith.llv1.MainActivity;
import com.arrowsmith.llv1.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoDialog extends DialogFragment {

    private View view;
    private MainActivity main;

    public InfoDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        main = (MainActivity) getActivity();
        main.getWindow().requestFeature(Window.FEATURE_NO_TITLE);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.fragment_info_dialog, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        /*

        TextView text = view.findViewById(R.id.text_info_dialog);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) text.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        text.setLayoutParams(params);*/
    }

    protected void forceWrapContent(View v) {
        // Start with the provided view
        View current = v;

        // Travel up the tree until fail, modifying the LayoutParams
        do {
            // Get the parent
            ViewParent parent = current.getParent();

            // Check if the parent exists
            if (parent != null) {
                // Get the view
                try {
                    current = (View) parent;
                } catch (ClassCastException e) {
                    // This will happen when at the top view, it cannot be cast to a View
                    break;
                }

                // Modify the layout
                current.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        } while (current.getParent() != null);

        // Request a layout to be re-done
        current.requestLayout();
    }

}
