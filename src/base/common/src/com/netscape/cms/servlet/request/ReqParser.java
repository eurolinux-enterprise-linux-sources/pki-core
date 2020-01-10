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
package com.netscape.cms.servlet.request;


import com.netscape.cms.servlet.common.*;
import com.netscape.cms.servlet.base.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.security.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.dbs.*;

import com.netscape.certsrv.request.*;
import com.netscape.cms.servlet.*;


/**
 * A class representing a request parser.
 * <P>
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class ReqParser implements IReqParser {

    private final static String TYPE = "requestType";
    private final static String STATUS = "status";
    private final static String CREATE_ON = "createdOn";
    private final static String UPDATE_ON = "updatedOn";
    private final static String UPDATE_BY = "updatedBy";

    /**
     * Constructs a request parser.
     */
    public ReqParser() {
    }

    /**
     * Maps request object into argument block.
     */
    public void fillRequestIntoArg(Locale l, IRequest req, CMSTemplateParams argSet, IArgBlock arg)
        throws EBaseException {
        arg.addStringValue(TYPE, req.getRequestType());
        arg.addLongValue("seqNum", 
            Long.parseLong(req.getRequestId().toString()));
        arg.addStringValue(STATUS, 
            req.getRequestStatus().toString());
        arg.addLongValue(CREATE_ON, 
            req.getCreationTime().getTime() / 1000);
        arg.addLongValue(UPDATE_ON, 
            req.getModificationTime().getTime() / 1000);
        String updatedBy = req.getExtDataInString(IRequest.UPDATED_BY);

        if (updatedBy == null) updatedBy = "";
        arg.addStringValue(UPDATE_BY, updatedBy);

        SessionContext ctx = SessionContext.getContext();
        String id = (String) ctx.get(SessionContext.USER_ID); 

        arg.addStringValue("callerName", id);
		
        String owner = req.getRequestOwner();

        if (owner != null) 
            arg.addStringValue("assignedTo", owner);
    }
}
