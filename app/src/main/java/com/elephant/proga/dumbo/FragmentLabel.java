package com.elephant.proga.dumbo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.elephant.proga.dumbo.interfaces.LabelInterface;

/**
 * Created by gluse on 03/12/14.
 */
public class FragmentLabel extends Fragment implements View.OnClickListener {

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
    private LabelInterface mLabelInterface;

    public FragmentLabel() {
        super();
    }

    public void hideLabel() {
        Log.d("LABEL","GOING INVISIBLE");
        this.labelView.setVisibility(View.INVISIBLE);

    }

    public void showLabel() {
        Log.d("LABEL","GOING VISIBLE");
        this.labelView.setVisibility(View.VISIBLE);

    }


    public void setFlightID(String flightid) {
        this.flightIDTextView.setText(flightid);
    }

    public void setPosition(float x, float y) {

    }


    public void minimize() {

    }

    public void maximize() {

    }

    public void setLabelInterface(LabelInterface labelInterface) {
        mLabelInterface = labelInterface;
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
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.label_layout,container,false);
        this.callToPredict = (TextView) v.findViewById(R.id.call_to_predict);
        this.callToPredict.setOnClickListener(this);
        this.altitudeTextView = (TextView) v.findViewById(R.id.altitude);
        this.altitudeTextView.setOnClickListener(this);
        this.flightIDTextView = (TextView) v.findViewById(R.id.flight_id);
        this.flightIDTextView.setOnClickListener(this);
        this.predictionCommands = v.findViewById(R.id.prediction_buttons_layout);
        v.setOnClickListener(this);
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
            if (source == pred2.getId()) this.mLabelInterface.predictionSelected(2);
            if (source == pred3.getId()) this.mLabelInterface.predictionSelected(3);
            if (source == pred4.getId()) this.mLabelInterface.predictionSelected(4);
            if (source == pred5.getId()) this.mLabelInterface.predictionSelected(5);
        }




    }
}
