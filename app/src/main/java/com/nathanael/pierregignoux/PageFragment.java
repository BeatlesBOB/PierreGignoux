package com.nathanael.pierregignoux;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class PageFragment extends Fragment {

    // 1 - Create keys for our Bundle
    private static final String KEY_POSITION="position";
    private static final String KEY_COLOR="color";


    public PageFragment() { }


    // 2 - Method that will create a new instance of PageFragment, and add data to its bundle.
    public static PageFragment newInstance(int position, int color) {

        // 2.1 Create new fragment
        PageFragment frag = new PageFragment();

        // 2.2 Create bundle and add it some data
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        args.putInt(KEY_COLOR, color);
        frag.setArguments(args);

        return(frag);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // 3 - Get layout of PageFragment
        View result = inflater.inflate(R.layout.fragment_page, container, false);

        // 4 - Get widgets from layout and serialise it
        LinearLayout rootView= (LinearLayout) result.findViewById(R.id.fragment_page_rootview);
        TextView textView= (TextView) result.findViewById(R.id.fragment_page_title);
        ImageView imageView = result.findViewById(R.id.fragment_page_image);

        // 5 - Get data from Bundle (created in method newInstance)
        int position = getArguments().getInt(KEY_POSITION, -1);
        int color = getArguments().getInt(KEY_COLOR, -1);

        // 6 - Update widgets with it
//        rootView.setBackgroundColor(color);
//        textView.setText("Page numéro "+position);
        if (position == 0)
        {
            rootView.setBackgroundColor(color);
            textView.setText(getString(R.string.viewpage1));
           imageView.setImageResource(R.drawable.img_onboard_1);

        }else if(position == 1)
        {
            rootView.setBackgroundColor(color);
            textView.setText(getString(R.string.viewpage2));
            imageView.setImageResource(R.drawable.img_onboard_2);


        }else {
            rootView.setBackgroundColor(color);
            textView.setText(getString(R.string.viewpage3));
            imageView.setImageResource(R.drawable.img_onboard_3);


        }

        Log.e(getClass().getSimpleName(), "onCreateView called for fragment number "+position);

        return result;
    }

}