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
package com.netscape.certsrv.base;

import java.util.*;
import java.security.cert.X509Certificate;


/**
 * This class manages nonces sometimes used to control request state flow.
 * <P>
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class Nonces implements IAuthInfo {

    private Hashtable mNonces = new Hashtable();
    private Vector mNonceList = new Vector();
    private int mNonceLimit;

    /**
     * Constructs nonces.
     */
    public Nonces() {
        mNonceLimit = 100;
        Vector mNonceList = new Vector();
        Hashtable mNonces = new Hashtable();
    }

    public Nonces(int limit) {
        mNonceLimit = limit;
        Vector mNonceList = new Vector();
        Hashtable mNonces = new Hashtable();
    }

    public long addNonce(long nonce, X509Certificate cert) {
        long i;
        long k = 0;
        long n = nonce;
        long m = (long)((mNonceLimit / 2) + 1);

        for (i = 0; i < m; i++) {
            k = n + i;
            // avoid collisions
            if (!mNonceList.contains((Object)k)) {
                break;
            }
            k = n - i;
            // avoid collisions
            if (!mNonceList.contains((Object)k)) {
                break;
            }
        }
        if (i < m) {
            mNonceList.add(k);
            mNonces.put(k, cert);
            if (mNonceList.size() > mNonceLimit) {
                n = ((Long)(mNonceList.firstElement())).longValue();
                mNonceList.remove(0);
                mNonces.remove((Object)n);
            }
        } else {
            // failed to resolved collision
            k = -nonce;
        }
        return k;
    }

    public X509Certificate getCertificate(long nonce) {
        X509Certificate cert = (X509Certificate)mNonces.get(nonce);
        return cert;
    }

    public X509Certificate getCertificate(int index) {
        X509Certificate cert = null;
        if (index >= 0 && index < mNonceList.size()) {
            long nonce = ((Long)(mNonceList.elementAt(index))).longValue();
            cert = (X509Certificate)mNonces.get(nonce);
        }
        return cert;
    }

    public long getNonce(int index) {
        long nonce = 0;
        if (index >= 0 && index < mNonceList.size()) {
            nonce = ((Long)(mNonceList.elementAt(index))).longValue();
        }
        return nonce;
    }

    public void removeNonce(long nonce) {
        mNonceList.remove((Object)nonce);
        mNonces.remove((Object)nonce);
    }


    public int size() {
        return mNonceList.size();
    }

    public int maxSize() {
        return mNonceLimit;
    }

    public void clear() {
        mNonceList.clear();
        mNonces.clear();
    }

    public boolean isInSync() {
        return (mNonceList.size() == mNonces.size());
    }
}
