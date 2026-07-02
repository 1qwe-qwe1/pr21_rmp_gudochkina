package com.example.pr21_rmp_gudochkina;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity {

    EditText nameInput, yearInput;
    Button addButton, viewButton;
    ListView userList;

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameInput = findViewById(R.id.nameInput);
        yearInput = findViewById(R.id.yearInput);
        addButton = findViewById(R.id.addButton);
        userList = findViewById(R.id.userList);

        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();

        loadUsers();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String yearStr = yearInput.getText().toString().trim();

                if (name.isEmpty() || yearStr.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                            "Заполните все поля!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int year;
                try {
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this,
                            "Год должен быть числом!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (year < DatabaseHelper.MIN_YEAR || year > DatabaseHelper.MAX_YEAR) {
                    Toast.makeText(MainActivity.this,
                            "Год должен быть между " + DatabaseHelper.MIN_YEAR
                                    + " и " + DatabaseHelper.MAX_YEAR,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_NAME, name);
                cv.put(DatabaseHelper.COLUMN_YEAR, year);

                long result = db.insert(DatabaseHelper.TABLE, null, cv);
                if (result > 0) {
                    Toast.makeText(MainActivity.this,
                            "Запись добавлена", Toast.LENGTH_SHORT).show();
                    nameInput.setText("");
                    yearInput.setText("");
                    loadUsers();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Ошибка добавления", Toast.LENGTH_SHORT).show();
                }
            }
        });

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
    }

    private void loadUsers() {
        Cursor newCursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE, null);

        if (userAdapter == null) {
            String[] headers = new String[]{DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_YEAR};
            int[] to = new int[]{android.R.id.text1, android.R.id.text2};
            userAdapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_2, newCursor, headers, to, 0);
            userList.setAdapter(userAdapter);
        } else {
            userAdapter.changeCursor(newCursor);
        }
        userCursor = newCursor;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userCursor != null && !userCursor.isClosed()) {
            userCursor.close();
        }
        db.close();
    }
}