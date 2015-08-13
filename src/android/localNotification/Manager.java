package org.apache.cordova.dialogs.localNotification;

import android.app.NotificationManager;
import android.content.Context;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Manager {

    // Context passed through constructor and used for notification builder.
    private Context context;

    /**
     * Constructor
     *
     * @param context
     *      Application context
     */
    private Manager(Context context){
        this.context = context;
    }

    /**
     * Static method to retrieve class instance.
     *
     * @param context
     *      Application context
     */
    public static Manager getInstance(Context context) {
        return new Manager(context);
    }


    /**
     * Clear local notification specified by ID.
     *
     * @param id
     *      The notification ID
     */
    public LocalNotification cancel (int id) {
        LocalNotification notification = get(id);

        if (notification != null) {
            notification.cancel();
        }

        return notification;
    }



    /**
     * All local notifications IDs.
     */
    public List<Integer> getIds() {
        Set<String> keys = getPrefs().getAll().keySet();
        ArrayList<Integer> ids = new ArrayList<Integer>();

        for (String key : keys) {
            ids.add(Integer.parseInt(key));
        }

        return ids;
    }


    /**
     * List of local notifications with matching ID.
     *
     * @param ids
     *      Set of notification IDs
     */
    public List<LocalNotification> getByIds(List<Integer> ids) {
        ArrayList<LocalNotification> notifications = new ArrayList<LocalNotification>();

        for (int id : ids) {
            LocalNotification notification = get(id);

            if (notification != null) {
                notifications.add(notification);
            }
        }

        return notifications;
    }

    /**
     * List of all local notification.
     */
    public List<LocalNotification> getAll() {
        return getByIds(getIds());
    }


    /**
     * If a notification with an ID exists.
     *
     * @param id
     *      Notification ID
     */
    public boolean exist (int id) {
        return get(id) != null;
    }


    /**
     * List of properties from local notifications with matching ID.
     *
     * @param ids
     *      Set of notification IDs
     */
    public List<JSONObject> getOptionsById(List<Integer> ids) {
        ArrayList<JSONObject> options = new ArrayList<JSONObject>();

        for (int id : ids) {
            LocalNotification notification = get(id);

            if (notification != null) {
                options.add(notification.getOptions().getDict());
            }
        }

        return options;
    }




    /**
     * Get existent local notification.
     *
     * @param id
     *      Notification ID
     */
    public LocalNotification get(int id) {
        Map<String, ?> alarms = getPrefs().getAll();
        String notId          = Integer.toString(id);
        JSONObject options;

        if (!alarms.containsKey(notId))
            return null;


        try {
            String json = alarms.get(notId).toString();
            options = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        Builder builder = new Builder(context, options);

        return builder.build();
    }

    /**
     * Merge two JSON objects.
     *
     * @param obj1
     *      JSON object
     * @param obj2
     *      JSON object with new options
     */
    private JSONObject mergeJSONObjects (JSONObject obj1, JSONObject obj2) {
        Iterator it = obj2.keys();

        while (it.hasNext()) {
            try {
                String key = (String)it.next();

                obj1.put(key, obj2.opt(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return obj1;
    }


    /**
     * Notification manager for the application.
     */
    private NotificationManager getNotMgr () {
        return (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
