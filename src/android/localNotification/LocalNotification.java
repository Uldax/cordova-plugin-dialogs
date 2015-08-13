/*
        * Copyright (c) 2013-2015 by appPlant UG. All rights reserved.
        *
        * @APPPLANT_LICENSE_HEADER_START@
*
        * This file contains Original Code and/or Modifications of Original Code
        * as defined in and that are subject to the Apache License
        * Version 2.0 (the 'License'). You may not use this file except in
        * compliance with the License. Please obtain a copy of the License at
        * http://opensource.org/licenses/Apache-2.0/ and read it before using this
        * file.
        *
        * The Original Code and all software distributed under the License are
        * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
        * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
        * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
        * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
        * Please see the License for the specific language governing rights and
        * limitations under the License.
        *
        * @APPPLANT_LICENSE_HEADER_END@
*/
package org.apache.cordova.dialogs.localNotification;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Wrapper class around OS notification class. Handles basic operations
 * like show, delete, cancel for a single local notification instance.
 */
public class LocalNotification {

      // Key for private preferences
    static final String PREF_KEY = "LocalNotification";

    // Application context passed by constructor
    private final Context context;

    // Notification options passed by JS
    private final NotificationOption options;

    // Builder with full configuration
    private final NotificationCompat.Builder builder;



    /**
     * Constructor
     *
     * @param context
     *      Application context
     * @param options
     *      Parsed notification options
     * @param builder
     *      Pre-configured notification builder
     */
    protected LocalNotification (Context context, NotificationOption options,
                            NotificationCompat.Builder builder, Class<?> receiver) {

        this.context = context;
        this.options = options;
        this.builder = builder;
    }

    /**
     * Get application context.
     */
    public Context getContext () {
        return context;
    }

    /**
     * Get notification options.
     */
    public NotificationOption getOptions () {
        return options;
    }

    /**
     * Get notification ID.
     */
    public int getId () {
        return options.getId();
    }


    /**
     * If the notification is an update.
     */
    protected boolean isUpdate () {

        if (!options.getDict().has("updatedAt"))
            return false;

        long now = new Date().getTime();

        long updatedAt = options.getDict().optLong("updatedAt", now);

        return (now - updatedAt) < 1000;
    }


    /**
     * Clear the local notification without canceling repeating alarms.
     *
     */
    public void clear () {
            getNotMgr().cancel(getId());
    }

      /**
     * Present the local notification to user.
     */
    public void show () {
        int id = getOptions().getId();
        if (Build.VERSION.SDK_INT <= 15) {
            // Notification for HoneyComb to ICS
            getNotMgr().notify(id, builder.getNotification());
        } else {
            // Notification for Jellybean and above
            getNotMgr().notify(id, builder.build());
        }
    }


    //Good to keep
    /**
     * Persist the information of this notification to the Android Shared
     * Preferences. This will allow the application to restore the notification
     * upon device reboot, app restart, retrieve notifications, aso.
     */
    private void persist () {
        SharedPreferences.Editor editor = getPrefs().edit();

        editor.putString(options.getIdStr(), options.toString());

        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * Remove the notification from the Android shared Preferences.
     */
    private void unpersist () {
        SharedPreferences.Editor editor = getPrefs().edit();

        editor.remove(options.getIdStr());

        if (Build.VERSION.SDK_INT < 9) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * Shared private preferences for the application.
     */
    private SharedPreferences getPrefs () {
        return context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Notification manager for the application.
     */
    private NotificationManager getNotMgr () {
        return (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Set default receiver to handle the trigger event.
     *
     * @param receiver
     *      broadcast receiver
     */
    public static void setDefaultTriggerReceiver (Class<?> receiver) {
        //defaultReceiver = receiver;
    }
}