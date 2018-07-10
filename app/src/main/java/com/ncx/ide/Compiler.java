package com.ncx.ide;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Compiler {
    private String url = "https://api.jdoodle.com/v1/execute";
    private JSONObject body = new JSONObject();
    private EditText outputET;
    private Context context;

    Compiler(Context ct, EditText et, String script, String stdin) {
        context = ct;
        outputET = et;
        try {
            body.put("clientId", "paste client id here");
            body.put("clientSecret", "paste client secret here");
            body.put("script", script);
            body.put("stdin", stdin);
            body.put("language", "java");
            body.put("versionIndex", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void compile() {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, (response) -> {
            try {
                Toast.makeText(context, "File Compiled", Toast.LENGTH_SHORT).show();
                String output = response.getString("output");
                if (response.getInt("statusCode") == 200) {
                    if (output.equals("null")) {
                        SpannableString message  = new SpannableString("No output.");
                        message.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        outputET.setText(message);
                    } else {
                        outputET.setText(output);
                    }
                } else {
                    SpannableString msg = new SpannableString(output);
                    msg.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    outputET.setText("ERROR:\n");
                    outputET.append(msg);
                }
            } catch (JSONException e) {
                SpannableString msg = new SpannableString("Error retrieving output");
                msg.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                outputET.setText(msg);
            }
        }, (error) -> {
            String errorMessage = "";
            if (error instanceof NetworkError) {
                errorMessage = "Error: Please check your internet connection!";
            } else if (error instanceof ServerError) {
                errorMessage = "Error: Server returned an error.";
            } else if (error instanceof AuthFailureError) {
                errorMessage = "Error: Failed to Authenticate! Please check your authentication tokens.";
            } else if (error instanceof ParseError) {
                errorMessage = "Error: Parsing to JSON failed! Please check your request and response data.";
            } else if (error instanceof TimeoutError) {
                errorMessage = "Error: Timeout. Please make sure you have provided STDIN input if your code requires it or you have no infinite loops in your code";
            }
            SpannableString message = new SpannableString(errorMessage);
            message.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, errorMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            outputET.setText(message);
        });
        queue.add(request);
    }
}
