package com.game.tictactoe.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.game.tictactoe.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextView textViewEmail, textViewPassword;
    private Button btnLogin, btnRegister;
    private ScrollView form;
    private ProgressBar pbLogin;
    String email, password;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        // 1. Instantiate an AlertDialog.Builder with its constructor
        builder = new AlertDialog.Builder(this);

        findViews();
        changeLoginFormVisibility(true);
        events();
    }

    private void findViews() {
        textViewEmail = findViewById(R.id.editTextEmail);
        textViewPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        btnRegister = findViewById(R.id.buttonToRegister);
        form = findViewById(R.id.formLogin);
        pbLogin = findViewById(R.id.progressBarLogin);

    }

    private void events() {
        btnLogin.setOnClickListener(view -> {
            email = textViewEmail.getText().toString();
            password = textViewPassword.getText().toString();

            if (email.isEmpty()){
                showEmailDialog();
                textViewEmail.setError("The email is required, please try again!!");
            } else if (password.isEmpty()){
                showPasswordDialog();
                textViewPassword.setError("The password is required, please try again!!");
            } else {
                loginUser();
                changeLoginFormVisibility(false);
            }

        });

        btnRegister.setOnClickListener(view -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        });
    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w("TAG", "SignIn Error ", task.getException());
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // We store the user info into the firestore
            Intent i = new Intent(LoginActivity.this, FindingGameActivity.class);
            startActivity(i);
        } else {
            changeLoginFormVisibility(true);
            showDialogToUser();
            btnLogin.setError("Can you check your credentials again, there is something wrong!!");
            btnLogin.requestFocus();
        }
    }

    private void changeLoginFormVisibility(boolean showForm) {
        pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE);
        form.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }

    private void showEmailDialog() {
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("There is something wrong with your EMAIL, please check again!!")
                .setTitle("ERROR WITH EMAIL");

        // Add the buttons
        builder.setPositiveButton("TRY AGAIN", (dialog, id) -> {
            // User clicked OK button
            dialog.dismiss();
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPasswordDialog() {
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("There is something wrong with your PASSWORD, please check again!!")
                .setTitle("ERROR WITH YOUR PASSWORD");

        // Add the buttons
        builder.setPositiveButton("TRY AGAIN", (dialog, id) -> {
            // User clicked OK button
            dialog.dismiss();
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDialogToUser() {

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("You need to be registered into the app, please click to make a register!!")
                .setTitle("YOU ARE NOT REGISTERED!!!");

        // Add the buttons
        builder.setPositiveButton("REGISTER", (dialog, id) -> {
            // User clicked OK button
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
            dialog.dismiss();
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}