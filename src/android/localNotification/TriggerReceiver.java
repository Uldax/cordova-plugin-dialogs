package org.apache.cordova.dialogs.localNotification;

/**
 * The alarm receiver is triggered when a scheduled alarm is fired. This class
 * reads the information in the intent and displays this information in the
 * Android notification bar. The notification uses the default notification
 * sound and it vibrates the phone.
 */
public class TriggerReceiver extends AbstractTriggerReceiver {

    /**
     * Called when a local notification was triggered. Does present the local
     * notification and re-schedule the alarm if necessary.
     *
     * @param notification
     *      Wrapper around the local notification
     * @param updated
     *      If an update has triggered or the original
     */
    @Override
    public void  (Notification notification, boolean updated) {
        notification.show();
    }

    /**
     * Build notification specified by options.
     *
     * @param builder
     *      Notification builder
     */
    @Override
    public Notification buildNotification (Builder builder) {
        return builder.build();
    }

}