package com.arrowsmith.llv1.classes;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arrowsmith.llv1.MainActivity;
import com.arrowsmith.llv1.R;

import java.util.ArrayList;
import java.util.List;

public class MemberListAdapter extends ArrayAdapter<Player> {

    private Context context;
    private List<Player> list;
    private Boolean playerAdded;
    private MainActivity main;

    // OTHER VARS
    boolean showHost;

    public MemberListAdapter(@NonNull Context context, int textResourceId, ArrayList<Player> list, Boolean playerAdded) {
        super(context, 0, list);
        this.context = context;
        this.list = list;
        this.playerAdded = playerAdded;
        this.main = (MainActivity) context;

        this.showHost = true;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.wr_member_list_item,null);

        TextView textPlayerName = (TextView) v.findViewById(R.id.text_player_name_here);
        TextView textIsHost = (TextView) v.findViewById(R.id.text_host_or_not);
        TextView textPoints = (TextView) v.findViewById(R.id.text_points);

        if(list.get(position).getName().equals(main.me.getName())) {
            SpannableString nameSpan = new SpannableString(list.get(position).getName());
            nameSpan.setSpan(new UnderlineSpan(), 0, nameSpan.length(), 0);
            textPlayerName.setText(nameSpan);
        }
        else{
            textPlayerName.setText(list.get(position).getName());
        }

        if(list.get(position).getHosting() && showHost) textIsHost.setText(" "+"HOST"+" ");
        else textIsHost.setVisibility(View.GONE);

        textPoints.setText(Integer.toString(list.get(position).getPoints()));

        if(playerAdded != null)
        {
            if(position == (list.size()-1) && playerAdded)
            {
                Animation popIn = AnimationUtils.loadAnimation(main, R.anim.pop_in_normal);
                v.startAnimation(popIn);
            }
        }


        return v;
    }


    public void setShowHost(boolean showHost) {
        this.showHost = showHost;
    }


}