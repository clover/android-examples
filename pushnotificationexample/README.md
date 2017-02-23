# Push Notification Example

This is an application that shows how you could receive push notifications from Clover.

To properly try this example, create your own version of this example (with your own package name).  Then add the app on Clover Developer portal and upload the apk.  Submit the apk and record down the appId.  Then login as your associated merchant account and install this app.  Once installed, launch the app and you will see instructions on how to post the notification.  Leave the app on that screen and go to https://www.clover.com/api_docs and find POST /v3/apps/{aId}/merchants/{mId}/notifications, you will have to fill out the API Token and mID on top of the page with the app's App Secret (not API Token) and merchant's mID. The basic body of the notification is something like this:
```
{
  "event": "test_notification",
  "data": "Up to 4k of data"
}
```
