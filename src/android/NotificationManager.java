package org.apache.cordova.dialogs;


/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * This class provides access to notifications on the device.
 *
 * Be aware that this implementation gets called on
 * navigator.notification.{alert|confirm|prompt}, and that there is a separate
 * implementation in org.apache.cordova.CordovaChromeClient that gets
 * called on a simple window.{alert|confirm|prompt}.
 */
public class NotificationManager extends CordovaPlugin {


    // Reference to the web view for static access
    private static CordovaWebView webView = null;

    // Indicates if the device is ready (to receive events)
    private static Boolean deviceready = false;

    // To inform the user about the state of the app in callbacks
    protected static Boolean isInBackground = true;

    // Queues all events before deviceready
    private static ArrayList<String> eventQueue = new ArrayList<String>();

    /**
     * Constructor.
     */
    public NotificationManager() {
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        isInBackground = false;
        deviceready();
    }

    /**
     * Call all pending callbacks after the deviceready event has been fired.
     */
    private static synchronized void deviceready() {
        isInBackground = false;
        deviceready = true;

        for (String js : eventQueue) {
            sendJavascript(js);
        }

        eventQueue.clear();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	/*
    	 * Don't run any of these if the current activity is finishing
    	 * in order to avoid android.view.WindowManager$BadTokenException
    	 * crashing the app. Just return true here since false should only
    	 * be returned in the event of an invalid action.
    	 */
        if (this.cordova.getActivity().isFinishing()) return true;

        if (action.equals("show")) {
            show(args);
        } else {
            return false;
        }

        // Only alert and confirm are async.
        callbackContext.success();
        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Use this instead of deprecated sendJavascript
     *
     * @param js JS code snippet as string
     */
    private static synchronized void sendJavascript(final String js) {

        if (!deviceready) {
            eventQueue.add(js);
            return;
        }
        Runnable jsLoader = new Runnable() {
            public void run() {
                webView.loadUrl("javascript:" + js);
            }
        };
        try {
            Method post = webView.getClass().getMethod("post", Runnable.class);
            post.invoke(webView, jsLoader);
        } catch (Exception e) {

            ((Activity) (webView.getContext())).runOnUiThread(jsLoader);
        }


        /**
         * Current application state.
         *
         * @return
         *      "background" or "foreground"
         */

    static String getApplicationState() {
        return isInBackground ? "background" : "foreground";
    }
}


