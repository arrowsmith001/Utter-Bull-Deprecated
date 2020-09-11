package com.arrowsmith.llv1.classes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.arrowsmith.llv1.GamePhase7RevealsFragment;
import com.arrowsmith.llv1.GamePhase8ResultsFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "debugtag";
    private final RoundDataInterpreter interpreter;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior,
                            RoundDataInterpreter interpreter) // Players' votes
    {
        super(fm, behavior);

        this.interpreter = interpreter;
        this.COUNT = interpreter.getTurnsCount() + 1; // Round reveals + 1 for the Results screen
        fragments = new ArrayList<>();

        Log.i(TAG, "ViewPagerAdapter: COUNT: "+getCount());

    }


    private int COUNT;
    private ArrayList<GamePhase7RevealsFragment> fragments;
    private GamePhase8ResultsFragment resultsFragment;

    @NonNull
    @Override public Fragment getItem(int position) {

        Fragment fragment = null;

        // If reveal page...
        if(position < (getCount() - 1)){

            Log.i(TAG, "VIEWPAGER: getItem: FRAGMENT TYPE 7 ADDED");

            fragment = (GamePhase7RevealsFragment) fragment;
            fragment = new GamePhase7RevealsFragment(interpreter.getTurnData().get(position));
            fragments.add((GamePhase7RevealsFragment)fragment);

        } else // If results page...
            if (position == (getCount() - 1)) {

                Log.i(TAG, "VIEWPAGER: getItem: FRAGMENT TYPE 8 ADDED");

                fragment = (GamePhase8ResultsFragment) fragment;
                fragment = new GamePhase8ResultsFragment(interpreter.getResultsData());
                resultsFragment = (GamePhase8ResultsFragment) fragment;
            }
        //fragment = new GamePhase5RevealsFragment(Integer.toString(position));

        return fragment;
    }

    public GamePhase8ResultsFragment getResultsFragment() {
        return resultsFragment;
    }

    public GamePhase7RevealsFragment getFragment(int position){
        return fragments.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    public void onPageSelected(int position) {

        if(position < (this.COUNT - 1)) fragments.get(position).setViewVis(0);

    }

}