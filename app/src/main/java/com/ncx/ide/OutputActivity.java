package com.ncx.ide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OutputActivity extends AppCompatActivity {

    @BindView(R.id.output) EditText output;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        ButterKnife.bind(this);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Output");

        Intent intent = getIntent();
        String code = intent.getStringExtra("code");
        String stdin = intent.getStringExtra("stdin");


        new Compiler(getApplicationContext(), output, code, stdin).compile();
    }
}
