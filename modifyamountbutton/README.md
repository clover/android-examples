This example shows you how to add a button on the pay activity. In this case the button does not represent a tender, but allows your brand/application to launched at this point in the checkout flow. Common examples are for loyalty, promotional discounts, donations etc. 

Simply define an activity that reacts to the clover.intent.action.MODIFY_ORDER action. The Clover pay activity will automatically show a button for all applications that listen for this intent.

For example in your AndroidManifest.xml -

```
 <activity
	android:name=".AddDiscountActivity"
    android:label="@string/title_activity_add_discount">
    <intent-filter>
    	<action android:name="clover.intent.action.MODIFY_ORDER"/>
     	<category android:name="android.intent.category.DEFAULT"/>
    </intent-filter>
</activity>
```
