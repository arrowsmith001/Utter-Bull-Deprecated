package com.arrowsmith.llv1.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.arrowsmith.llv1.MainActivity;
import com.arrowsmith.llv1.R;


public class QuitGameDialogFragment extends DialogFragment {

    MainActivity main;
    QuitListener activityCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Log.i(TAG, "onCreateView: QuitGameDialogFragment");
        View view = inflater.inflate(R.layout.gr_dialog_fragment_quit,container,false);

        Button btnOk = view.findViewById(R.id.button_confirm);
        Button btnCancel = view.findViewById(R.id.button_cancel);

        main = (MainActivity) getActivity();
        try {
            activityCallback = (QuitListener) main;
        } catch (ClassCastException e) {
            throw new ClassCastException(main.toString()
                    + " must implement QuitListener");
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activityCallback.quit();
                dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        return view;

    }


    public interface QuitListener {
        public void quit();
    }
}
