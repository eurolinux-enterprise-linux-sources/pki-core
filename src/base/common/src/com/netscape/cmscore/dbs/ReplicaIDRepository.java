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
package com.netscape.cmscore.dbs;


import java.math.BigInteger;

import com.netscape.certsrv.dbs.*;
import com.netscape.certsrv.dbs.replicadb.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.apps.CMS;

/**
 * A class represents a replica repository. It
 * creates unique managed replica IDs.
 * <P>
 *
 * @author alee
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class ReplicaIDRepository extends Repository
    implements IReplicaIDRepository {

    private IDBSubsystem mDBService;
    private String mBaseDN;

    /**
     * Constructs a certificate repository.
     */
    public ReplicaIDRepository(IDBSubsystem dbService, int increment, String baseDN)
        throws EDBException {
        super(dbService, increment, baseDN);
        mBaseDN = baseDN;
        mDBService = dbService;
    }
   
 
    /**
     * Returns last serial number in given range
     */
    public BigInteger getLastSerialNumberInRange(BigInteger serial_low_bound, BigInteger serial_upper_bound)
    throws EBaseException {
        CMS.debug("ReplicaIDReposoitory: in getLastSerialNumberInRange: low "  + serial_low_bound + " high " + serial_upper_bound);
        if(serial_low_bound == null || serial_upper_bound == null || serial_low_bound.compareTo(serial_upper_bound) >= 0 ) {
            return null;
        }
        BigInteger ret = new BigInteger(getMinSerial());
        if ((ret==null) || (ret.compareTo(serial_upper_bound) >0) || (ret.compareTo(serial_low_bound) <0)) {
            return null;
        }
        return ret;
    }

    /**
     * Retrieves DN of this repository.
     */
    public String getDN() {
        return mBaseDN;
    }

    /**
     * Retrieves backend database handle.
     */
    public IDBSubsystem getDBSubsystem() {
        return mDBService;
    }
}
