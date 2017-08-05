package org.gemafrzen.redditsavedlinks.exceptions;

/**
 * Created on 05.08.2017.
 */

public class NoRefreshOfTokenException extends Exception {
    private String errormessage = "";

    public NoRefreshOfTokenException(String errormessage){
        this.errormessage = errormessage;
    }

    public String getErrormessage() {
        return errormessage;
    }
}
