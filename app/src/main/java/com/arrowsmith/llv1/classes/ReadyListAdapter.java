package com.arrowsmith.llv1.classes;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arrowsmith.llv1.R;
import com.arrowsmith.llv1.classes.LoadingDots;
import com.arrowsmith.llv1.classes.Player;

import java.util.List;

public class ReadyListAdapter extends ArrayAdapter<Player> {

    private String textWhenReady;
    private Context context;
    private List<Player> list;

    public ReadyListAdapter(@NonNull Context context, int resource, @NonNull List<Player> list, String textWhenReady) {
        super(context, resource, list);
        this.context = context;
        this.list = list;
        this.textWhenReady = textWhenReady;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.ready_list_member,null);

        TextView name = v.findViewById(R.id.textNameReadyList);
        LoadingDots dots = v.findViewById(R.id.loadingDotsReadyList);
        TextView ready = v.findViewById(R.id.text_ready_list_ready);

        // Set name
        name.setText(list.get(position).getName());
        ready.setText(textWhenReady + " ");

        boolean isReady = list.get(position).isReady();

        if(isReady)
        {
            ready.setVisibility(View.VISIBLE);
            dots.setVisibility(View.GONE);
        }
        else
        {
            v.getBackground().setColorFilter(context.getResources().getColor(R.color.light_grey), PorterDuff.Mode.MULTIPLY);
            ready.setVisibility(View.GONE);
            dots.setVisibility(View.VISIBLE);
        }

        return v;
    }
}
