package com.elephant.proga.dumbo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.elephant.proga.dumbo.interfaces.Label;
import com.elephant.proga.dumbo.interfaces.LabelUser;

import static com.elephant.proga.dumbo.R.color.accent;
import static com.elephant.proga.dumbo.R.color.primary;
import static com.elephant.proga.dumbo.R.color.primary_dark;
import static com.elephant.proga.dumbo.R.color.primary_transparent;

/**
 * Created by gluse on 03/12/14.
 */
public class FragmentLabel extends Fragment implements View.OnClickListener,Label, View.OnTouchListener {

    private TextView callToPredict = null;
    private TextView flightIDTextView = null;
    private TextView altitudeTextView = null;
    private View predictionCommands = null;

    private Button pred2 = null;
    private Button pred3 = null;
    private Button pred4 = null;
    private Button pred5 = null;

    private View labelView = null;
    private boolean minimized = false;
    private LabelUser mLabelUser;

    public FragmentLabel() {
        super();
    }

    @Override
    public void hideLabel() {
        Log.d("LABEL","GOING INVISIBLE");
        this.labelView.setVisibility(View.INVISIBLE);

    }

    @Override
    public void showLabel() {
        Log.d("LABEL","GOING VISIBLE");
        this.labelView.setVisibility(View.VISIBLE);

    }


    public void setFlightID(String flightid) {
        this.flightIDTextView.setText(flightid);
    }

    public void setPosition(float x, float y) {
        this.getView().setTranslationX(x);
        this.getView().setTranslationY(y);

    }


    public void minimize() {

    }

    public void maximize() {

    }

    public void setLabelUser(LabelUser labelUser) {
        mLabelUser = labelUser;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.minimized = true;
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.label_layout,container,false);
        this.callToPredict = (TextView) v.findViewById(R.id.call_to_predict);
        this.callToPredict.setOnClickListener(this);
        this.callToPredict.setOnTouchListener(this);
        this.altitudeTextView = (TextView) v.findViewById(R.id.altitude);
        this.altitudeTextView.setOnClickListener(this);
        this.altitudeTextView.setOnTouchListener(this);
        this.flightIDTextView = (TextView) v.findViewById(R.id.flight_id);
        this.flightIDTextView.setOnClickListener(this);
        this.flightIDTextView.setOnTouchListener(this);
        this.predictionCommands = v.findViewById(R.id.prediction_buttons_layout);
        v.setOnClickListener(this);
        v.setOnTouchListener(this);
        this.labelView = v;

        //prediction buttons
        this.pred2 = (Button) v.findViewById(R.id.pred_button2);
        this.pred3 = (Button) v.findViewById(R.id.pred_button3);
        this.pred4 = (Button) v.findViewById(R.id.pred_button4);
        this.pred5 = (Button) v.findViewById(R.id.pred_button5);
        this.pred2.setOnClickListener(this);
        this.pred3.setOnClickListener(this);
        this.pred4.setOnClickListener(this);
        this.pred5.setOnClickListener(this);
        this.labelView.setVisibility(View.INVISIBLE);




        Log.d("LABEL","check v");
        return v;

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int source = v.getId();
        int action = event.getAction();

        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                //if (source == callToPredict.getId() || source == labelView.getId() || source == altitudeTextView.getId() || source == flightIDTextView.getId()) {
                    labelView.setBackgroundColor(getResources().getColor(primary_dark));
                //}
                //else
                //{
                //    View sourceView = labelView.findViewById(source);
                //    sourceView.setBackgroundColor(accent);
                //}
                return true;
            }

            case MotionEvent.ACTION_UP: {
                if (source == callToPredict.getId() || source == labelView.getId() || source == altitudeTextView.getId() || source == flightIDTextView.getId()) {
                    labelView.setBackgroundColor(getResources().getColor(primary_transparent));
                }
                //this will call the registered clicklistener
                this.labelView.performClick();
                return false;
            }

            default: return false;

        }



/*
        if (source == callToPredict.getId() || source == labelView.getId() || source == altitudeTextView.getId() || source == flightIDTextView.getId()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                Log.d("TOUCH","DOWN");
                labelView.setBackgroundColor(getResources().getColor(primary_dark));
                return true;
            }
            else {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Log.d("TOUCH","UP OR CANCEL");
                    labelView.setBackgroundColor(getResources().getColor(primary_transparent));
                    return false;
                }
            }
        }
        return true;
*/
    }


    @Override
    public void onClick(View v) {
            //open/close prediction commands
        int source = v.getId();

        if (source == callToPredict.getId() || source == labelView.getId() || source == altitudeTextView.getId() || source == flightIDTextView.getId()) {
            if (minimized) {
                this.callToPredict.setText("Touch to predict");
                this.predictionCommands.setVisibility(View.VISIBLE);
            } else {
                this.predictionCommands.setVisibility(View.GONE);
                //this.callToPredict.setText("Touch to minimize");
            }

            minimized = !minimized;
        }
        else {
            if (source == pred2.getId())
            {
                Log.d("LABEL","PREDICTION 2 MINUTES");
                this.mLabelUser.predictionSelected(2,this.flightIDTextView.getText().toString());
            }
            if (source == pred3.getId()) this.mLabelUser.predictionSelected(3,this.flightIDTextView.getText().toString());
            if (source == pred4.getId()) this.mLabelUser.predictionSelected(4,this.flightIDTextView.getText().toString());
            if (source == pred5.getId()) this.mLabelUser.predictionSelected(5,this.flightIDTextView.getText().toString());
        }




    }
}
