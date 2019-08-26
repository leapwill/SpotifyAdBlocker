package com.cameron.spotifyadblocker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements ViewAdditionalFiltersDialogFragment.ViewAdditionalFiltersDialogListener {
    private boolean enabled;
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        serviceIntent = new Intent(this, ReceiverForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(getString(R.string.service_notification_channel), getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.channel_description));
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume()
    {
        // FIXME how to handle STOP action
        super.onResume();
        restoreCheckboxState();
    }

    private void restoreCheckboxState()
    {
        enabled = false;
        SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_enabled), MODE_PRIVATE);
        enabled = preferences.getBoolean(getString(R.string.saved_enabled), enabled);
        CheckBox enabledCheckbox = (CheckBox) findViewById(R.id.checkBox);
        enabledCheckbox.setChecked(enabled);
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    public void onCheckboxClick(View view) {
        if (enabled) {
            Log.d("DEBUG", "Stopping Service");
            stopService(serviceIntent);
            enabled = false;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            enabled = true;
        }
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.saved_enabled), MODE_PRIVATE).edit();
        preferencesEditor.putBoolean(getString(R.string.saved_enabled), enabled);
        preferencesEditor.apply();
    }

    public void notificationAccess(View view) {
        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
    }

    public void addAdditionalFilter(View view) {
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE).edit();
        EditText et = (EditText)view.getRootView().findViewById(R.id.editTextAddFilter);
        String newFilter = et.getText().toString();
        if (!newFilter.equals("")) {
            et.setText("");
            preferencesEditor.putString("filter_" + newFilter, newFilter);
            preferencesEditor.apply();
            Toast.makeText(this, "Added filter: " + newFilter, Toast.LENGTH_SHORT).show();
        }
        //toggle service
        this.onCheckboxClick(findViewById(R.id.checkBox));
        this.onCheckboxClick(findViewById(R.id.checkBox));
    }

    public void addCurrent(View view) {
        EditText et = findViewById(R.id.editTextAddFilter);
        et.setText(Util.getInstance(this).getCurrentTitle());
        this.addAdditionalFilter(findViewById(R.id.buttonAddFilter));
    }

    public void openAdditionalFilterListDialog(View view) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE);
        Collection<? extends String> additionalFilters = (Collection<String>) preferences.getAll().values();
        ViewAdditionalFiltersDialogFragment viewAdditionalFiltersDialogFragment = ViewAdditionalFiltersDialogFragment.newInstance(additionalFilters.toArray(new String[additionalFilters.size()]));
        viewAdditionalFiltersDialogFragment.show(getFragmentManager(), "additionalFiltersDialog");
    }

    @Override
    public void onFilterClick(DialogInterface dialogInterface, int i) {
        SharedPreferences.Editor preferencesEditor = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE).edit();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.saved_filters), MODE_PRIVATE);
        Collection<String> additionalFilters = (Collection<String>) preferences.getAll().values();
        String filterToRemove = additionalFilters.toArray(new String[additionalFilters.size()])[i];
        preferencesEditor.remove("filter_" + filterToRemove);
        Toast.makeText(this, "Deleted filter: " + filterToRemove, Toast.LENGTH_SHORT).show();
        preferencesEditor.apply();
    }
}
