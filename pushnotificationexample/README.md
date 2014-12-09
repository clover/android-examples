# Push Notification Example

This is an application that shows how you could receive push notifications from Clover.

To properly try this example, create your own version of this example (with your own package name).  Then add the app on Clover Developer portal and upload the apk.  Submit the apk and record down the appId.  Then login as your associated merchant account and install this app.  Once installed, launch the app and you will see instructions on how to post the notification.  Leave the app on that screen and go to http://com.clover.com/api_explorer or the dev1 flavor and use api "POST /v2/merchant/{mId}/apps/{appId}/notify" with a correct Access Token and the Merchant Id of the merchant you installed the app and the appId you recorded down earlier.  Use the following JSON Post Data:

{
  "notification": {
    "appEvent": "test_notification",
    "payload": "Up to 4k of data"
  }
}

After the api is called, you will receive a notification on the device and see the text "Result: Up to 4k of data" show up on the app screen.
