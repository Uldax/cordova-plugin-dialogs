package org.apache.cordova.dialogs.localNotification;

import android.app.AlarmManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Wrapper around the JSON object passed through JS which contains all
 * possible option values. Class provides simple readers and more advanced
 * methods to convert independent values into platform specific values.
 */
public class NotificationOption {


    // Key name for bundled extras
    static final String EXTRA = "NOTIFICATION_OPTIONS";

    // The original JSON object
    private JSONObject options = new JSONObject();

    // Application context
    private final Context context;

    // Asset util instance
    private final AssetUtil assets;


    /**
     * Constructor
     *
     * @param context
     *      Application context
     */
    public NotificationOption(Context context){
        this.context = context;
        this.assets  = AssetUtil.getInstance(context);
    }

    /**
     * Parse given JSON properties.
     *
     * @param options
     *      JSON properties
     */
    public NotificationOption parse (JSONObject options) {
        this.options = options;


        /**
         * Parse asset URIs.
         */
        if (! options.has("iconUri")) {
            Uri iconUri = assets.parse(options.optString("icon", "icon"));
            Uri soundUri = assets.parseSound(options.optString("sound", null));

            try {
                options.put("iconUri", iconUri.toString());
                options.put("soundUri", soundUri.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return this;
    }




    /**
     * Application context.
     */
    public Context getContext () {
        return context;
    }

    /**
     * Wrapped JSON object.
     */
    JSONObject getDict () {
        return options;
    }

    /**
     * Text for the local notification.
     */
    public String getText() {
        return options.optString("text", "");
    }


    /**
     * Badge number for the local notification.
     */
    public int getBadgeNumber() {
        return options.optInt("badge", 0);
    }

    /**
     * ongoing flag for local notifications.
     */
    public Boolean isOngoing() {
        return options.optBoolean("ongoing", false);
    }

    /**
     * autoClear flag for local notifications.
     */
    public Boolean isAutoClear() {
        return options.optBoolean("autoClear", false);
    }

    /**
     * ID for the local notification as a number.
     */
    public Integer getId() {
        return options.optInt("id", 0);
    }

    /**
     * ID for the local notification as a string.
     */
    public String getIdStr() {
        return getId().toString();
    }

    /**
     * Trigger date.
     */
    public Date getTriggerDate() {
        return new Date(getTriggerTime());
    }

    /**
     * Trigger date in milliseconds.
     */
    public long getTriggerTime() {
        return Math.max(
                System.currentTimeMillis(),
                options.optLong("at", 0) * 1000
        );
    }

    /**
     * Title for the local notification.
     */
    public String getTitle() {
        String title = options.optString("title", "");

        if (title.isEmpty()) {
            title = context.getApplicationInfo().loadLabel(
                    context.getPackageManager()).toString();
        }

        return title;
    }

    /**
     * @return
     *      The notification color for LED
     */
    public int getLedColor() {
        String hex = options.optString("led", "000000");
        int aRGB   = Integer.parseInt(hex,16);

        aRGB += 0xFF000000;

        return aRGB;
    }

    /**
     * Sound file path for the local notification.
     */
    public Uri getSoundUri() {
        Uri uri = null;

        try{
            uri = Uri.parse(options.optString("soundUri"));
        } catch (Exception e){
            e.printStackTrace();
        }

        return uri;
    }

    /**
     * Icon bitmap for the local notification.
     */
    public Bitmap getIconBitmap() {
        String icon = options.optString("icon", "icon");
        Bitmap bmp;

        try{
            Uri uri = Uri.parse(options.optString("iconUri"));
            bmp = assets.getIconFromUri(uri);
        } catch (Exception e){
            bmp = assets.getIconFromDrawable(icon);
        }

        return bmp;
    }

    /**
     * Small icon resource ID for the local notification.
     */
    public int getSmallIcon () {
        String icon = options.optString("smallIcon", "");

        int resId = assets.getResIdForDrawable(icon);

        if (resId == 0) {
            resId = android.R.drawable.screen_background_dark;
        }

        return resId;
    }

    /**
     * JSON object as string.
     */
    public String toString() {
        return options.toString();
    }

}


