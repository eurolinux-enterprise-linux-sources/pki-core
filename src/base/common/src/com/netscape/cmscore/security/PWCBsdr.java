// --- BEGIN COPYRIGHT BLOCK ---
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
// (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---
package com.netscape.cmscore.security;


import org.mozilla.jss.*;
import org.mozilla.jss.crypto.*;
import org.mozilla.jss.crypto.SecretDecoderRing;
import org.mozilla.jss.crypto.TokenException;
import java.io.*;
import java.lang.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.mozilla.jss.util.Password;
import org.mozilla.jss.util.PasswordCallback;
import org.mozilla.jss.util.PasswordCallbackInfo;
import com.netscape.cmscore.base.*;
import com.netscape.certsrv.apps.CMS;
import com.netscape.certsrv.logging.ILogger;


/* 
 * A class to retrieve passwords from the SDR password cache
 *
 * @author Christina Fu
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */

public class PWCBsdr implements PasswordCallback {
    InputStream in = null;
    OutputStream out = null;
    String mprompt = "";  
    boolean firsttime = true;
    private PasswordCallback mCB = null;
    private String mPWcachedb = null;
    private ILogger mLogger = null;

    public PWCBsdr() {
        this(null);
    }
  
    public PWCBsdr(String prompt) {
        in = System.in;
        out = System.out;
        mprompt = prompt;

        /* to get the test program work
         System.out.println("before CMS.getLogger");
         try {
         */
        mLogger = CMS.getLogger();

        /*
         } catch (NullPointerException e) {
         System.out.println("after CMS.getLoggergot NullPointerException ... testing ok");
         }
         System.out.println("after CMS.getLogger");
         */
        // get path to password cache
        try {
            mPWcachedb = CMS.getConfigStore().getString("pwCache");
            CMS.debug("got pwCache from configstore: " +
                mPWcachedb);
        } catch (NullPointerException e) {
            System.out.println("after CMS.getConfigStore got NullPointerException ... testing ok");
        } catch (Exception e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSCORE_SECURITY_GET_CONFIG"));
            // let it fall through
        }

        //    System.out.println("after CMS.getConfigStore");
        if (File.separator.equals("/")) {  
            // Unix
            mCB = new PWsdrConsolePasswordCallback(prompt);
        } else {
            mCB = new PWsdrConsolePasswordCallback(prompt);
            // mCB = new PWsdrDialogPasswordCallback( prompt );
        }

        // System.out.println( "Created PWCBsdr with prompt of "
        //                   + mprompt );
    }

    /* We are now assuming that PasswordCallbackInfo.getname() returns 
     * the tag we are hoping to match in the cache.
     */

    public Password getPasswordFirstAttempt(PasswordCallbackInfo info)
        throws PasswordCallback.GiveUpException {

        CMS.debug("in getPasswordFirstAttempt");

        /* debugging code to see if token is logged in
         try {
         CryptoManager cm = CryptoManager.getInstance();
         CryptoToken token =
         cm.getInternalKeyStorageToken();
         if (token.isLoggedIn() == false) {
         // missed it.
         CMS.debug("token not yet logged in!!");
         } else {
         CMS.debug("token logged in.");
         }
         } catch (Exception e) {
         CMS.debug("crypto manager error:"+e.toString());
         }
         CMS.debug("still in getPasswordFirstAttempt");
         */
        Password pw = null;
        String tmpPrompt = info.getName();

        String skip_token = System.getProperty("cms.skip_token");

        if (skip_token != null) {
            if (tmpPrompt.equals(skip_token)) {
                throw new PasswordCallback.GiveUpException();
            }
        }

        try {
            String defpw = System.getProperty("cms.defaultpassword");

            if (defpw != null) {
                return new Password(defpw.toCharArray());
            }

            /* mprompt has precedence over info.name */
            if (!(mprompt == null)) {
                tmpPrompt = mprompt;
            }

            if (tmpPrompt == null) { /* no name, fail */
                System.out.println("Shouldn't get here");
                throw new PasswordCallback.GiveUpException();
            } else {  /* get password from password cache */

                CMS.debug("getting tag = " + tmpPrompt);
                PWsdrCache pwc = new PWsdrCache(mPWcachedb, mLogger);

                pw = pwc.getEntry(tmpPrompt);

                if (pw != null) {
                    CMS.debug("non-null password returned in first attempt");
                    String tmp = new String(pw.getCharCopy());

                    return (pw);
                } else { /* password not found */
                    // we don't want caller to do getPasswordAgain,    for now
                    log(ILogger.LL_FAILURE, CMS.getLogMessage("CMSCORE_SECURITY_THROW_CALLBACK"));
                    throw new PasswordCallback.GiveUpException();
                }
            }
        } catch (Throwable e) {
            // System.out.println( "BUG HERE!!!!first!!!!!!!!!!!!!!!!!" );
            // e.printStackTrace();
            throw new PasswordCallback.GiveUpException();
        }
    }

    /* The password cache has failed to return a password (or a usable password.
     * Now we will try and get the password from the user and hopefully add
     * the password to the cache pw cache 
     */
    public Password getPasswordAgain(PasswordCallbackInfo info)
        throws PasswordCallback.GiveUpException {

        CMS.debug("in getPasswordAgain");
        try {
            Password pw = null;

            try {
                if (firsttime) {
                    try {
                        firsttime = false;

                        pw = mCB.getPasswordFirstAttempt(info);
                    } catch (PasswordCallback.GiveUpException e) {
                        throw new PasswordCallback.GiveUpException();
                    }
                } else {
                    pw = mCB.getPasswordAgain(info);
                }
                return (pw);
            } catch (PasswordCallback.GiveUpException e) {
                throw new PasswordCallback.GiveUpException();
            }
        } catch (Throwable e) {
            // System.out.println( "BUG HERE!! in the password again!!"
            //                   + "!!!!!!!!!!!" );
            // e.printStackTrace();
            throw new PasswordCallback.GiveUpException();
        }
    }

    public void log(int level, String msg) {
        if (mLogger == null) {
            System.out.println(msg);
        } else {
            mLogger.log(ILogger.EV_SYSTEM, ILogger.S_OTHER, level, "PWCBsdr " + msg); 
        }
    }
}


class PWsdrConsolePasswordCallback implements PasswordCallback {
    private String mPrompt = null;

    public PWsdrConsolePasswordCallback(String p) {
        mPrompt = p;
    }

    public String getPrompt() {
        return mPrompt;
    }

    public Password getPasswordFirstAttempt(PasswordCallbackInfo info)
        throws PasswordCallback.GiveUpException {
        if (mPrompt == null) {
            System.out.println("Get password " + info.getName());
        } else {
            System.out.println(getPrompt());
        }

        Password tmppw = PWUtil.readPasswordFromStream();

        return (tmppw);
    }

    public Password getPasswordAgain(PasswordCallbackInfo info)
        throws PasswordCallback.GiveUpException {
        System.out.println("Password Incorrect.");
        if (mPrompt == null) {
            System.out.println("Get password " + info.getName());
        } else {
            System.out.println(getPrompt());
        }

        Password tmppw = PWUtil.readPasswordFromStream();

        return (tmppw);
    }
}


class PWsdrDialogPasswordCallback extends JDialogPasswordCallback {
    private String mPrompt = null;

    public PWsdrDialogPasswordCallback(String p) {
        super();
        mPrompt = p;
    }

    public String getPrompt(PasswordCallbackInfo info) {
        if (mPrompt == null) {
            return super.getPrompt(info);
        } else {
            return mPrompt;
        }
    }
}

