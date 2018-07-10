package com.ncx.ide;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.stackoverflow.util.getFileName;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.editor)
    EditText editor;
    @BindView(R.id.fileView)
    TextView fileView;

    private Highlighter h = new Highlighter();

    private Uri uri;
    private String stdin;
    private String filename;
    private int READ_REQUEST_CODE = 1;
    private int WRITE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        editor.addTextChangedListener(new TextWatcher() {

            boolean editing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editing) {
                    editing = true;

                    String f = fileView.getText().toString();
                    if (f.charAt(f.length() - 1) != '*') fileView.append("*");

                    String txt = editor.getText().toString();
                    int cursorStart = editor.getSelectionStart();
                    int cursorEnd = editor.getSelectionEnd();

                    editor.getText().clear();
                    editor.append(h.highlight(txt));

                    editor.setSelection(cursorStart, cursorEnd);

                    editing = false;
                }
            }
        });
    }

    public void getStdin(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter stdin");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setSingleLine(false);
        input.setText(stdin);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> stdin = input.getText().toString());

        builder.show();
    }

    public void saveFile(View view) {
        if (uri == null) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, WRITE_REQUEST_CODE);
        } else {
            writeContent(uri);
            Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();
        }
    }

    public void openFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "fileName");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void runFile(View view) {
        Intent intent = new Intent(this, OutputActivity.class);
        String code = editor.getText().toString();
        intent.putExtra("code", code);
        intent.putExtra("stdin", stdin);
        startActivity(intent);
    }

    private void readContent(Intent data) {
        uri = data.getData();

        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
        }
        assert is != null;

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        StringBuilder content = new StringBuilder();

        try {
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error Reading File", Toast.LENGTH_SHORT).show();
        }

        editor.setText(h.highlight(content.toString()));

        filename = getFileName(this, uri);
        fileView.setText(filename);

        Toast.makeText(this, "File Opened", Toast.LENGTH_SHORT).show();

        try {
            br.close();
            is.close();
        } catch (IOException e) {}
    }

    private void writeContent(Uri uri) {
        OutputStream os = null;
        try {
            os = getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File Not Found", Toast.LENGTH_SHORT).show();
        }
        assert os != null;

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        try {
            bw.write(editor.getText().toString());
        } catch (IOException e) {
            Toast.makeText(this, "Unable to write file", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "File Saved", Toast.LENGTH_SHORT).show();

        filename = getFileName(this, uri);
        fileView.setText(filename);

        try {
            bw.flush();
            bw.close();
            os.close();
        } catch (IOException e) {}
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == READ_REQUEST_CODE && result == RESULT_OK && data != null) {
            readContent(data);
        } else if (request == WRITE_REQUEST_CODE && result == RESULT_OK && data != null) {
            uri = data.getData();
            writeContent(uri);
        }
    }
}
