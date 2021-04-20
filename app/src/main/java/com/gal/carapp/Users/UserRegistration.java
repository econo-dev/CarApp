package com.gal.carapp.Users;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.BackendlessUser;
import com.gal.carapp.R;
import com.sdsmdg.tastytoast.TastyToast;

public class UserRegistration extends AppCompatActivity {
    Context context;
    Users_Backendless user;

    Button btnRegNewUser, btnRegCancel;

    EditText txtUserName, txtRegPass1, txtRegPass2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        setPointer();
    }

    private void setPointer() {
        this.context=this;
        user = new Users_Backendless(context);

        txtUserName = findViewById(R.id.txtRegUserName);
        txtRegPass1 = findViewById(R.id.txtRegPass1);
        txtRegPass2 = findViewById(R.id.txtRegPass2);

        btnRegNewUser = findViewById(R.id.btnRegNewUser);
        btnRegCancel = findViewById(R.id.btnRegCancel);

        btnRegCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnRegNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!txtRegPass1.getText().toString().equals(txtRegPass2.getText().toString())){
                    TastyToast.makeText(context, "Passwords mismatch", TastyToast.LENGTH_SHORT,TastyToast.ERROR).show();
                    return;
                }
                if(txtUserName.getText().toString().length()<3){
                    TastyToast.makeText(context, "User name must contain at least 3 letters", TastyToast.LENGTH_SHORT,TastyToast.ERROR).show();
                    return;
                }
                user.addNewUser(txtUserName.getText().toString(),txtRegPass1.getText().toString());
                finish();
            }
        });
    }
}
