package com.example.pr21_rmp_gudochkina;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserActivity extends AppCompatActivity {

    EditText nameBox;
    EditText yearBox;
    Button delButton;
    Button saveButton;

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    long userId = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameBox = findViewById(R.id.name);
        yearBox = findViewById(R.id.year);
        delButton = findViewById(R.id.deleteButton);
        saveButton = findViewById(R.id.saveButton);

        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userId = extras.getLong("id");
        }

        if (userId > 0) {
            userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE + " where " +
                    DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
            if (userCursor.moveToFirst()) {
                nameBox.setText(userCursor.getString(1));
                yearBox.setText(String.valueOf(userCursor.getInt(2)));
            } else {
                Toast.makeText(this, "Запись не найдена", Toast.LENGTH_SHORT).show();
                finish();
            }
            userCursor.close();
            yearBox.setText(String.valueOf(userCursor.getInt(2)));
            userCursor.close();
        } else {
            delButton.setVisibility(View.GONE);
        }
    }

    public void save(View view) {
        String name = nameBox.getText().toString().trim();
        String yearStr = yearBox.getText().toString().trim();

        if (name.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Год должен быть числом!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (year < DatabaseHelper.MIN_YEAR || year > DatabaseHelper.MAX_YEAR) {
            Toast.makeText(this,
                    "Год должен быть между " + DatabaseHelper.MIN_YEAR
                            + " и " + DatabaseHelper.MAX_YEAR,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_NAME, name);
        cv.put(DatabaseHelper.COLUMN_YEAR, year);

        if (userId > 0) {
            db.update(DatabaseHelper.TABLE, cv, DatabaseHelper.COLUMN_ID + "=" + userId, null);
        } else {
            db.insert(DatabaseHelper.TABLE, null, cv);
        }
        goHome();
    }

    public void delete(View view) {
        db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(userId)});
        goHome();
    }

    private void goHome() {
        db.close();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userCursor != null && !userCursor.isClosed()) {
            userCursor.close();
        }
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}