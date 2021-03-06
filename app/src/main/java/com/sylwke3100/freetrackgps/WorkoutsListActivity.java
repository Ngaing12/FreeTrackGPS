package com.sylwke3100.freetrackgps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkoutsListActivity extends Activity {
    private SimpleAdapter simpleAdapter;
    private WorkoutsListManager workoutsListManager;
    private ListView workoutList;
    private Menu optionsMenu;
    private ArrayList<HashMap<String, String>> routesList;
    public static final int EXPORT_DIALOG = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workouts_list);
        routesList = new ArrayList<HashMap<String, String>>();
        workoutList = (ListView) this.findViewById(R.id.listWorkout);
        registerForContextMenu(workoutList);
        workoutsListManager = new WorkoutsListManager(getBaseContext());
        onUpdateWorkoutsList();

        workoutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            List<RouteListElement> objects = workoutsListManager.getUpdatedWorkoutsRawList();

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(WorkoutsListActivity.this, WorkoutInfoActivity.class);
                intent.putExtra("routeId", objects.get(i).id);
                startActivity(intent);
            }
        });
    }

    private void onUpdateWorkoutsList() {
        routesList = workoutsListManager.getUpdatedWorkoutsList();
        simpleAdapter = new SimpleAdapter(this, routesList, R.layout.textview_row_lines,
                new String[]{"time", "distance"}, new int[]{R.id.line_time, R.id.line_distance});
        workoutList.setAdapter(simpleAdapter);
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_workoutspreview, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_workout_delete:
                onDeleteWorkoutAlert(info.position);
                return true;
            case R.id.action_workout_export:
                new ExportTask(this, workoutsListManager).execute(info.position);
                return true;
            case R.id.action_workout_change:
                onUpdateNameWorkout(info.position);
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onDeleteWorkoutAlert(final int id) {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.deleteWorkoutTitleAlert))
                .setMessage(getString(R.string.deleteWorkoutTextAlert))
                .setPositiveButton(this.getString(R.string.yesLabel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                workoutsListManager.deleteWorkout(id);
                                onUpdateWorkoutsList();
                            }
                        }).setNegativeButton(this.getString(R.string.noLabel), null).show();

    }

    public void updateIconOptionMenu() {
        Integer dateFilterIcon, nameFilterIcon;
        if (workoutsListManager.getStatusTimeFilter())
            dateFilterIcon = R.drawable.tick;
        else
            dateFilterIcon = R.drawable.emptytick;
        if (workoutsListManager.getStatusNameFilter())
            nameFilterIcon = R.drawable.tick;
        else
            nameFilterIcon = R.drawable.emptytick;
        optionsMenu.findItem(R.id.action_overflow).getSubMenu().findItem(R.id.action_filter_by_date).setIcon(dateFilterIcon);
        optionsMenu.findItem(R.id.action_overflow).getSubMenu().findItem(R.id.action_filter_by_name).setIcon(nameFilterIcon);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_workoutpreview_filters, menu);
        this.optionsMenu = menu;
        updateIconOptionMenu();
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        updateIconOptionMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        updateIconOptionMenu();
        switch (item.getItemId()) {
            case R.id.action_filter_by_date:
                Intent intent = new Intent(this, DateFilterActivity.class);
                startActivity(intent);
                break;
            case R.id.action_filter_by_name:
                onUpdateNameFilter();
                break;
            case R.id.action_filters_reset:
                workoutsListManager.clearAllFilters();
                onUpdateWorkoutsList();
                break;
        }
        return true;
    }

    public void onUpdateNameFilter() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompt_workout_name_filer, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.nameFilter);
        input.setText(workoutsListManager.getFilterName());
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.okLabel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        workoutsListManager.setNameFilter(input.getText().toString());
                        onUpdateWorkoutsList();
                    }
                }).setNegativeButton(R.string.cancelLabel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }

    public void onUpdateNameWorkout(final int idWorkout) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompt_workout_name_edit, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final EditText input = (EditText) promptView.findViewById(R.id.nameWorkout);
        input.setText(workoutsListManager.getWorkoutName(idWorkout));
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.okLabel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        workoutsListManager.updateWorkoutName(idWorkout, input.getText().toString());
                        onUpdateWorkoutsList();
                    }
                }).setNegativeButton(R.string.cancelLabel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }

    public void onResume() {
        super.onResume();
        onUpdateWorkoutsList();
    }

    public Dialog onCreateDialog(int dialogId) {

        switch (dialogId) {
            case EXPORT_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle(getString(R.string.exportingTitleLabel));
                dialog.setMessage(getString(R.string.exportingMessageLabel));
                dialog.setCancelable(false);
                return dialog;
            default:
                break;
        }

        return null;
    }
}
