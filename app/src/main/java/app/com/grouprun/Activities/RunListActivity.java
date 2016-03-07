package app.com.grouprun.Activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.text.ParseException;

import app.com.grouprun.R;

public class RunListActivity extends AppCompatActivity {
    ParseObject runObject;
    ListView view;
    CustomAdapter adpater;
    ParseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_list);
        currentUser = ParseUser.getCurrentUser();
        view = (ListView) findViewById(R.id.listView);
        runObject = new ParseObject("Run");
        adpater = new CustomAdapter(getApplicationContext());
        view.setAdapter(adpater);

    }

    class CustomAdapter extends ParseQueryAdapter
    {
        public CustomAdapter(Context context){
            super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
                @Override
                public ParseQuery<ParseObject> create() {
                    ParseQuery runQuery= null;
                    try {
                        runQuery = currentUser.getRelation("listOfRuns").getQuery();

                    }catch (Exception ex){
                        System.out.println(ex);
                    }
                    return runQuery;
                }
            });
        }

        public View getItemView(ParseObject object, View v, ViewGroup parent){
            if(v==null){
                v= View.inflate(getContext(),R.layout.run_item,null);
            }
            super.getItemView(object,v,parent);

            //add the run's time
            TextView runTimeTextView =  (TextView)v.findViewById(R.id.time);
            runTimeTextView.setText(object.getString("time"));

            //add the run's distance
            TextView runDistanceTextView =  (TextView)v.findViewById(R.id.distance);
            runDistanceTextView.setText(object.getString("distance"));
            System.out.println("Distance: "+object.getString("distance"));
            return v;
        }
    }
}
