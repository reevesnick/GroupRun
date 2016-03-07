package app.com.grouprun.Fragments;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import app.com.grouprun.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CompletedRunDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class CompletedRunDialogFragment extends DialogFragment {


    private Button saveButton;
    private Button cancelButton;
    private EditText timeText;
    private EditText distanceText;
    private ParseUser currentUser;
    public interface CompletedRunDialogListener{

    }

    CompletedRunDialogListener completedRunDialogListener;


    public CompletedRunDialogFragment(){


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_completed_run_dialog, container);
        saveButton = (Button)view.findViewById(R.id.save_button);
        cancelButton = (Button)view.findViewById(R.id.cancel_button);
        timeText = (EditText) view.findViewById(R.id.time_text);
        distanceText = (EditText) view.findViewById(R.id.distance_text);
        String timeText = getArguments().getString("timeText");
        this.timeText.setText(timeText);
        String distanceText = getArguments().getString("distanceText");
        this.distanceText.setText(distanceText);
        onCancelButton();
        onSaveButton();


        return view;
    }
    public void onCancelButton(){
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    public void setDistanceText(String text){
        timeText.setText(text);
    }
    public void onSaveButton(){
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                currentUser = ParseUser.getCurrentUser();
                final ParseObject run = new ParseObject("Run");
                run.put("time", timeText.getText().toString());
                run.put("distance", distanceText.getText().toString());
                final ParseRelation userRun = currentUser.getRelation("listOfRuns");
                run.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            userRun.add(run);

                        }
                    }
                });

                currentUser.saveInBackground();
            }
        });
    }


    public void onStart(){
        super.onStart();
        currentUser = ParseUser.getCurrentUser();
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            completedRunDialogListener = (CompletedRunDialogListener) activity;
        }catch(ClassCastException cast){
            throw new ClassCastException(activity.toString()+" must implement CompletedRunDialogListener");
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }




}
