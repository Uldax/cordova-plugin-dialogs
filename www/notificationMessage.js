/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * 'License'); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

var exec = require('cordova/exec'),
    channel = require('cordova/channel');

//Notification API implementation
var NotificationPermission = {
    default : 'default',
    denied : 'denied',
    granted : 'granted'
},

NotificationDirection = {
    auto : 'auto',
    ltr : 'ltr',
    rtl : 'rtl'
};

function createJSONData(notification){
    var options = {
        dir  : notification.dir,
        lang : notification.lang,
        body : notification.body,
        tag  : notification.tag ,
        icon : notification.icon
    };
    //Prevent not found error
    for (var opt in options) {
        if (options[opt] === null ) {
            delete options[opt];
        }
    }
    return options;
}

//The user agent must keep a list of pending notifications and a list of active notifications.
function createEvent(type, data) {
    var event = document.createEvent('Events');
    event.initEvent(type, false, false);
    if (data) {
        for (var i in data) {
            if (data.hasOwnProperty(i)) {
                event[i] = data[i];
            }
        }
    }
    return event;
}

var globalItemId = Date.now(),
notificationId = Date.now();

function generateItemId() {
    var itemId = globalItemId++;
    if (!window.isFinite(itemId)) {
        globalItemId = 0;
    }
    return itemId.toString();
}

function createConstProperty(object, name, value) {
    Object.defineProperty(object, name, {
        enumerable: true,
        get: function() { return value; }
    });
}

function hasPermission (callback) {
    if (device.platform !== 'iOS') {
        callback(true);
        return;
    }
    //TODO
    exec(callback, null, 'LocalNotification', 'hasPermission', []);
}


var Notification = function (title , options) {
    if (arguments.length === 0 || typeof title !== 'string') {
        throw new TypeError('Failed to construct \'Notification\': 1 argument required, but only 0 present');
    }

    options = options || {};
    // itemId is required parameter that identifies the notification.
    // If tag is provided it serves as an itemId, when tag isn't provided itemId is generated.
    if (!options.tag) {
       options.tag = generateItemId() + '_itemId';
    }
    var dir = (options.dir && (options.dir in NotificationDirection)) ? options.dir : 'auto' ;

    createConstProperty(this, 'title', title);
    createConstProperty(this, 'lang', options.lang || '');
    createConstProperty(this, 'body', options.body || null);
    createConstProperty(this, 'icon', options.icon || null);
    createConstProperty(this, 'tag', options.tag);
    createConstProperty(this, 'dir', dir);

    //this._id = notification_idCounter++;
    var self = this;
    //this.onclick = null;
    //Setter because we don't use addEventListener ?
    Object.defineProperty(this, "onclick", {
        set: function (callBack) {
            console.log('in the set function');
            exec(null, null, 'NotificationMessage', 'onclick', [self.tag,callback]);
            }
    });

    this.onerror = null;
    //deprecate
    this.onshow = null;
    this.onclose = null;

     // EventTarget Interface
    if (EventTarget) {
        console.log('EventTargetDetected');
        var eventTarget = document.createDocumentFragment();
        function delegate(method) {
            this[method] = eventTarget[method].bind(eventTarget);
        }
        [
          'addEventListener',
          'dispatchEvent',
          'removeEventListener'
        ].forEach(delegate, this);
    } else {
        //Create the eventHandler
        targetEventHandlers = {};
        targetEventHandlers.onclick = channel.create('click');
        targetEventHandlers.onshow = channel.create('show');
        targetEventHandlers.onerror = channel.create('error');
        targetEventHandlers.onclose = channel.create('close');
        this.addEventListener = function (type, handler) {
            var e = type.toLowerCase();
            //if the type is a channel(EventHandler)
            if ((targetEventHandlers[e] !== 'undefined')) {
                targetEventHandlers[e].subscribe(handler);
            } else {
                console.log('Error with channel');
            }
        };

        this.removeEventListener = function (type, handler) {
            var e = type.toLowerCase();
            if (typeof targetEventHandlers[e] !== 'undefined') {
                targetEventHandlers[e].unsubscribe(handler);
            } else {
                console.log('Error with channel in removeListener');
            }
        };

        this.dispatchEvent = function (type) {
            var e = type.toLowerCase(),
                evt = createEvent(e,null);
            if (typeof targetEventHandlers[e] !== 'undefined') {
                setTimeout(function () {
                    targetEventHandlers[e].fire(evt);
                }, 0);
            } else {
                console.log('Error with channel in dispatchEvent');
            }
        };
    }

    //Continue running these steps asynchronouusly.
    setTimeout(function() {
        //If the notification platform supports icons,
        //the user agent may start fetching notification's icon URL at this point, if icon URL is set.
        self.show();
    }, 0);
};

Notification.permission = NotificationPermission.default;

// Method Stub - each WebWorks app has 'granted' permission by default
// exept ios ?
Notification.requestePermission = function (callback) {
    hasPermission(function (granted) {
        console.log('Have notification permission: ' + granted);
        Notification.permission = NotificationPermission.granted;
    });
};

//If notification is neither in the list of pending notifications nor in the list of active notifications, terminate these steps
//Queue a task to remove notification from either the list of pending notifications or the list of active notifications,
//and fire an event named close on notification.
Notification.prototype.close = function() {
    if (this.tag) {
        exec(null, null, 'NotificationMessage', 'close', [this.tag]);
    }
    //this.dispatchEvent('close');
    this.dispatchEvent(createEvent('close',null));
};

Notification.prototype.show = function() {

    if (Notification.permission === NotificationPermission.default) {
        Notification.requestePermission();
    }
    if (Notification.permission !== NotificationPermission.granted){
        //cancel any ongoing fetch for notification's icon URL, queue a task to fire an event
        console.log('not Allowed');
        //this.dispatchEvent('error');
        this.dispatchEvent(createEvent('onerror',null));
    } else {
        var options = createJSONData(this);
        //display notification on device
        console.log(options);
        exec(null, this.error, 'NotificationMessage', 'show', [ this.title, options]);
        //If displaying fails (e.g. the notification platform returns an error), fire an event named error on notification and terminate these steps.
        //Append notification to the list of active notifications.
        //Fire an event named show on notification.
    }
};

Notification.prototype.error = function(e) {
    console.log(e);
    this.dispatchEvent(createEvent('onerror',e));
}

module.exports = Notification;