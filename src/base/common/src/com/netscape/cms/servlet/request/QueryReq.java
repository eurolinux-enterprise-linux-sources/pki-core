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
import netscape.security.x509.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.authority.*;
import com.netscape.certsrv.authentication.*; 
import com.netscape.certsrv.authorization.*; 
import com.netscape.cms.servlet.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.logging.*;


/**
 * Show paged list of requests matching search criteria
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class QueryReq extends CMSServlet {
    // constants
    private final static String INFO = "QueryReq";
    private final static String IN_SHOW_ALL = "showAll";
    private final static String IN_SHOW_WAITING = "showWaiting";
    private final static String IN_SHOW_IN_SERVICE = "showInService";
    private final static String IN_SHOW_PENDING= "showPending";
    private final static String IN_SHOW_CANCELLED = "showCancelled";
    private final static String IN_SHOW_REJECTED = "showRejected";
    private final static String IN_SHOW_COMPLETED = "showCompleted";
    private final static String IN_MAXCOUNT = "maxCount";
    private final static String IN_TOTALCOUNT = "totalRecordCount";
    private final static String ON = "on";
    private final static String PROP_PARSER = "parser";

    private final static String TPL_FILE = "queryReq.template";

    private final static String OUT_SERVICE_URL = "serviceURL";
    private final static String OUT_OP = "op";
    private final static String OUT_MAXCOUNT = IN_MAXCOUNT;
    private final static String OUT_TOTALCOUNT = IN_TOTALCOUNT;
    private final static String OUT_CURRENTCOUNT = "currentRecordCount";
    private final static String OUT_SENTINEL_DOWN = "querySentinelDown";
    private final static String OUT_SHOW_COMPLETED = IN_SHOW_COMPLETED;
    private final static String OUT_SEQNUM = "seqNum";
    private final static String OUT_STATUS = "status";
    private final static String OUT_CREATE_ON = "createdOn";
    private final static String OUT_UPDATE_ON = "updatedOn";
    private final static String OUT_UPDATE_BY = "updatedBy";
    private final static String OUT_REQUESTING_USER = "requestingUser";
    //keeps track of where to begin if page down
    private final static String OUT_FIRST_ENTRY_ON_PAGE = "firstEntryOnPage";
    //keeps track of where to begin if page up
    private final static String OUT_LAST_ENTRY_ON_PAGE = "lastEntryOnPage";
    private final static String OUT_SUBJECT = "subject";
    private final static String OUT_REQUEST_TYPE = "requestType";
    private final static String OUT_COMMENTS = "requestorComments";
    private final static String OUT_SERIALNO = "serialNumber";
    private final static String OUT_OWNER_NAME = "ownerName";
    private final static String OUT_PUBLIC_KEY_INFO = 
        "subjectPublicKeyInfo";
    private final static String OUT_ERROR = "error";
    private final static String OUT_AUTHORITY_ID = "authorityid";

    // variables
    private IReqParser mParser = null;
    private IRequestQueue mQueue = null;
    private String mFormPath = null;
    private int mMaxReturns = 2000;

    public CMSRequest newCMSRequest() {
        return new CMSRequest();
    }

    /**
     * Constructor
     */
    public QueryReq() {
        super();
    }

    /**
     * initialize the servlet. This servlet uses the template file
     * "queryReq.template" to process the response.
     *
     * @param sc servlet configuration, read from the web.xml file
     */
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        mQueue = mAuthority.getRequestQueue();
        mFormPath = "/" + mAuthority.getId() + "/" + TPL_FILE;

        try {
            mMaxReturns = Integer.parseInt(sc.getInitParameter("maxResults"));
        } catch (Exception e) {
            /* do nothing, just use the default if integer parsing failed */
        }

        String tmp = sc.getInitParameter(PROP_PARSER);

        if (tmp != null) {
            if (tmp.trim().equals("CertReqParser.NODETAIL_PARSER"))
                mParser = CertReqParser.NODETAIL_PARSER;
            else if (tmp.trim().equals("CertReqParser.DETAIL_PARSER"))
                mParser = CertReqParser.DETAIL_PARSER;
            else if (tmp.trim().equals("KeyReqParser.PARSER"))
                mParser = KeyReqParser.PARSER;
        }			

        // override success and error templates to null - 
        // handle templates locally.
        mTemplates.remove(CMSRequest.SUCCESS);
        mTemplates.remove(CMSRequest.ERROR);

        if (mOutputTemplatePath != null)
            mFormPath = mOutputTemplatePath;
    }
	
    private String getRequestType(String p) {
        String filter = "(requestType=*)";

        if (p == null)
            return filter;
        if (p.equals(IRequest.ENROLLMENT_REQUEST)) {
            filter = "(requestType=" + IRequest.ENROLLMENT_REQUEST + ")";
        } else if (p.equals(IRequest.RENEWAL_REQUEST)) {
            filter = "(requestType=" + IRequest.RENEWAL_REQUEST + ")";
        } else if (p.equals(IRequest.REVOCATION_REQUEST)) {
            filter = "(requestType=" + IRequest.REVOCATION_REQUEST + ")";
        } else if (p.equals(IRequest.UNREVOCATION_REQUEST)) {
            filter = "(requestType=" + IRequest.UNREVOCATION_REQUEST + ")";
        } else if (p.equals(IRequest.KEYARCHIVAL_REQUEST)) {
            filter = "(requestType=" + IRequest.KEYARCHIVAL_REQUEST + ")";
        } else if (p.equals(IRequest.KEYRECOVERY_REQUEST)) {
            filter = "(requestType=" + IRequest.KEYRECOVERY_REQUEST + ")";
        } else if (p.equals(IRequest.GETCACHAIN_REQUEST)) {
            filter = "(requestType=" + IRequest.GETCACHAIN_REQUEST + ")";
        } else if (p.equals(IRequest.GETREVOCATIONINFO_REQUEST)) {
            filter = "(requestType=" + IRequest.GETREVOCATIONINFO_REQUEST + ")";
        } else if (p.equals(IRequest.GETCRL_REQUEST)) {
            filter = "(requestType=" + IRequest.GETCRL_REQUEST + ")";
        } else if (p.equals(IRequest.GETCERTS_REQUEST)) {
            filter = "(requestType=" + IRequest.GETCERTS_REQUEST + ")";
        } else if (p.equals(IRequest.NETKEY_KEYGEN_REQUEST)) {
            filter = "(requestType=" + IRequest.NETKEY_KEYGEN_REQUEST + ")";
        } else if (p.equals(IN_SHOW_ALL)) {
            filter = "(requestType=*)";
        }
        return filter;
    }

    private String getRequestState(String p) {
        String filter = "(requeststate=*)";

        if (p == null)
            return filter;
        if (p.equals(IN_SHOW_WAITING)) {
            filter = "(requeststate=pending)";
        } else if (p.equals(IN_SHOW_IN_SERVICE)) {
            filter = "(requeststate=svc_pending)";
        } else if (p.equals(IN_SHOW_PENDING)) {
            filter = "(requeststate=pending)";
        } else if (p.equals(IN_SHOW_CANCELLED)) {
            filter = "(requeststate=canceled)";
        } else if (p.equals(IN_SHOW_REJECTED)) {
            filter = "(requeststate=rejected)";
        } else if (p.equals(IN_SHOW_COMPLETED)) {
            filter = "(requeststate=complete)";
        } else if (p.equals(IN_SHOW_ALL)) {
            filter = "(requeststate=*)";
        }
        return filter;
    }

    /**
     * Process the HTTP request.
     * <ul>
     * <li>http.param reqState request state
	 *            (one of showAll, showWaiting, showInService, 
	 *            showCancelled, showRejected, showCompleted)
     * <li>http.param reqType
     * <li>http.param seqNumFromDown request ID to start at (decimal, or hex if
     *           when paging down
     *           seqNumFromDown starts with 0x)
     * <li>http.param seqNumFromUp request ID to start at (decimal, or hex if
     *           when paging up
     *           seqNumFromUp starts with 0x)
     * <li>http.param maxCount maximum number of records to show
     * <li>http.param totalCount total number of records in set of pages
     * <li>http.param direction "up", "down", "begin", or "end"
     * </ul>
     *
     * @param cmsReq the object holding the request and response information
     */

    public void process(CMSRequest cmsReq) throws EBaseException {
    	CMS.debug("in QueryReq servlet");
    	
    	// Authentication / Authorization

    	HttpServletRequest req = cmsReq.getHttpReq();
    	IAuthToken authToken = authenticate(cmsReq);
    	AuthzToken authzToken = null;
    	
    	try {
    		authzToken = authorize(mAclMethod, authToken,
    				mAuthzResourceName, "list");
    	} catch (EAuthzAccessDenied e) {
    		log(ILogger.LL_FAILURE,
    				CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
    	} catch (Exception e) {
    		log(ILogger.LL_FAILURE,
    				CMS.getLogMessage("ADMIN_SRVLT_AUTH_FAILURE", e.toString()));
    	}
    	if (authzToken == null) {
    		cmsReq.setStatus(CMSRequest.UNAUTHORIZED);
    		return;
    	}
    	


    	  	
    	CMSTemplate form = null;
    	Locale[] locale = new Locale[1];
    	
    	try {
    		// if get a EBaseException we just throw it. 
    		form = getTemplate(mFormPath, req, locale);
    	} catch (IOException e) {
    		log(ILogger.LL_FAILURE,
    				CMS.getLogMessage("CMSGW_ERR_GET_TEMPLATE", mFormPath, e.toString()));
    		throw new ECMSGWException(
    				CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
    	}
    	
    	/** 
    	 * WARNING:
    	 * 
    	 * PLEASE DO NOT TOUCH THE FILTER HERE. ALL FILTERS ARE INDEXED.
    	 *
    	 **/
    	String filter = null;
    	String reqState = req.getParameter("reqState");
    	String reqType = req.getParameter("reqType");
    	
    	if (reqState == null || reqType == null) {
    		filter = "(requeststate=*)";
    	} else if (reqState.equals(IN_SHOW_ALL) && 
    			reqType.equals(IN_SHOW_ALL)) {
    		filter = "(requeststate=*)";
    	} else if (reqState.equals(IN_SHOW_ALL)) {
    		filter = getRequestType(reqType);
    	} else if (reqType.equals(IN_SHOW_ALL)) {
    		filter = getRequestState(reqState);
    	} else {
    		filter = "(&" + getRequestState(reqState) + 
    		getRequestType(reqType) + ")";
    	}
    	
    	String direction = "begin";
    	if (req.getParameter("direction") != null) {
    		direction = req.getParameter("direction").trim();
    	}
    	
    	
    	int top=0, bottom=0;
    	
    	try {
    		String top_s = req.getParameter(OUT_FIRST_ENTRY_ON_PAGE);
    		if (top_s == null) top_s = "0";
    		
    		String bottom_s = req.getParameter(OUT_LAST_ENTRY_ON_PAGE);
    		if (bottom_s == null) bottom_s = "0";
    		
    		top = Integer.parseInt(top_s);
    		bottom = Integer.parseInt(bottom_s);
    		
    	} catch (NumberFormatException e) {

    	}
    	
    	// avoid NumberFormatException to the user interface
    	int maxCount = 10;
    	try {
    		maxCount = Integer.parseInt(req.getParameter(IN_MAXCOUNT));
    	} catch (Exception e) {
    	}
        if (maxCount > mMaxReturns) {
            CMS.debug("Resetting page size from " + maxCount + " to " + mMaxReturns);
            maxCount = mMaxReturns;
        }

    	HttpServletResponse resp = cmsReq.getHttpResp(); 
    	CMSTemplateParams argset = doSearch(locale[0],filter, maxCount, direction, top, bottom );
	
  
        argset.getFixed().addStringValue("reqType",reqType);
        argset.getFixed().addStringValue("reqState", reqState);
        argset.getFixed().addIntegerValue("maxCount",maxCount);
    	
    	
    	try {
    		form.getOutput(argset);    		
    		resp.setContentType("text/html");
    		form.renderOutput(resp.getOutputStream(), argset);
    	} catch (IOException e) {
    		log(ILogger.LL_FAILURE,
    				CMS.getLogMessage("CMSGW_ERR_STREAM_TEMPLATE", e.toString()));
    		throw new ECMSGWException(
    				CMS.getUserMessage("CMS_GW_DISPLAY_TEMPLATE_ERROR"));
    	}
    	cmsReq.setStatus(CMSRequest.SUCCESS);
    	return;
    }

    private static String makeRequestStatusEq(RequestStatus s) {
        return "(" + "requestState" + "=" + s + ")";
    }

    private static String makeRequestIdCmp(String op, int bound) {
        return "(requestId" + op + bound + ")";
    }

    /**
     * Perform search based on direction button pressed
     * @param filter ldap filter indicating which VLV to search through. This can be
     * 'all requests', 'pending', etc
     * @param count the number of requests to show per page
     * @param direction either 'begin', 'end', 'previous' or 'next' (defaults to end)
     * @param top  the number of the request shown on at the top of the current page
     * @param bottom the number of the request shown on at the bottom of the current page
     * @return 
     */
    
    private CMSTemplateParams doSearch(Locale l, String filter,
    		int count, String direction, int top, int bottom)
    {
    	CMSTemplateParams ctp = null;
    	if (direction.equals("previous")) {
    		ctp = doSearch(l, filter, -count, top-1);
    	} else if (direction.equals("next")) {
    		ctp = doSearch(l,filter, count, bottom+1);
    	} else if (direction.equals("begin")) {
    		ctp = doSearch(l,filter, count, 0);
    	} else {  // if 'direction is 'end', default here
    		ctp = doSearch(l,filter, -count, -1);
    	}
    	return ctp;
    }
    
    

	/**
	 * 
	 * @param locale
	 * @param filter the types of requests to return - this must match the VLV index
	 * @param count maximum number of records to return
	 * @param marker indication of the request ID where the page is anchored
	 * @return
	 */

    private CMSTemplateParams doSearch(
    		Locale locale,
    		String filter,
    		int count, 
    		int marker) {
    	
    	IArgBlock header = CMS.createArgBlock();
    	IArgBlock context = CMS.createArgBlock();
    	CMSTemplateParams argset = new CMSTemplateParams(header, context);
    	
    	try {
    		long startTime = CMS.getCurrentDate().getTime();
    		// preserve the type of request that we are
    		// requesting.
    		
    		header.addStringValue(OUT_AUTHORITY_ID, mAuthority.getId());
    		header.addStringValue(OUT_REQUESTING_USER, "admin");
    		
    		
    		boolean jumptoend = false;
    		if (marker == -1) {
    			marker = 0;       // I think this is inconsequential
    			jumptoend = true; // override  to '99' during search 
    		}
    		
    		RequestId id = new RequestId(Integer.toString(marker));
    		IRequestVirtualList list =   mQueue.getPagedRequestsByFilter(
    				id,
    				jumptoend,
    				filter, 
    				count+1,
    		"requestId");
    		
    		int totalCount = list.getSize() - list.getCurrentIndex();
    		header.addIntegerValue(OUT_TOTALCOUNT, totalCount);
    		header.addIntegerValue(OUT_CURRENTCOUNT, list.getSize());
    		
    		int numEntries = list.getSize() - list.getCurrentIndex();
    		
    		Vector v = fetchRecords(list,Math.abs(count));
    		v = normalizeOrder(v);
    		trim(v,id);
    		
    		
    		int currentCount = 0;
    		int curNum = 0;
    		int firstNum = -1;
    		Enumeration requests = v.elements();
    		
    		while (requests.hasMoreElements()) {
    			IRequest request = null;
    			try {
    				request = (IRequest) requests.nextElement();
    			} catch (Exception e) {
    				CMS.debug("Error displaying request:"+e.getMessage());
    				// handled below
    			}
    			if (request == null) {
    				log(ILogger.LL_WARN, "Error display request on page");
    				continue;
    			}
    			
    			curNum = Integer.parseInt(
    					request.getRequestId().toString());
    			
    			if (firstNum == -1) {
    				firstNum = curNum; 
    			}
    			
    			IArgBlock rec = CMS.createArgBlock();
    			mParser.fillRequestIntoArg(locale, request, argset, rec);
    			mQueue.releaseRequest(request);
    			argset.addRepeatRecord(rec);
    			
    			currentCount++;
    			
    		}// while
    		long endTime = CMS.getCurrentDate().getTime();
    		
    		header.addIntegerValue(OUT_CURRENTCOUNT, currentCount);
    		header.addStringValue("time", Long.toString(endTime - startTime));
    		header.addIntegerValue(OUT_FIRST_ENTRY_ON_PAGE, firstNum);
    		header.addIntegerValue(OUT_LAST_ENTRY_ON_PAGE, curNum);
    		
    	} catch (EBaseException e) {
    		header.addStringValue(OUT_ERROR, e.toString(locale));
    	} catch (Exception e) {
    	}
    	return argset;
    	
    }

    /**
     * If the vector contains the marker element at the end, remove it.
     * @param v  The vector to trim
     * @param marker  the marker to look for.
     */
	private void trim(Vector v, RequestId marker) {
		int i = v.size()-1;
		if (((IRequest)v.elementAt(i)).getRequestId().equals(marker)) {
			v.remove(i);
		}
		
	}

	/**
	 * Sometimes the list comes back from LDAP in reverse order. This function makes
	 * sure the results are in 'forward' order.
	 * @param list
	 * @return
	 */
    private Vector fetchRecords(IRequestVirtualList list, int maxCount) {
    	
    	Vector v = new Vector();
    	int count = list.getSize();
    	int c=0;
    	for (int i=0; i<count; i++) {
    		IRequest request = list.getElementAt(i);
    		if (request != null) {
    		   v.add(request);
    		   c++;
    		}
    		if (c >= maxCount) break;
    	}
    	
    	return v;

    }

    /**
     * If the requests are in backwards order, reverse the list
     * @param list
     * @return
     */
    private Vector normalizeOrder(Vector list) {
    	
    	int firstrequestnum = Integer.parseInt(((IRequest) list.elementAt(0))
    			.getRequestId().toString());
    	int lastrequestnum = Integer.parseInt(((IRequest) list.elementAt(list
    			.size() - 1)).getRequestId().toString());
    	boolean reverse = false;
    	if (firstrequestnum > lastrequestnum) {
    		reverse = true; // if the order is backwards, place items at the beginning
    	}
    	Vector v = new Vector();
    	int count = list.size();
    	for (int i = 0; i < count; i++) {
    		Object request = list.elementAt(i);
    		if (request != null) {
    			if (reverse)
    				v.add(0, request);
    			else
    				v.add(request);
    		}
    	}
    	
    	return v;
    }
}
