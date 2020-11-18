package com.game.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.game.tictactoe.R;
import com.game.tictactoe.app.Constants;
import com.game.tictactoe.model.PlayGame;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FindingGameActivity extends AppCompatActivity {

    private TextView txtViewLoading;
    private ProgressBar progressBar;
    private ScrollView layoutProgressBar, layoutGameMenu;
    private Button btnPLay, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid, gameId;
    private PlayGame playGame;
    private ListenerRegistration listenerRegistration = null;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_game);

        initFindViews();
        initProgressBar();
        initFirebase();
        initEvents();

    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db =  FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
    }

    private void initEvents() {
        btnPLay.setOnClickListener(view -> {
            changeMenuVisivility(false);
            findFreeGameToPlay();
        });
        btnRanking.setOnClickListener(view -> {

        });
    }

    private void findFreeGameToPlay() {
        txtViewLoading.setText("Searching for a free opponent to Play ...");
        animationView.playAnimation();

        db.collection("active_games")
                .whereEqualTo("playerTwoId", "")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() == 0){
                            // There is not a free game, so we create one
                            createNewGame();
                        } else {
                            boolean found = false;
                            for (DocumentSnapshot docPLay: task.getResult().getDocuments()) {
                                if (!docPLay.get("playerOneId").equals(uid)) {
                                    found = true;
                                    gameId = docPLay.getId();
                                    playGame = docPLay.toObject(PlayGame.class);
                                    playGame.setPlayerTwoId(uid);

                                    db.collection("active_games")
                                            .document(gameId)
                                            .set(playGame)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(FindingGameActivity.this,
                                                            "Am so sorry, there isn't a second Player, try later!!",
                                                            Toast.LENGTH_LONG).show();
                                                    changeMenuVisivility(true);

                                                    txtViewLoading.setText("Found a game for you ...");
                                                    animationView.setRepeatCount(0);
                                                    animationView.setAnimation("checked_animation.json");
                                                    animationView.playAnimation();

                                                    new Handler().postDelayed(() -> startGame(), 1500);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            changeMenuVisivility(true);
                                            Toast.makeText(FindingGameActivity.this,
                                                    "There is a problem finding the proper game for you",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                }
                                if (!found) createNewGame();
                            }
                        }
                    }
                });
    }

    private void createNewGame() {
        txtViewLoading.setText("Creating game just for you ...");
        PlayGame newGame = new PlayGame(uid);

            db.collection("active_games")
                    .add(newGame)
                    .addOnSuccessListener(documentReference -> {
                        gameId = documentReference.getId();
                        // We wait until the second player start to play
                        waitForPLayer();
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            changeMenuVisivility(true);
                            Toast.makeText(FindingGameActivity.this,
                                    "There is a problem creating the proper game for you",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void waitForPLayer() {
        txtViewLoading.setText("Waiting for the opponent...");
        listenerRegistration = db.collection("active_games")
                .document(gameId)
                .addSnapshotListener((value, error) -> {
                    if (!value.get("playerTwoId").equals("")){
                        txtViewLoading.setText("Starting the game...");

                        animationView.setRepeatCount(0);
                        animationView.setAnimation("checked_animation.json");
                        animationView.playAnimation();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startGame();
                            }
                        }, 1500);
                    }
                });
    }

    private void startGame() {
        if (listenerRegistration != null){
            listenerRegistration.remove();
        }
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(Constants.EXTRA_GAME_ID, gameId);
        startActivity(i);
        gameId = "";
    }

    private void initProgressBar() {
        animationView = findViewById(R.id.animation_view);


        progressBar.setIndeterminate(true);
        txtViewLoading.setText("Loading ...");

        changeMenuVisivility(true);

    }

    private void changeMenuVisivility(boolean showMenu) {
        layoutProgressBar.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutGameMenu.setVisibility(showMenu ? View.VISIBLE: View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameId != null){
            changeMenuVisivility(false);
            waitForPLayer();
        } else {
            changeMenuVisivility(true);
        }
    }

    @Override
    protected void onStop() {
        if (listenerRegistration != null){
            listenerRegistration.remove();
        }
        if (!gameId.equals("")){
            db.collection("active_games")
                    .document(gameId)
                    .delete()
                    .addOnCompleteListener(task -> {
                        gameId = "";
                    });
        }
        super.onStop();
    }

    private void initFindViews() {
        txtViewLoading = findViewById(R.id.textViewLoading);
        progressBar = findViewById(R.id.progressBarPlaying);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        layoutGameMenu = findViewById(R.id.layoutGameMenu);
        btnPLay = findViewById(R.id.buttonPlay);
        btnRanking = findViewById(R.id.buttonRanking);
    }


}