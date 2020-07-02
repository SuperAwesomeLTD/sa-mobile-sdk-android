/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.lib.sautils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Class that contains a lot of static aux functions used across the SDK to simplify code
 */
public class SAUtils {

    /**
     * SAUtils enum defining possible SDK system sizes
     *  - undefined: means the SDK could not determine the correct system size
     *  - phone: the SDK determined it's a phone-type device
     *  - tablet: the SDK determined it's a tablet-type device
     */
    public enum SASystemSize {
        undefined {
            @Override
            public String toString() {
                return "undefined";
            }
        },
        phone {
            @Override
            public String toString() {
                return "phone";
            }
        },
        tablet {
            @Override
            public String toString() {
                return "tablet";
            }
        }
    }

    /**
     * SAUtils enum defining possible connection types
     *  - unknown
     *  - ethernet
     *  - wifi
     *  - cellular_unknown, 2g, 3g, 4g,
     */
    public enum SAConnectionType {
        unknown {
            @Override
            public String toString() {
                return "unknown";
            }
        },
        ethernet {
            @Override
            public String toString() {
                return "ethernet";
            }
        },
        wifi {
            @Override
            public String toString() {
                return "wifi";
            }
        },
        cellular_unknown {
            @Override
            public String toString() {
                return "cellular_unknown ";
            }
        },
        cellular_2g {
            @Override
            public String toString() {
                return "cellular_2g";
            }
        },
        cellular_3g {
            @Override
            public String toString() {
                return "cellular_3g";
            }
        },
        cellular_4g {
            @Override
            public String toString() {
                return "cellular_4g";
            }
        }
    }

    /**
     * Internal SAUtils class defining a SASize object (containing width & height)
     */
    public static class SASize {

        // member vars
        public int width = 0;
        public int height = 0;

        /**
         * SASize basic constructor
         *
         * @param w new width
         * @param h new height
         */
        SASize (int w, int h){
            width = w;
            height = h;
        }
    }

    /**
     * Function that does the math to transform a pair of Width x Height into a new pair
     * of Width x Height by also maintaining aspect ratio
     *
     * @param sourceW   new Height
     * @param sourceH   new Width
     * @param boundingW source Width
     * @param boundingH source Height
     * @return          a rect with X, Y, W, H
     */
    public static Rect mapSourceSizeIntoBoundingSize(float sourceW, float sourceH, float boundingW, float boundingH) {
        if (boundingW == 1 || boundingW == 0) { boundingW = sourceW; }
        if (boundingH == 1 || boundingH == 0) { boundingH = sourceH; }
        float oldR = boundingW / boundingH;
        float newR = sourceW / sourceH;
        float X = 0, Y = 0, W = 0, H = 0;

        if (oldR > newR) {
            W = sourceW; H = W / oldR; X = 0; Y = (sourceH - H) / 2.0f;
        }
        else {
            H = sourceH; W = H * oldR; Y = 0; X = (sourceW - W) / 2.0f;
        }

        return new Rect((int)X, (int)Y, (int)W, (int)H);
    }

    /**
     * Function that checks to see if a target rect is inside a frame rect
     *
     * @param target the child target
     * @param frame  the parent frame
     * @return       true or false
     */
    public static boolean isTargetRectInFrameRect(Rect target, Rect frame) {
        // parent
        float x11 = frame.left;
        float y11 = frame.top;
        float x12 = frame.left + frame.right;
        float y12 = frame.top + frame.bottom;

        // child
        float x21 = target.left;
        float y21 = target.top;
        float x22 = target.left + target.right;
        float y22 = target.top + target.bottom;

        // overlaps
        float overlap_x = Math.max(0, Math.min(x12, x22)) - Math.max(x11, x21);
        float overlap_y = Math.max(0, Math.min(y12, y22)) - Math.max(y11, y21);
        float overlap = overlap_x * overlap_y;

        // child area
        float barea = target.right * target.bottom;

        // threshold
        float threshold = barea / 2.0f;

        return overlap > threshold;
    }

    /**
     * Function that returns a random number between two limits
     *
     * @param min min edge
     * @param max max edge
     * @return    a random integer
     */
    public static int randomNumberBetween(int min, int max){
        Random rand  = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

    /**
     * Generate a Unique Key
     *
     * @return a unique key string
     */
    public static String generateUniqueKey () {
        // constants
        final String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXZY0123456789";
        final int length = alphabet.length();
        final int dauLength = 32;

        // generate the string
        String s = "";
        for (int i = 0; i < dauLength; i++){
            int index = SAUtils.randomNumberBetween(0, length - 1);
            s += alphabet.charAt(index);
        }
        return s;
    }

    /**
     * Remove all elements from a list, except the first
     *
     * @param original the original list
     * @return         a list with just one element, the first one of the original
     */
    public static List removeAllButFirstElement(List original) {
        if (original != null && original.size() >= 1) {
            List finalList = new ArrayList<>();
            finalList.add(original.get(0));
            return finalList;
        } else  {
            return original;
        }
    }

    /**
     * Get the current scale factor
     *
     * @param activity  the activity to pass along as context
     * @return          a float meaning the scale
     */
    public static float getScaleFactor(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);
        return  (float) metrics.densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT;
    }

    /**
     * Function that returns the current screen size
     *
     * @param activity  the activity to pass along as context
     * @return          a SASize object with width & height members
     */
    public static SASize getRealScreenSize(Activity activity, boolean rotate) {
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();

        View decorView = activity.getWindow().getDecorView();

        if (!rotate){
            return new SASize(decorView.getWidth(), decorView.getHeight());
        } else {
            return new SASize(decorView.getHeight(), decorView.getWidth());
        }
    }

    /**
     * Function that gets the system size
     *
     * @return where either tablet or mobile
     */
    public static SASystemSize getSystemSize() {

        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double)width/(double)dens;
        double hi = (double)height/(double)dens;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi, 2);
        double screenInches = Math.sqrt(x + y);

        if (screenInches < 6){
            return SASystemSize.phone;
        } else {
            return SASystemSize.tablet;
        }
    }

    /**
     * Just return the verbose system
     *
     * @return a string
     */
    public static String getVerboseSystemDetails() {
        return "android" + "_" + getSystemSize().toString();
    }

    /**
     * Function that gets the app name
     *
     * @return the app name
     */
    public static String getAppLabel(Context context) {

        String defaultAppLabel = "Unknown";

        if (context != null) {

            PackageManager packageManager = context.getPackageManager();

            if (packageManager != null) {

                try {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
                    String name = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
                    name = URLEncoder.encode(name, "UTF-8");
                    return name;
                } catch (PackageManager.NameNotFoundException | UnsupportedEncodingException  ignored) {
                    return defaultAppLabel;
                }
            } else {
                return defaultAppLabel;
            }
        } else {
            return defaultAppLabel;
        }
    }

    /**
     * Function that returns loaded plugin name or null if no plugin is used
     * @param context Android Context
     * @return the name of the plugin e.g. admob, mopub
     */
    public static String getPluginName(Context context) {
        if (context == null) return null;

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            return applicationInfo.metaData.getString("tv.superawesome.plugins.publisher.NAME");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Function that checks at run-time if a class is loaded or not
     *
     * @param className the name I want a test for
     * @return          true if found, false otherwsie
     */
    public static boolean isClassAvailable(String className){
        boolean driverAvailable = true;

        try {
            Class.forName(className);
        } catch (ClassNotFoundException | NullPointerException e) {
            driverAvailable = false;
        }

        return driverAvailable;
    }

    /**
     * Gets the current connection type
     *
     * @return A connection type (SA) enum
     */
    public static SAConnectionType getNetworkConnectivity (Context context) {
        // null guard
        if (context == null) return SAConnectionType.unknown;

        // get connectivity manager
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        // unknown case
        if(info == null || !info.isConnected()) {
            return SAConnectionType.unknown;
        }

        // wifi case
        if(info.getType() == ConnectivityManager.TYPE_WIFI) {
            return SAConnectionType.wifi;
        }

        // mobile case
        if(info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return SAConnectionType.cellular_2g;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return SAConnectionType.cellular_3g;
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return SAConnectionType.cellular_4g;
                default:
                    return SAConnectionType.unknown;
            }
        }

        // default return
        return SAConnectionType.unknown;
    }

    /**
     * Get the User agent based on android size (mobile or tablet)
     *
     * @param context the current context
     * @return        a user agent string
     */
    public static String getUserAgent(Context context) {
        if (Build.VERSION.SDK_INT >= 17) {
            if (context != null) {
                try {
                    return WebSettings.getDefaultUserAgent(context);
                } catch (Exception e) {
                    return System.getProperty("http.agent");
                }
            } else {
                return System.getProperty("http.agent");
            }
        } else {
            if (context != null) {
                return new WebView(context).getSettings().getUserAgentString();
            } else {
                return System.getProperty("http.agent");
            }
        }
    }

    /**
     * Cache buster is just a big random number
     */
    public static int getCacheBuster() {
        return randomNumberBetween(1000000, 1500000);
    }

    /**
     * Function that forms a query string from a dict
     *
     * @param dict a json dictionary
     */
    public static String formGetQueryFromDict(JSONObject dict) {
        String queryString = "";

        // null check
        if (dict == null) return queryString;

        ArrayList<String> queryArray = new ArrayList<>();


        Iterator<String> keys = dict.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object valObj = dict.opt(key);
            String val = (valObj != null ? valObj.toString().replace("\"","") : "");

            queryArray.add(key + "=" + val + "&");
        }

        for (String queryObj : queryArray) {
            queryString += queryObj;
        }

        if (queryString.length() > 1) {
            return queryString.substring(0, queryString.length() - 1);
        } else {
            return queryString;
        }
    }

    /**
     * Function that encodes a dict
     *
     * @param dict a json dictionary
     */
    public static String encodeDictAsJsonDict(JSONObject dict) {
        // check null
        if (dict == null || dict.length() == 0) return "%7B%7D";

        // if all's ok try encoding
        return encodeURL(dict.toString());
    }

    /**
     * Method that encodes an URL correctly
     *
     * @param urlStr URL to be encoded
     * @return       a new encoded URL
     */
    public static String encodeURL(String urlStr) {
        // check null
        if (urlStr == null || urlStr.equals("")) return "";

        // if all's ok try encoding
        try {
            return URLEncoder.encode(urlStr, "UTF-8");
        } catch (UnsupportedEncodingException | NullPointerException e) {
            return "";
        }
    }

    /**
     * Get a base CDN URL from resource
     *
     * @param resourceURL a valid resource URL
     * @return            a base string formed from a full resource URL
     */
    public static String findBaseURLFromResourceURL(String resourceURL) {
        if (resourceURL == null) return null;
        if (!isValidURL(resourceURL)) return null;
        String workString = resourceURL.replace("\\", "");
        String[] components = workString.split("/");
        String result = "";
        for (int i = 0; i < components.length - 1; i++){
            result += components[i] + "/";
        }
        return isValidURL(result) ? result : null;
    }

    /**
     * Return true if json is empty, false otherwise
     *
     * @param dict a json dict
     */
    public static boolean isJSONEmpty(JSONObject dict) {
        return dict == null || dict.length() == 0 || dict.toString().equals("{}");
    }

    /**
     * Checks if an URL is valid
     *
     * @param url the url in question
     * @return    true if valid, false otherwise
     */
    public static boolean isValidURL(String url) {
        String regex = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        return url != null && !url.equals("http://") && !url.equals("https://") && pattern.matcher(url).matches();
    }

    /**
     * Validate email
     *
     * @param target the char sequence to check for email validity
     * @return       true of false
     */
    public static boolean isValidEmail(CharSequence target) {
        String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        return target != null && !target.toString().isEmpty() && pattern.matcher(target).matches();
    }

    /**
     * Check weather a view is currently on screen and visible
     *
     * @param view to be checked
     * @return true if the view is visible, false otherwise
     */
    public static boolean isViewVisibleOnScreen(final View view) {
        if (view == null || !view.isShown()) {
            return false;
        }

        final DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        final Rect screenRect = new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);

        final Rect viewRect = new Rect();
        view.getGlobalVisibleRect(viewRect);

        return viewRect.intersect(screenRect);
    }
}
