package com.arrowsmith.llv1.classes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.arrowsmith.llv1.GamePhase2TextEntryFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import static com.arrowsmith.llv1.MainActivity.TAG;

public class QueryHelper {

    // DEBUG ONLY
    private String customIdea;
    // DEBUG ONLY

    private boolean truthOrLie;
    private boolean lewdnessAllowed;
    private FirebaseFirestore fs;
    QueryRetriever qr;

    private CollectionReference categoriesRef;
    private CollectionReference ideasRef;


    public QueryHelper(boolean truthOrLie, FirebaseFirestore firestoreReference, QueryRetriever queryRetriever) {

        this.truthOrLie = truthOrLie;
        this.fs = firestoreReference;
        this.qr = queryRetriever;
        this.lewdnessAllowed = true; // By default, lewdness allowed

        ideasRef = fs.collection("ideas");
        categoriesRef = fs.collection("categories");
    }

    public void setLewdnessAllowed(boolean lewdnessAllowed) {
        this.lewdnessAllowed = lewdnessAllowed;
    }

    public void start(){

        if(customIdea == null)
        {
            runQuery();
        }
        else
        {
            onTextContentExtracted(customIdea);
        }

    }

    private void runQuery() {

        double randomNumber;
        randomNumber = Math.random();

        // Create a query against the collection
        Query query = ideasRef
                .whereEqualTo("trueiftruth", truthOrLie)
                .whereGreaterThan("random", randomNumber)
                .orderBy("random", Query.Direction.ASCENDING)
                .limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            private String text;

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // If the query is a success...

                onIdeasQueryComplete(task);
            }
        });

    }

    private void onIdeasQueryComplete(Task<QuerySnapshot> task) {

        String text;

        if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {

            // Get content
            text = (String) task.getResult().getDocuments().get(0).get("content");
            Log.i(TAG, "onIdeasQueryComplete: Query outcome: " + text);

            // Get lewd
            boolean isLewd = (boolean) task.getResult().getDocuments().get(0).get("lewd");

            if (isLewd && !lewdnessAllowed)
            {
                Log.i(TAG, "onIdeasQueryComplete: LEWD - rerunning");
                runQuery();
            }
            else
            {
                onTextContentExtracted(text);
            }

        } else {
            Log.i(TAG, "onIdeasQueryComplete: Query FAILED: "+task.getException().getMessage());
            // callQueryMethod();
        }

    }

    private void onTextContentExtracted(String text) {

        // Check if querying required
        Matcher mCat = checkForCategories(text);

        // If categories exist, we have the mCat to go forward...
        if (mCat.find()) resolveNextCategory(text, mCat);
        else finalFormatting(text);

        // TODO: Implement lewdness filter
        // Boolean isLewd = (Boolean) task.getResult().getDocuments().get(0).get("true_if_truth");

    }

    private Matcher checkForCategories(String text) {

        // Figure out category patterns
        Pattern patternCat = Pattern.compile("(?<=\\[)([^\\]]+)(?=\\])");

        return patternCat.matcher(text);
    }

    private void resolveNextCategory(String text, Matcher matcher) {

        Matcher mCat = matcher;

        // Extract category content
        String allSubCats = text.substring(mCat.start(),mCat.end());

        // If this category is singular
        if(!allSubCats.contains("/"))
        {
            Log.i(TAG, "resolveNextCategory: LEWD - rerunning");
            queryCategories(text, allSubCats, mCat);
        }
        else // Multiple categories case
        {
            String[] allSubCatsSplit = allSubCats.split("/");
            int randomIndex = (int) (Math.random() * ((double) allSubCatsSplit.length));

            // Choose random category
            String randomCategory = allSubCatsSplit[randomIndex];

            // Replace original multi-category with its single chosen one
            text = text.replaceFirst(allSubCats,randomCategory);

            // Query for this category
            queryCategories(text, randomCategory, mCat);
        }
    }


    private void queryCategories(String text, String category, Matcher matcher) {

        Log.i(TAG, "queryCategories: QUERYING "+category+" FOR TEXT "+text);

        double randomNumber;
        randomNumber = Math.random();

        // Create a query against the collection
        Query query = categoriesRef
                .whereEqualTo("category", category)
                .whereGreaterThan("random", randomNumber)
                .orderBy("random", Query.Direction.ASCENDING)
                .limit(1);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // If the query is a success...

                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {

                    // Get content
                    String resultText = (String) task.getResult().getDocuments().get(0).get("entry");

                    // Get lewd
                    boolean isLewd = (boolean) task.getResult().getDocuments().get(0).get("lewd");

                    if(isLewd && !lewdnessAllowed)
                    {
                        queryCategories(text, category, matcher);
                    }
                    else
                    {
                        // Replace the first category of this kind with the result of query
                        String newText = text.replaceFirst("\\["+category+"\\]",resultText);

                        // If matcher has another match, resolve next category
                        // else, check again for matches
                        if(matcher.find()) resolveNextCategory(newText, matcher);
                        else {
                            Matcher newMCat = checkForCategories(newText);
                            if (newMCat.find())
                            {
                                Log.i(TAG, "onComplete: cat: "+category+", entry:"+newText);
                                resolveNextCategory(newText, newMCat);
                            }
                            else
                            {
                                Log.i(TAG, "onComplete: cat: "+category+", entry:"+newText);
                                finalFormatting(newText);
                            }
                        }

                    }

                } else {
                    try {
                        Log.i(TAG, "onComplete: Query FAILED: "+task.getException().getMessage());
                    }catch(Exception e)
                    {
                        Log.i(TAG, "onComplete: ERROR IN QUERY FAILED: "+e.getMessage());
                    }
                    // TODO: Act upon query fail
                }
            }
        });
    }


    private void finalFormatting(String text) {

        String newText = text;

        newText = dealWithNumbers(newText);
        newText = dealWithArticle(newText);
        newText = capitalise(newText);

        qr.setHintText(newText);
    }

    private String dealWithNumbers(String text) {

        String newText = text;

        // DEAL WITH NUMBERS
        Pattern patternNum = Pattern.compile("(?<=<)([^>]+)(?=>)");
        Matcher mNum = patternNum.matcher(text);

        while(mNum.find()){

            String numbers = text.substring(mNum.start(),mNum.end());
            String[] numbersSplit = numbers.split("[-]");
            int[] n = new int[2];

            for(int i=0; i < 2;i++){
                n[i] = Integer.parseInt(numbersSplit[i]);
            }

            double randomNumber;
            randomNumber = Math.random();

            int number = n[0] + (int)(randomNumber*(((double)n[1] - n[0])));

            Log.i(TAG, "formatCategory: NUMBERSPLIT: "+numbersSplit);
            Log.i(TAG, "formatCategory: NUMBERS: "+numbers);
            Log.i(TAG, "formatCategory: NUMBER: "+number);

            newText = text.replaceFirst("<"+numbers+">",Integer.toString(number));
        }

        return newText;
    }

    private String dealWithArticle(String text) {

        String newText = text;

        // DEAL WITH NUMBERS
        Pattern patternNum = Pattern.compile("@");
        Matcher mArt = patternNum.matcher(text);

        while(mArt.find()){

            Log.i(TAG, "dealWithArticle: mART FOUND");

            char charAfter = ' ';
            int incr = 0;

            while(charAfter == ' ')
            {
                incr++;
                if(mArt.start() + incr > text.length() - 1) {
                    Log.i(TAG, "dealWithArticle: INDEX EXCEEDED BOUNDS");
                    return newText;
                }
                else {
                    charAfter = text.charAt(mArt.start() + incr);
                    Log.i(TAG, "dealWithArticle: CHAR NOW "+charAfter);
                }
            }

            String vowelSet = "aeiouAEIOU";
            String article;

            // If not a vowel...
            if(vowelSet.indexOf(charAfter) == -1) {
                article = "a";
            }
            else article = "an";

            Log.i(TAG, "dealWithArticle: @ to change to "+article);

            newText = newText.replaceFirst("@",article);
        }

        return newText;
    }

    private String capitalise(String text) {

        // TODO: So far only capitalises first letter - potential for capitalising elsewhere where necessary? i.e. after punctuation

        String newText = text;

        // Capitalise first letter only
        newText = Character.toString(newText.charAt(0)).toUpperCase()
                + newText.substring(1);

        return newText;
    }

    public void setCustomIdea(String customIdea) {
        this.customIdea = customIdea;
    }

    public interface QueryRetriever {
        void setHintText(String text);
    }
}
