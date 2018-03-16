package uk.ac.abertay.forbes.assessment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class OpenScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        try {
            setContentView(R.layout.activity_open_screen);
            wait(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        setContentView(R.layout.activity_main_menu);
    }

    public void make(View view) {
        Intent intent = new Intent(this, RecordOptions.class);
        startActivity(intent);
    }

    public void read(View view) {
        Intent intent = new Intent(this, ReadOptions.class);
        startActivity(intent);
    }
}
