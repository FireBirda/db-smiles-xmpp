package org.jivesoftware.smack;

/**
 * The AbstractConnectionListener class provides an empty implementation for all
 * methods defined by the {@link ConnectionListener} interface. This is a
 * convenience class which should be used in case you do not need to implement
 * all methods.
 * 
 * @author Henning Staib
 */
public class AbstractConnectionListener implements ConnectionListener {

    public void connectionClosed() {
        // do nothing
    }

    public void connectionClosedOnError(Exception e) {
        // do nothing
    }

    public void reconnectingIn(int seconds) {
        // do nothing
    }

    public void reconnectionFailed(Exception e) {
        // do nothing
    }

    public void reconnectionSuccessful() {
        // do nothing
    }

}
