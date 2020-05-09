package com.example.courier.Driver.UserManagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.courier.R;

public class MainInterfaceActivity extends AppCompatActivity {

    Button signIn, signUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        signIn = (Button)findViewById(R.id.btn_dsignIn);
        signUp = (Button)findViewById(R.id.btn_dregister);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainInterfaceActivity.this,DriverRegister.class));
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainInterfaceActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}
