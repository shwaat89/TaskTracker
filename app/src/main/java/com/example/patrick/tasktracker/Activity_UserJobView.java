package com.example.patrick.tasktracker;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseRelation;
import com.parse.SaveCallback;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Shwaat on 11/3/2014.
 */
public class Activity_UserJobView extends Activity {
    //variables for the xml elements.
    TextView depValue;
    TextView locValue;
    TextView descValue;
    TextView dateValue;
    TextView matsValue;
    //variables for time keeping.
    Long start;
    Long stop;
    Long elapsed;
    Long paused;
    Long resumed;
    Boolean startPressed = false;
    Boolean pausePressed = false;
    Boolean resumePressed = false;
    //variables for querying the parse database
    String workOrderId;
    String userId;
    ParseQuery<ParseObject> query;
    ParseObject po;
    ParseObject relationObject;

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Parse.initialize(this, "6yEsCcvYy5ym7rmRKWleVy5A9jc2wHFz6aEL3Czs", "t3h3S0090VVBwdw0zasj5J0b28dLe9xebL5nIfKw");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_job_view);
        Intent intent = getIntent();
        //assigns data that was passed from previous activity.
        workOrderId = intent.getStringExtra("workOrderId");
        userId = intent.getStringExtra("userId");

        //sets variables to their respective xml elements.
        depValue = (TextView)findViewById(R.id.user_view_department_value);
        locValue = (TextView)findViewById(R.id.user_view_location_value);
        descValue = (TextView)findViewById(R.id.user_view_workDesc_value);
        dateValue = (TextView)findViewById(R.id.user_view_schedule_value);
        matsValue = (TextView)findViewById(R.id.user_view_workMats_value);

        //gets all work WorkOrder_Employee objects and includes the tables from the FK pointers "workorder" and "employee" columns.
        ParseQuery<ParseObject> wo_emp = ParseQuery.getQuery("WorkOrder_Employee");
        wo_emp.include("workorder");
        wo_emp.include("employee");
        wo_emp.getInBackground(workOrderId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null){
                    relationObject = parseObject;
                    //gets the WorkOrder object related to the single WorkOrder_Employee object in order to populate xml elements with data.
                    query = ParseQuery.getQuery("WorkOrder");
                    query.include("department");
                    query.include("location");
                    query.getInBackground(parseObject.getParseObject("workorder").getObjectId(), new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                po = parseObject;
                                ParseObject dep = po.getParseObject("department");
                                ParseObject loc = po.getParseObject("location");
                                depValue.setText(dep.getString("Department_id"));
                                locValue.setText(loc.getString("Location_id"));

                                descValue.setText(po.getString("description"));
                                dateValue.setText(po.getString("scheduleDate"));
                                matsValue.setText(po.getString("materials"));
                            } else {
                                Log.d("UserJobView", "Workorder " + e.toString());
                            }
                        }
                    });
                }else{
                    Log.d("UserJobView","Wo_emp " + e.toString());
                }
            }
        });
    }

    //Start button pressed on the device.
    public void Start(View view){
        if(!startPressed) {
            start = System.currentTimeMillis();
            startPressed = true;
            relationObject.put("Start_time", sdf.format(start));
            //put this time started into the relational table of employee -- workorder (many to many)
            relationObject.saveInBackground();
        }
        //start has already been pressed. do nothing.
    }

    //Stop button pressed on the device.
    public void Stop(View view){
        if(startPressed ) {
            stop = System.currentTimeMillis();

            //resumePressed only true after pause was pressed.
            if(resumePressed) {
                elapsed += stop - resumed;
            }else if(pausePressed) {
                //the timer was not resumed. keeps time elapsed from time that pause was pressed.
                final int hours = (int) (elapsed / 3600000);
                final int minutes = (int) (elapsed - hours * 3600000) / 60000;
                final int seconds = (int) (elapsed - hours * 3600000 - minutes * 60000) / 1000;
                relationObject.put("Elapsed_time", elapsed);
                relationObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("UserJobView", "Elapsed time = " + hours+":"+minutes+":"+seconds);
                        finish();
                    }
                });
            }else{
                //if neither pause nor resume was pressed, then only use start and stop time for elapsed time.
                elapsed = stop - start;
                final int hours = (int) (elapsed / 3600000);
                final int minutes = (int) (elapsed - hours * 3600000) / 60000;
                final int seconds = (int) (elapsed - hours * 3600000 - minutes * 60000) / 1000;

                //put this time elapsed and time stopped into the relational table of employee -- workorder (many to many)
                relationObject.put("Stop_time", sdf.format(stop));
                relationObject.put("Elapsed_time", ""+hours+":"+minutes+":"+seconds);
                relationObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("UserJobView", "Elapsed time = " + hours+":"+minutes+":"+seconds);
                        finish();
                    }
                });
            }

        }//else do nothing.
    }
    //Pause button pressed on the device.
    public void Pause(View view){
        if(startPressed){
            paused = System.currentTimeMillis();
            //the current elapsed time will be from start timestamp to when pause was pressed.
            elapsed = paused - start;
            pausePressed = true;
        }//else do nothing.
    }
    //Resume button pressed on the device.
    public void Resume(View view){
        if(pausePressed){
            pausePressed = false;
            resumed = System.currentTimeMillis();
        }//else do nothing.
    }
}
