package com.arrowsmith.llv1.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.arrowsmith.llv1.MainActivity;
import com.arrowsmith.llv1.R;



public class QuitFromGameRoundFragment extends DialogFragment{

    // TODO: Auto dismiss upon either being kicked, or upon Results -> Loading movement

    MainActivity main;
    QuitFromGameListener activityCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Log.i(TAG, "onCreateView: QuitGameDialogFragment");
        View view = inflater.inflate(R.layout.gr_dialog_fragment_quit_from_game_round,container,false);

        Button btnOk = view.findViewById(R.id.button_confirm2);
        Button btnCancel = view.findViewById(R.id.button_cancel2);
        TextView textAreYouSureText = view.findViewById(R.id.text_are_you_sure2);

        textAreYouSureText.setText(Html.fromHtml(getString(R.string
                .are_you_sure_you_want_to_quit_you_and_all_other_players_will_be_sent_back_to_the_lobby)));

        main = (MainActivity) getActivity();
        try {
            activityCallback = (QuitFromGameListener) main;
        } catch (ClassCastException e) {
            throw new ClassCastException(main.toString()
                    + " must implement QuitFromGameListener");
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activityCallback.quitFromGame();
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


    public interface QuitFromGameListener {
        public void quitFromGame();
    }
}