/**
 * @class: SANetListener.java
 * @copyright: (c) 2015 SuperAwesome Ltd. All rights reserved.
 * @author: Gabriel Coman
 * @date: 28/09/2015
 *
 */
package tv.superawesome.sdk.data.Network;

/**
 * @brief: This is a listener interface for SAGet and SAPost async task classes
 */
public interface SANetListener {

    /**
     * This function should be called in case of Async operation success, and should
     * always return an anonymous data object
     *
     * @param: data - is a callback parameter; to be accessed by the class that implements
     * this Listener interface
     */
    void success(Object data);

    /**
     * This function should be called in case of Async operation failure, and
     * should have no parameters
     */
    void failure();

}
