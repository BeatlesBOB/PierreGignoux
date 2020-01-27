package com.example.pierregignoux;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabLayout;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class MainActivity extends AppCompatActivity {


    private  DotsIndicator dotsIndicator;
    private Button continuer;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //3 - Configure ViewPager
            this.configureViewPager();
            continuer = findViewById(R.id.continuebutton);

            continuer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                    startActivity(intent);
                }
            });

        }

        private void configureViewPager(){
            // 1 - Get ViewPager from layout
            ViewPager pager = (ViewPager)findViewById(R.id.pager);
            // 2 - Set Adapter PageAdapter and glue it together
            pager.setAdapter(new PageAdapter(getSupportFragmentManager(), getResources().getIntArray(R.array.colorPagesViewPager)) {
            });

            dotsIndicator = (DotsIndicator) findViewById(R.id.dots_indicator);
            dotsIndicator.setViewPager(pager);
        }



}

