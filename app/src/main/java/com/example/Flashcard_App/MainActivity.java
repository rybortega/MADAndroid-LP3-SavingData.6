package com.example.Flashcard_App;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView flashcardQuestion;
    TextView flashcardAnswer;

    FlashcardDatabase flashcardDatabase;
    List<Flashcard> allFlashcards;

    int currentCardDisplayedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flashcardDatabase = new FlashcardDatabase(this);
        allFlashcards = flashcardDatabase.getAllCards();

        if (allFlashcards != null && allFlashcards.size() > 0) {
            ((TextView) findViewById(R.id.flashcard_question)).setText(allFlashcards.get(0).getQuestion());
            ((TextView) findViewById(R.id.flashcard_answer)).setText(allFlashcards.get(0).getAnswer());
        }

        flashcardQuestion = findViewById(R.id.flashcard_question);
        flashcardAnswer = findViewById(R.id.flashcard_answer);

        flashcardQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View answerSideView = findViewById(R.id.flashcard_answer);
                View questionSideView = findViewById(R.id.flashcard_question);

                // get the center for the clipping circle
                int cx = answerSideView.getWidth() / 2;
                int cy = answerSideView.getHeight() / 2;

                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);

                // create the animator for this view (the start radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(answerSideView, cx, cy, 0f, finalRadius);

                // hide the question and show the answer to prepare for playing the animation!
                questionSideView.setVisibility(View.INVISIBLE);
                answerSideView.setVisibility(View.VISIBLE);

                anim.setDuration(3000);
                anim.start();
            }
        });

        flashcardAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View answerSideView = findViewById(R.id.flashcard_answer);
                View questionSideView = findViewById(R.id.flashcard_question);

                // get the center for the clipping circle
                int cx = answerSideView.getWidth() / 2;
                int cy = answerSideView.getHeight() / 2;

                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);

                // create the animator for this view (the start radius is zero)
                Animator anim = ViewAnimationUtils.createCircularReveal(questionSideView, cx, cy, 0f, finalRadius);

                // hide the question and show the answer to prepare for playing the animation!
                answerSideView.setVisibility(View.INVISIBLE);
                questionSideView.setVisibility(View.VISIBLE);

                anim.setDuration(3000);
                anim.start();
            }
        });

        ImageView addQuestionImageView = findViewById(R.id.addButton);
        addQuestionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivityForResult(intent, 100);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Animation leftOutAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.left_out);
                final Animation rightInAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.right_in);

                // don't try to go to next card if you have no cards to begin with
                if (allFlashcards.size() == 0)
                    return;
                // advance our pointer index so we can show the next card
                currentCardDisplayedIndex++;

                // make sure we don't get an IndexOutOfBoundsError if we are viewing the last indexed card in our list
                if(currentCardDisplayedIndex >= allFlashcards.size()) {
                    Snackbar.make(v,
                            "You've reached the end of the cards, going back to start.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                    currentCardDisplayedIndex = 0;
                }

                findViewById(R.id.flashcard_question).startAnimation(leftOutAnim);

                leftOutAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // this method is called when the animation first starts
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // set the question and answer TextViews with data from the database
                        allFlashcards = flashcardDatabase.getAllCards();
                        Flashcard flashcard = allFlashcards.get(currentCardDisplayedIndex);

                        ((TextView) findViewById(R.id.flashcard_question)).setText(flashcard.getAnswer());
                        ((TextView) findViewById(R.id.flashcard_answer)).setText(flashcard.getQuestion());

                        findViewById(R.id.flashcard_question).startAnimation(rightInAnim);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // we don't need to worry about this method
                    }
                });
            }
        });

        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashcardDatabase.deleteCard(((TextView) findViewById(R.id.flashcard_question)).getText().toString());
                allFlashcards = flashcardDatabase.getAllCards();

                if (allFlashcards.isEmpty()) {
                    flashcardQuestion.setText("Add a new card!");
                } else {
                    currentCardDisplayedIndex--;

                    if (currentCardDisplayedIndex == -1) {
                        currentCardDisplayedIndex = allFlashcards.size() - 1;
                    }

                    Flashcard flashcard = allFlashcards.get(currentCardDisplayedIndex);
                    flashcardQuestion.setText(flashcard.getQuestion());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (data != null && resultCode == RESULT_OK) {
                String questionString =  data.getExtras().getString("QUESTION_KEY");
                String answerString =  data.getExtras().getString("ANSWER_KEY");
                flashcardQuestion.setText(questionString);
                flashcardAnswer.setText(answerString);

                flashcardDatabase.insertCard(new Flashcard(questionString, answerString));
                allFlashcards = flashcardDatabase.getAllCards();
            }
        }
    }
}