package com.example.patrick.tasktracker;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;

/**
 * Created by Patrick on 11/16/2014.
 */
public class QueryAdapterEmpRemove extends ParseQueryAdapter<ParseObject> {
    ArrayList<ParseObject> listToRemove;
    public QueryAdapterEmpRemove(Context context, QueryFactory<ParseObject> factory,ArrayList<ParseObject> listToRemove) {
        super(context, factory);
        this.listToRemove = listToRemove;
    }

    @Override
    public View getItemView(final ParseObject parseobject, View v, ViewGroup parent) {
        ViewHolder holder = null;
        if (v == null) {
            //this is the layout file that contains a Relative layout containing a TextView and Checkbox element.
            v = View.inflate(getContext(), R.layout.admin_emp_remove_listitem, null);
            holder = new ViewHolder();
            holder.name = (TextView)v.findViewById(R.id.admin_rem_emp_name);
            holder.name.setText(parseobject.get("First_name").toString() + " " + parseobject.get("Last_name").toString());
            holder.checkbox = (CheckBox)v.findViewById(R.id.admin_rem_emp_checkbox);
            final CheckBox checkbox = holder.checkbox;
            v.setTag(holder);

            //listener to run when user presses on checkbox.
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isChecked = checkbox.isChecked();
                    if(isChecked) {
                        Log.d("EmpRemove", "Added to list: " + parseobject.getString("First_name") + " " + parseobject.getString("Last_name"));

                        listToRemove.add(parseobject);
                        checkbox.setChecked(true);
                    }else{
                        Log.d("EmpRemove", "Removed from list: " + parseobject.getString("First_name") + " " + parseobject.getString("Last_name"));
                        listToRemove.remove(parseobject);
                        checkbox.setChecked(false);
                    }
                }
            });
        }else{
            holder = (ViewHolder)v.getTag();
        }
        return v;
    }




    private class ViewHolder{
        TextView name;
        CheckBox checkbox;
    }

}
