package com.game.tictactoe.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.game.tictactoe.R;
import com.game.tictactoe.app.Constants;
import com.game.tictactoe.model.PlayGame;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    List<ImageView> cells;
//    int [][] ticGameSolution = new int[2][2];
    TextView txPlayerOne, txPlayerTwo;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseUser firebaseUser;
    String uid, gameId, playerOneName = "", playerTwoName = "", winnerId = "";
    PlayGame playGame;
    ListenerRegistration listenerGame = null;
    String playerName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(GameActivity.this, FindingGameActivity.class);
            startActivity(i);
            finish();
        });

        initViews();
        initGame();

    }

    private void initGame() {
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        Bundle extras = getIntent().getExtras();
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
        listenerGame = db.collection("games")
                .document(gameId)
                .addSnapshotListener(GameActivity.this, (snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(GameActivity.this, "There is an ERROR to get the Game Data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String source = snapshot != null
                            && snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server";

                    if (snapshot.exists() && source.equals("Server")) {
                        // We parsing DocumentSnapshot > PLayGame
                        playGame = snapshot.toObject(PlayGame.class);
                        if (playerOneName.isEmpty() || playerTwoName.isEmpty()) {
                            // get the name of the players
                            getPlayersName();
                        }
                        updateUI();
                    }
                    updatePlayersUI();
                });
    }

    private void updatePlayersUI() {
        if (playGame.isPlayerOneTurn()) {
            txPlayerOne.setTextColor(getResources().getColor(R.color.primaryTextColorNight));
            txPlayerTwo.setTextColor(getResources().getColor(R.color.primaryColorNight));
        } else {
            txPlayerOne.setTextColor(getResources().getColor(R.color.primaryColorNight));
            txPlayerTwo.setTextColor(getResources().getColor(R.color.primaryTextColorNight));
        }

        if (!playGame.getWinnerId().isEmpty()) {
            winnerId = playGame.getWinnerId();
            showDialogGameOver();
        }
    }

    private void updateUI() {
        for (int i = 0; i < 9; i++) {
            int currentCell = playGame.getSelectedCells().get(i);
            ImageView imCurrentCell = cells.get(i);
            if (currentCell == 0) {
                imCurrentCell.setImageResource(R.drawable.ic_square_svgrepo_com);
            } else if (currentCell == 1) {
                imCurrentCell.setImageResource(R.drawable.ic_cross_svgrepo_com);
            } else {
                imCurrentCell.setImageResource(R.drawable.ic_circle_svgrepo_com);
            }
        }
    }

    private void getPlayersName() {
        // get the name of the player 1
        db.collection("users")
                .document(playGame.getPlayerOneId())
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        playerOneName = documentSnapshot.get("name").toString();
                        txPlayerOne.setText(playerOneName);

                        if (playGame.getPlayerOneId().equals(uid)) {
                            playerName = playerOneName;
                        }
                    }
                });

        // get the name of the player 2
        db.collection("users")
                .document(playGame.getPlayerTwoId())
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        playerTwoName = documentSnapshot.get("name").toString();
                        txPlayerTwo.setText(playerTwoName);

                        if (playGame.getPlayerTwoId().equals(uid)) {
                            playerName = playerTwoName;
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        if (listenerGame != null) {
            listenerGame.remove();
        }
        super.onStop();
    }

    public void cellSelected(View view) {
        if (!playGame.getWinnerId().isEmpty()) {
            Toast.makeText(GameActivity.this, "The game is finished", Toast.LENGTH_SHORT).show();
        } else {
            if (playGame.isPlayerOneTurn() && playGame.getPlayerOneId().equals(uid)) {
                // playerOne is playing
                updateGame(view.getTag().toString());
            } else if (!playGame.isPlayerOneTurn() && playGame.getPlayerTwoId().equals(uid)) {
                // playerTwo is playing
                updateGame(view.getTag().toString());
            } else {
                Toast.makeText(GameActivity.this, "You need to wait your turn!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateGame(String cellNumber) {
        int positionCell = Integer.parseInt(cellNumber);

        if (playGame.getSelectedCells().get(positionCell) != 0) {
            Toast.makeText(GameActivity.this, "You need to selected a free cell", Toast.LENGTH_SHORT).show();
        } else {
            if (playGame.isPlayerOneTurn()) {
                cells.get(positionCell).setImageResource(R.drawable.ic_cross_svgrepo_com);
                playGame.getSelectedCells().set(positionCell, 1);
            } else {
                cells.get(positionCell).setImageResource(R.drawable.ic_circle_svgrepo_com);
                playGame.getSelectedCells().set(positionCell, 2);
            }

            if (existSolution()) {
                playGame.setWinnerId(uid);
                Toast.makeText(this, "There is a winner", Toast.LENGTH_SHORT).show();
            } else if (existATied()) {
                playGame.setWinnerId("TIED");
                Toast.makeText(this, "There is a TIED", Toast.LENGTH_SHORT).show();
            } else {
                changeTurn();
            }
            // update the info into Firestore the data game
            db.collection("games")
                    .document(gameId)
                    .set(playGame)
                    .addOnSuccessListener(GameActivity.this, aVoid -> {

                    })
                    .addOnFailureListener(GameActivity.this, e ->
                            Log.w("ERROR", "Error to send the data of the game"));
        }

    }

    private void changeTurn() {
        // Turn of the player
        playGame.setPlayerOneTurn(!playGame.isPlayerOneTurn());
    }

    private boolean existATied() {
        boolean exist = false;
        // Tied
        boolean freeCell = false;
        for (int i = 0; i < 9; i++) {
            if (playGame.getSelectedCells().get(i) == 0) {
                freeCell = true;
                break;
            }
        }
        if (!freeCell)
            exist = true;

        return exist;
    }

//    private boolean horizontalSolutions() {
//        List<Integer> selectedCells = playGame.getSelectedCells();
//
//        // horizontals solutions
//        ticGameSolution[0][0] = selectedCells.get(0);
//        ticGameSolution[1][0] = selectedCells.get(1);
//        ticGameSolution[2][0] = selectedCells.get(2);
//
//        ticGameSolution[0][1] = selectedCells.get(3);
//        ticGameSolution[1][1] = selectedCells.get(4);
//        ticGameSolution[2][1] = selectedCells.get(5);
//
//        ticGameSolution[0][2] = selectedCells.get(6);
//        ticGameSolution[1][2] = selectedCells.get(7);
//        ticGameSolution[2][2] = selectedCells.get(8);
//
//        return true;
//    }
//    private boolean verticalSolution() {
//        List<Integer> selectedCells = playGame.getSelectedCells();
//
//        // Verticals solutions
//        ticGameSolution[0][0] = selectedCells.get(0);
//        ticGameSolution[0][1] = selectedCells.get(1);
//        ticGameSolution[0][2] = selectedCells.get(2);
//
//        ticGameSolution[1][0] = selectedCells.get(3);
//        ticGameSolution[1][1] = selectedCells.get(4);
//        ticGameSolution[1][1] = selectedCells.get(5);
//
//        ticGameSolution[2][0] = selectedCells.get(6);
//        ticGameSolution[2][1] = selectedCells.get(7);
//        ticGameSolution[2][2] = selectedCells.get(8);
//
//        return false;
//    }
//
//    private boolean diagonalSolution() {
//        List<Integer> selectedCells = playGame.getSelectedCells();
//
//        // Diagonals solutions
//        ticGameSolution[0][0] = selectedCells.get(0);
//        ticGameSolution[1][1] = selectedCells.get(4);
//        ticGameSolution[2][2] = selectedCells.get(8);
//
//        ticGameSolution[0][2] = selectedCells.get(2);
//        ticGameSolution[1][1] = selectedCells.get(4);
//        ticGameSolution[2][0] = selectedCells.get(6);
//
//        return true;
//    }

    private boolean existSolution() {
        boolean exist = false;
        List<Integer> selectedCells = playGame.getSelectedCells();

        // Horizontal
        // Solution 0 - 1 - 2
        if (selectedCells.get(0).equals(selectedCells.get(1))
                && selectedCells.get(1).equals(selectedCells.get(2))
                && !selectedCells.get(2).equals(0)) {
            exist = true;
        } else
            // Solution 3 - 4 - 5
            if (selectedCells.get(3).equals(selectedCells.get(4))
                    && selectedCells.get(4).equals(selectedCells.get(5))
                    && !selectedCells.get(5).equals(0)) {
                exist = true;
            } else
                // Solution 6 - 7 - 8
                if (selectedCells.get(6).equals(selectedCells.get(7))
                        && selectedCells.get(7).equals(selectedCells.get(8))
                        && !selectedCells.get(8).equals(0)) {
                    exist = true;
                } else // Verticals
                    // Solution 0 - 3 - 6
                    if (selectedCells.get(0).equals(selectedCells.get(3))
                            && selectedCells.get(3).equals(selectedCells.get(6))
                            && !selectedCells.get(6).equals(0)) {
                        exist = true;
                    } else
                        // Solution 1 - 4 - 7
                        if (selectedCells.get(1).equals(selectedCells.get(4))
                                && selectedCells.get(4).equals(selectedCells.get(7))
                                && !selectedCells.get(7).equals(0)) {
                            exist = true;
                        } else
                            // Solution 2 - 5 - 8
                            if (selectedCells.get(2).equals(selectedCells.get(5))
                                    && selectedCells.get(5).equals(selectedCells.get(8))
                                    && !selectedCells.get(8).equals(0)) {
                                exist = true;
                            } else // Diagonals
                                // Solution 0 - 4 - 8
                                if (selectedCells.get(0).equals(selectedCells.get(4))
                                        && selectedCells.get(4).equals(selectedCells.get(8))
                                        && !selectedCells.get(8).equals(0)) {
                                    exist = true;
                                } else
                                    // Solution 2 - 4 - 6
                                    if (selectedCells.get(2).equals(selectedCells.get(2))
                                            && selectedCells.get(4).equals(selectedCells.get(6))
                                            && !selectedCells.get(6).equals(0)) {
                                        exist = true;
                                    }

        return exist;
    }

    public void showDialogGameOver() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        // Reference of the view Components
        TextView txtPoints = v.findViewById(R.id.textViewPoints);
        TextView txtInfo = v.findViewById(R.id.textViewInformation);
        LottieAnimationView gameOverAnimation = v.findViewById(R.id.animation_view);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Game Over");
        builder.setCancelable(false);
        builder.setView(v);

        if (winnerId.equals("TIED")) {
            txtInfo.setText(playerName.toUpperCase() + " is a Tied!!");
            txtPoints.setText("+1 points");
        } else if (winnerId.equals(uid)) {
            txtInfo.setText(playerName.toUpperCase() + " you win the game!!");
            txtPoints.setText("+5 points");
        } else {
            txtInfo.setText(playerName.toUpperCase() + " you lose the game!!");
            txtPoints.setText("+0 points");
            gameOverAnimation.setAnimation("animation-desperate.json");
        }

        gameOverAnimation.playAnimation();

        // Add the buttons
        builder.setPositiveButton("Exit", (dialog, id) -> {
            // User clicked OK button
            Intent i = new Intent(GameActivity.this, FindingGameActivity.class);
            startActivity(i);
            finish();
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();

    }

}