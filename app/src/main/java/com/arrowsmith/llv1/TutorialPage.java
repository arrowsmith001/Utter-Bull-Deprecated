package com.arrowsmith.llv1;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialPage extends Fragment {

    private static final String TAG = "debugtag";
    private int position;
    private Drawable img;
    private String textContent;
    private View view;
    private TextView content;
    private ImageView imgView;

    public TutorialPage() {
        // Required empty public constructor
    }

    public TutorialPage(String textContent,
                        Drawable img,
                        int position) {
        this.textContent = textContent;
        this.img = img;
        this.position = position;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (View) inflater.inflate(R.layout.fragment_tutorial_page, container, false);

        content = (TextView) view.findViewById(R.id.textTutorialContent);
        imgView = (ImageView) view.findViewById(R.id.imageTutorialPage);

        content.setText(textContent);

        /*imgView.setImageDrawable(img);
        img.start();*/
        imgView.setImageDrawable(img);
        try{
            ((AnimationDrawable)img).start();
        }catch (Exception e)
        {
            Log.i(TAG, "onCreateView: caught exception");
        }




        return view;
    }
}
