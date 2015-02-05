package com.elephant.proga.dumbo.receivers;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by gluse on 03/10/14.
 */
abstract class Receiver implements Runnable {

    protected String source;
    protected String content;
    protected JSONObject jcontent;
    protected long sleepingTime;

    public Receiver(String source, long sleepingTime) {

        this.source = source;
        this.sleepingTime = sleepingTime;
    }


    private String responseContentToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    protected String GET() {
        HttpResponse response;
        HttpGet req;
        HttpClient client;

        client = new DefaultHttpClient();
        req = new HttpGet(this.source);


        try {
            response = client.execute(req);
            return responseContentToString(response.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Override this method to specialize your conversion
    protected JSONObject toJSON(String string) {

        try {
            //Log.d("RECEIVER",string);
            return new JSONObject(string);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected JSONArray toJSONArray(String string) {

        try {
            //Log.d("RECEIVER",string);
            return new JSONArray(string);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



}
