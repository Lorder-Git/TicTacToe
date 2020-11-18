package com.game.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.game.tictactoe.R;
import com.game.tictactoe.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private TextView textViewNameRegister, textViewEmailRegister, textViewPasswordRegister;
    private Button btnToLogin, btnRegister;
    private ScrollView formRegister;
    private ProgressBar pbRegister;
    String name, email, password;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    AlertDialog.Builder builder;
    User newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Instantiate an AlertDialog.Builder with its constructor
        builder = new AlertDialog.Builder(this);

        findViews();
        events();
    }

    private void findViews() {
        textViewNameRegister = findViewById(R.id.editTextNameRegister);
        textViewEmailRegister = findViewById(R.id.editTextEmailRegister);
        textViewPasswordRegister = findViewById(R.id.editTextPasswordRegister);
        btnToLogin = findViewById(R.id.buttonToLogin);
        btnRegister = findViewById(R.id.buttonRegister);
        formRegister = findViewById(R.id.formRegister);
        pbRegister = findViewById(R.id.progressBarRegister);
    }



    private void events() {
        btnRegister.setOnClickListener(view -> {
            name = textViewNameRegister.getText().toString();
            email = textViewEmailRegister.getText().toString();
            password = textViewPasswordRegister.getText().toString();

            if (name.isEmpty()){
                showNameDialog();
              textViewNameRegister.setError("The name is required, please fill it!!");
            } else if (email.isEmpty()){
                showEmailDialog();
                textViewEmailRegister.setError("The email is required, please fill it!!");
            } else if (password.isEmpty()){
                showPasswordDialog();
                textViewPasswordRegister.setError("The password is required, please fill it!!");
            } else {
                registerUser();
                changeRegisterFormVisibility(false);
            }

        });

        btnToLogin.setOnClickListener(view -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        });
    }

    private void registerUser() {
        changeRegisterFormVisibility(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
//                            Toast.makeText(RegisterActivity.this, "It was an error in the register, please try again", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // We store the user info into the firestore
            newUser = new User(name, 0, 0);

            db.collection("users")
                    .document(user.getUid())
                    .set(newUser)
                    .addOnSuccessListener(aVoid -> {
                        finish();
                        Intent i = new Intent(RegisterActivity.this, FindingGameActivity.class);
                        startActivity(i);
                    });
        } else {
            changeRegisterFormVisibility(true);
            showDialogToUserSameEmail();
            btnRegister.setError("Can you check your credentials again, there is something wrong!!");
            btnRegister.requestFocus();
        }
    }

    private void changeRegisterFormVisibility(boolean showForm) {
        pbRegister.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formRegister.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }

    private void showNameDialog() {
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("There is something wrong with your NAME, please check again!!")
                .setTitle("ERROR WITH THE NAME");

        // Add the buttons
        builder.setPositiveButton("TRY AGAIN", (dialog, id) -> {
            // User clicked OK button
            dialog.dismiss();
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
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

    private void showDialogToUserSameEmail() {

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Would you like to make a login?")
                .setTitle("CREDENTIALS IN USE");

        // Add the buttons
        builder.setPositiveButton("YES", (dialog, id) -> {
            // User clicked OK button
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            dialog.dismiss();
        });
        builder.setNegativeButton("NO", (dialog, id) -> {
                // User cancelled the dialog
            dialog.dismiss();
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}