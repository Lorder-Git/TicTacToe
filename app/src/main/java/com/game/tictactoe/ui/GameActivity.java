package com.game.tictactoe.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.game.tictactoe.R;
import com.game.tictactoe.app.Constants;
import com.game.tictactoe.model.PlayGame;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    List<ImageView> cells;
    TextView txPlayerOne, txPlayerTwo;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    String uid, gameId, playerOneName = "", playerTwoName = "";
    PlayGame playGame;
    ListenerRegistration listenerGame;
    Bundle extras;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

        });

        initViews();
        initGame();

    }

    private void initGame() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        extras = getIntent().getExtras();
        gameId = extras.getString(Constants.EXTRA_GAME_ID);

    }

    private void initViews() {
        txPlayerOne = findViewById(R.id.textViewPlayerOne);
        txPlayerTwo = findViewById(R.id.textViewPlayerTwo);
        cells = new ArrayList<>();
        cells.add(findViewById(R.id.imageView0));
        cells.add(findViewById(R.id.imageView1));
        cells.add(findViewById(R.id.imageView2));
        cells.add(findViewById(R.id.imageView3));
        cells.add(findViewById(R.id.imageView4));
        cells.add(findViewById(R.id.imageView5));
        cells.add(findViewById(R.id.imageView6));
        cells.add(findViewById(R.id.imageView7));
        cells.add(findViewById(R.id.imageView8));
    }

    @Override
    protected void onStart() {
        super.onStart();
        gameListener();
    }

    private void gameListener() {

        listenerGame = db.collection("active_games")
                .document(gameId)
                .addSnapshotListener(GameActivity.this, (value, error) -> {
                    if (error != null){
                        Toast.makeText(this, "There is an ERROR to get the Game Data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String source = value != null
                            && value.getMetadata().hasPendingWrites() ? "Local" : "Server";
                    if (value.exists() && source.equals("Server")) {
                        // We parsing DocumentSnapshot > PLayGame
                        playGame = value.toObject(PlayGame.class);
                        if (playerOneName.isEmpty() || playerTwoName.isEmpty()) {
                            // get the name of the players
                            getPlayersName();
                        }
                    }
                });
    }

    private void getPlayersName() {
        // get the name of the player 1
        db.collection("users")
                .document(playGame.getPlayerOneId())
                .get()
                .addOnSuccessListener(GameActivity.this, documentSnapshot -> {
                    playerOneName = documentSnapshot.get("name").toString();
                    txPlayerOne.setText(playerOneName);
                });

        // get the name of the player 2
        db.collection("users")
                .document(playGame.getPlayerTwoId())
                .get()
                .addOnSuccessListener(GameActivity.this, documentSnapshot -> {
                    playerTwoName = documentSnapshot.get("name").toString();
                    txPlayerTwo.setText(playerTwoName);
                });

    }

    @Override
    protected void onStop() {
        if (listenerGame != null) {
            listenerGame.remove();
        }
        super.onStop();
    }
}