# Twikon

Twitter application for Android development project.

## Function
- Account authentication
- Display timeline
- Refresh timeline
- Tweet
- Retweet

## Before use this application
First, please sign in [Twitter Developers](https://dev.twitter.com/) with your Twitter account.    

Second, click "create a new application" and input your information.    

Third, memo your ***Callback URL***, ***Consumer ker*** and ***Consumer secret*** at "Details".

Finally, make "res/values/strings.xml" file like the following...   
```
<?xml version="1.0" encoding="utf-8"?>    
<resources>

    <string name="app_name">MyTwitter</string>
    <string name="hello_world">Hello world!</string>
    <string name="menu_settings">Settings</string>
    <string name="twitter_consumer_key"> "Input Consumer key" </string>
    <string name="twitter_consumer_secret"> "Input Consumer secret" </string>
    <string name="twitter_oauth">Authentication</string>
	<string name="twitter_callback_url"> "Input Callback URL" </string>
    <string name="menu_refresh">Refresh</string>
	<string name="submit_tweet">Tweet</string>
</resources>
```
