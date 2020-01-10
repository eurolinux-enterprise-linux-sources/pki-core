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
package com.netscape.cmscore.logging;


import java.io.*;
import java.util.*;
import com.netscape.certsrv.logging.*;

import com.netscape.cmscore.util.*;


/**
 * A class represents certificate server logger
 * implementation.
 * <P>
 *
 * @author thomask 
 * @author mzhao
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class SignedAuditLogger extends Logger {

    /**
     * Constructs a generic logger, and registers a list
     * of resident event factories.
     */
    public SignedAuditLogger() {
        super();
        register(EV_SIGNED_AUDIT, new SignedAuditEventFactory());
    }
}
