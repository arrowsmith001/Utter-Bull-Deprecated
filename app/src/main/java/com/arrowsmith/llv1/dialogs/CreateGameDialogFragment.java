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


public class CreateGameDialogFragment extends DialogFragment {

    MainActivity main;
    CreateGameListener activityCallback;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        main = (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.ma_dialog_fragment_create_game,container,false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        EditText editName = (EditText) view.findViewById(R.id.edit_name);
        Button btnCreateRoom = (Button) view.findViewById(R.id.button_join_room);
        Button btnCancel = (Button) view.findViewById(R.id.button_cancel);
        TextView nameEntry = (TextView) view.findViewById(R.id.text_name_entry);
        TextView invalidText = (TextView) view.findViewById(R.id.text_validation);

        editName.setText(main.prefs.getString("playerName",""));
        invalidText.setVisibility(View.GONE);

        try {
            activityCallback = (CreateGameListener) main;
        } catch (ClassCastException e) {
            throw new ClassCastException(main.toString()
                    + " must implement CreateGameListener");
        }

        btnCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editName.getText().toString().equals("")) {
                    activityCallback.onHostNameEntered(editName.getText().toString().trim());
                    dismiss();
                }else{
                    invalidText.setVisibility(View.VISIBLE);
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

        return view;

    }


    public interface CreateGameListener{
        void onHostNameEntered(String name);
    }

}
