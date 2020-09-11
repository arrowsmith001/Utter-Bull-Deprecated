package com.arrowsmith.llv1.classes;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.arrowsmith.llv1.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private final ExpandableListView expList;
    private final ResultsData resultsData;
    private Context context;
    private List<Player> expandableListTitle;
    private HashMap<String, List<Round>> expandableListDetail;

    public CustomExpandableListAdapter(Context context, ResultsData resultsData,
                                       ExpandableListView expList) {
        this.context = context;
        this.resultsData = resultsData;
        this.expList = expList;

        this.initialised = false;
    }

    private boolean initialised;

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.gp8_results_list_header, null);

        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.text_player_name_here);
        TextView score = (TextView) convertView.findViewById(R.id.text_points);
        TextView scorePrev = (TextView) convertView.findViewById(R.id.text_points_PREV);
        TextView scoreNew = (TextView) convertView.findViewById(R.id.text_points_NEW);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.UK);

        String name = resultsData.getPlayersOrdered().get(listPosition);

        listTitleTextView.setText(name);
        score.setText(numberFormat.format(resultsData.getNewScores().get(name)));
        scorePrev.setText("(" + resultsData.getOldScores().get(name) + ")");

        int newPoints = resultsData.getScoreDiff().get(name);

        scoreNew.setText((newPoints >= 0 ? "+" : "-") + numberFormat.format(Math.abs(newPoints)));

        if(!initialised)
        {
            Animation popIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom);
            popIn.setStartOffset(listPosition*500);
            popIn.setInterpolator(new DecelerateInterpolator(2));
            popIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    initialised = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            convertView.startAnimation(popIn);
        }

        return convertView;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {


            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.gp8_name_scorecards,null);


        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.textScorecardRoundNumber);
        TextView expandedListTextView2 = (TextView) convertView.findViewById(R.id.textScorecardScoresheet);
        TextView name = (TextView) convertView.findViewById(R.id.textRoundPlayerName);
        ImageView img = (ImageView) convertView.findViewById(R.id.img_icon);

        TurnData turnData = resultsData.getTurnData().get(expandedListPosition);

        expandedListTextView.setText("TURN "+Integer.toString(expandedListPosition + 1));
        name.setText("(" + turnData.getName() + ")");

        String thisPlayerName = resultsData.getPlayersOrdered().get(listPosition);
        String content = "";

        List<Achievement> achievements = turnData.getAchievementsUnlocked().get(thisPlayerName);

        for (Achievement ach : achievements)
        {
            content += ach.toSimpleString() + "\n";
        }

        // Default drawable
        img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_hyphen));

        // Correct player
        if(turnData.getCorrectVoterNames().contains(thisPlayerName))
        {
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_tick));
            DrawableCompat.setTint(
                    DrawableCompat.wrap(img.getDrawable()),
                    ContextCompat.getColor(context, R.color.light_green)
            );
        }
        if(turnData.getIncorrectVoterNames().contains(thisPlayerName))
        {
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_cross));
            DrawableCompat.setTint(
                    DrawableCompat.wrap(img.getDrawable()),
                    ContextCompat.getColor(context, R.color.colorPrimary)
            );
        }

        Log.i(TAG, "getChildView: SABOTEUR INFO: saboname: "+turnData.getSaboName()+", thisplayer: "+thisPlayerName);
        // Saboteur case
        if(turnData.getSaboName() != null && turnData.getSaboName().equals(thisPlayerName))
        {
            img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_sabo));
            DrawableCompat.setTint(
                    DrawableCompat.wrap(img.getDrawable()),
                    ContextCompat.getColor(context, R.color.black)
            );

            if(turnData.getS_badSabo())
            {
                DrawableCompat.setTint(
                        DrawableCompat.wrap(img.getDrawable()),
                        ContextCompat.getColor(context, R.color.red)
                );
            }
            else if(turnData.getS_mostEarned())
            {
                DrawableCompat.setTint(
                        DrawableCompat.wrap(img.getDrawable()),
                        ContextCompat.getColor(context, R.color.silver)
                );
            }
            else if(turnData.getS_allEarned())
            {
                DrawableCompat.setTint(
                        DrawableCompat.wrap(img.getDrawable()),
                        ContextCompat.getColor(context, R.color.gold)
                );
            }
        }

        // Player case
        if(turnData.getName().equals(thisPlayerName))
        {
            if (turnData.isP_nobodyVoted()) img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_speaker_empty));
            else
            if (turnData.isP_fiftyFiftyEarned()) img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_50_50));
            else
            if (turnData.isP_mostEarned()) img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_speaker_good));
            else
            if (turnData.isP_allEarned())
            {
                img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_speaker_good));
                img.setBackground(context.getResources().getDrawable(R.drawable.ic_icon_speaker_good_bg));
                DrawableCompat.setTint(
                        DrawableCompat.wrap(img.getBackground()),
                        ContextCompat.getColor(context, R.color.gold)
                );
            }
            else img.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_icon_speaker_bad));
        }


        if(content.equals(""))
        {
            /*if(xVoterNames.contains(resultsData.getPlayersOrdered().get(listPosition)))
            {
                content = "Didn\'t vote";
                expandedListTextView2.setTextColor(context.getResources().getColor(R.color.grey_font));
            }
            else*/
                content = " - ";
        }

        expandedListTextView2.setText(content);

        return convertView;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.resultsData.getTurnData().get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.resultsData.getTurnData().size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.resultsData.getTurnData().get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.resultsData.getTurnData().size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }



    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}