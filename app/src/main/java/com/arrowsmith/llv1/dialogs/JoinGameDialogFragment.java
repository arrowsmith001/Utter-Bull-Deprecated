package com.arrowsmith.llv1.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.arrowsmith.llv1.MainActivity;
import com.arrowsmith.llv1.R;

public class JoinGameDialogFragment extends DialogFragment {

    MainActivity main;
    JoinGameDialogFragment.JoinGameListener activityCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        main = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.ma_dialog_fragment_join_game,container,false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        EditText editName = (EditText) view.findViewById(R.id.edit_name);
        EditText editCode = (EditText) view.findViewById(R.id.edit_code);
        Button btnJoinRoom = (Button) view.findViewById(R.id.button_join_room);
        Button btnCancel = (Button) view.findViewById(R.id.button_cancel);
        TextView nameEntry = (TextView) view.findViewById(R.id.text_name_entry);
        TextView invalidText = (TextView) view.findViewById(R.id.text_validation);
        TextView invalidText2 = (TextView) view.findViewById(R.id.text_validation2);

        // TODO: Trim name validation

        // DEBUG FLAG - SET CODE
        editCode.setText("ABC51");

        editName.setText(main.prefs.getString("playerName",""));
        invalidText.setVisibility(View.GONE);
        invalidText2.setVisibility(View.GONE);

        //Log.i(TAG, "onCreateView: DEBUG FLAG: EditText set to AAA00");
        // editCode.setText("AAA00"); // For debugging purposes
        /*String playerNamePref = MainActivity.playerNamePref;
        if (!playerNamePref.equals("")) {

        }*/

        try {
            activityCallback = (JoinGameListener) main;
        } catch (ClassCastException e) {
            throw new ClassCastException(main.toString()
                    + " must implement JoinGameDialogFragment");
        }

        btnJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = editName.getText().toString().trim();
                String code = editCode.getText().toString().trim();
                //Boolean isCodeValid = MainActivity.validateCode(code);
                boolean isCodeValid = true;

                if(!name.equals("") && isCodeValid==true) {
                    activityCallback.onJoinDetailsEntered(name,code);
                    dismiss();
                }else{
                    if(name.equals("")) invalidText.setVisibility(View.VISIBLE);
                    else invalidText.setVisibility(View.GONE);
                    if(!isCodeValid) invalidText2.setVisibility(View.VISIBLE);
                    else invalidText2.setVisibility(View.GONE);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        editName.setCursorVisible(false);
        editCode.setCursorVisible(false);

        return view;

    }

    public interface JoinGameListener{
        void onJoinDetailsEntered(String name, String gameCode);
    }

}
