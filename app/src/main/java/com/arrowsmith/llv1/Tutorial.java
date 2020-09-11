package com.arrowsmith.llv1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.arrowsmith.llv1.classes.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import static com.arrowsmith.llv1.MainActivity.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tutorial extends Fragment {

    private View view;
    private ViewPager pager;
    private MainActivity main;
    private Button btnExit;
    private Button btnNextPage;

    public Tutorial() {
        // Required empty public constructor
    }

    private boolean firstTimeCase;
    private boolean fromMainMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.fragment_tutorial, container, false);
        main = (MainActivity) getActivity();

        // Initialise button views
        btnExit = (Button) view.findViewById(R.id.button_exit_tutorial);
        btnNextPage = (Button) view.findViewById(R.id.button_next_tutorial_page);

        // Decide if this is first time case or not
        firstTimeCase = main.prefs.getBoolean("firstStart",true);

        if(getArguments() != null)
        {
            fromMainMenu = TutorialArgs.fromBundle(getArguments()).getFromMainMenu();
        }
        else
        {
            fromMainMenu = false;
        }


        // Change first time case
        SharedPreferences.Editor editor = main.prefs.edit();
        editor.putBoolean("firstStart",false);
        editor.apply();
        Log.i(TAG, "onCreateView: TUTORIAL: " + main.prefs.getBoolean("firstStart",false));

        setUpPager();

        setButtons();

        return view;
    }

    private void setButtons() {

        if(firstTimeCase)
        {
            btnExit.setText("SKIP TUTORIAL");
        }
        else
        {
            btnExit.setText("EXIT TUTORIAL");
        }

        btnExit.setOnClickListener(v ->
        {
            moveToMainMenu();
        });

        btnNextPage.setOnClickListener(v ->
        {
            pager.setCurrentItem(
                    Math.min(
                            pager.getCurrentItem() + 1, pager.getAdapter().getCount() - 1),
                    true);
        });


    }

    private void setUpPager() {

        pager = (ViewPager) view.findViewById(R.id.pagerTutorial);
        TabLayout tabs = (TabLayout) view.findViewById(R.id.tabsTutorial);
        tabs.setupWithViewPager(pager, true);
        tabs.setTabMode(TabLayout.MODE_FIXED);

        TutorialViewPagerAdapter adapter = new TutorialViewPagerAdapter(
                getChildFragmentManager(), ViewPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, main);
        pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        tabs.getTabAt(0).setIcon(getResources().getDrawable(R.drawable.tab_selector_2));
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {/*
                if(position > 0) tabs.getTabAt(position - 1).setIcon(getResources().getDrawable(R.drawable.tab_selector_2));
                if(position == tabs.getTabCount() - 1) tabs.getTabAt(position).setIcon(getResources().getDrawable(R.drawable.tab_selector_2));*/
                onPageScrollStateChanged(ViewPager.SCROLL_STATE_SETTLING);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                int upTo = pager.getCurrentItem();

                if(state == ViewPager.SCROLL_STATE_IDLE)
                {
                    tabs.getTabAt(upTo).setIcon(getResources().getDrawable(R.drawable.tab_selector_2));
                }

                if(state == ViewPager.SCROLL_STATE_SETTLING) {

                    for(int i = 0; i < upTo; i++) {
                        tabs.getTabAt(i).setIcon(getResources().getDrawable(R.drawable.tab_selector_2));
                    }

                    if(pager.getCurrentItem() == pager.getAdapter().getCount() - 1)
                    {
                        handler.postDelayed(lastPageRunnable, 1000);
                    }
                }

            }
        });

        // Set tab icons
        for(int i = 1; i < tabs.getTabCount(); i++)
        {
            tabs.getTabAt(i).setIcon(getResources().getDrawable(R.drawable.tab_selector));
        }

    }

    Handler handler = new Handler();
    Runnable lastPageRunnable = new Runnable() {
        @Override
        public void run() {

            onLastPage();

        }
    };

    private void onLastPage() {

        if(firstTimeCase)
        {
            btnExit.setVisibility(View.GONE);

            btnNextPage.setText("GET STARTED!");
            btnNextPage.getBackground().setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.MULTIPLY);
            btnNextPage.setTextColor(getResources().getColor(R.color.white));
            btnNextPage.setOnClickListener(v -> {
                moveToMainMenu();
            });
        }
        else
        {
            btnNextPage.setVisibility(View.GONE);
        }
    }

    public void moveToMainMenu() {

        Log.i(TAG, "moveToTutorial: CALLED FROM FRAG");

        NavController navController = Navigation.findNavController(getActivity(), R.id.my_nav_host_fragment);
        TutorialDirections.ActionTutorialToMainMenu action = TutorialDirections.actionTutorialToMainMenu();
        action.setShowButtonsNow(firstTimeCase || fromMainMenu);
        navController.navigate(action);
    }

    //////////////////////////////////////////////////////////////////

    private Tutorial.OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Tutorial.OnFragmentInteractionListener) {
            mListener = (Tutorial.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
class TutorialViewPagerAdapter extends FragmentPagerAdapter {

    private MainActivity main;

    // INPUTS
    String[] pageText;
    Drawable[] pageImg;

    public TutorialViewPagerAdapter(@NonNull FragmentManager fm, int behavior, MainActivity main)
    {
        super(fm, behavior);
        this.main = main;

        setPageText();
        setImages();
    }

    private void setImages() {
        pageImg = new Drawable[]{
                main.getResources().getDrawable(R.drawable.tutorial_1),
                main.getResources().getDrawable(R.drawable.tutorial_2),
                main.getResources().getDrawable(R.drawable.tutorial_3),
                main.getResources().getDrawable(R.drawable.tutorial_4),
                main.getResources().getDrawable(R.drawable.tutorial_5),
                main.getResources().getDrawable(R.drawable.tutorial_6),
                main.getResources().getDrawable(R.drawable.tutorial_7),
                main.getResources().getDrawable(R.drawable.tutorial_8),
                main.getResources().getDrawable(R.drawable.tutorial_9),
                main.getResources().getDrawable(R.drawable.tutorial_10)
        };

    }

    private void setPageText() {
        pageText = new String[]{
                main.getResources().getString(R.string.tutorial_p1),
                main.getResources().getString(R.string.tutorial_p2),
                main.getResources().getString(R.string.tutorial_p3),
                main.getResources().getString(R.string.tutorial_p4),
                main.getResources().getString(R.string.tutorial_p5),
                main.getResources().getString(R.string.tutorial_p6),
                main.getResources().getString(R.string.tutorial_p7),
                main.getResources().getString(R.string.tutorial_p8),
                main.getResources().getString(R.string.tutorial_p9),
                main.getResources().getString(R.string.tutorial_p10)
        };
    }


    @Override public Fragment getItem(int position) {

        Fragment fragment = null;

        fragment = (TutorialPage) fragment;
        fragment = new TutorialPage(pageText[position],
                pageImg[position],
                position);

        return fragment;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getCount() {
        return pageText.length;
    }


}
