Parental gates are used in apps targeted towards kids to prevent them from engaging in commerce or following links out of an app to websites, social networks, or other apps without the knowledge of their parent or guardian. A parental gate presents an adult level task which must be completed in order to continue.

To enable the parental gate, set to "true" the third parameter in the SAVideoActivity.show function:

```
SAVideoActivity.start(MainActivity.this, ad, true, adListener, parentalGateListener, videoAdListener);

```

![](img/parental_gate.png "Parental Gate on Android")