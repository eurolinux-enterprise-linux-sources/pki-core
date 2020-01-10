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
package com.netscape.certsrv.request;


import java.util.*;
import java.math.*;

import com.netscape.certsrv.base.EBaseException;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.request.*;
import com.netscape.certsrv.ldap.*;
import com.netscape.certsrv.logging.ILogger;
import com.netscape.certsrv.ca.ICertificateAuthority;
import com.netscape.certsrv.publish.IPublisherProcessor;

/**
 * The ARequestNotifier class implements the IRequestNotifier interface,
 * which notifies all registered request listeners.
 *
 * @version $Revision: 1665 $, $Date: 2010-12-22 16:35:59 -0800 (Wed, 22 Dec 2010) $
 */
public class ARequestNotifier implements IRequestNotifier {
    private Hashtable mListeners = new Hashtable();
    private Vector mNotifierThreads = new Vector();
    private Vector mRequests = new Vector();
    private int mMaxRequests = 100;
    private boolean mSearchForRequests = false;
    private int mMaxThreads = 1;
    private ICertificateAuthority mCA = null;
    private boolean mIsPublishingQueueEnabled = false;
    private int mPublishingQueuePriority = 0;
    private int mMaxPublishingQueuePageSize = 1;
    private IRequestQueue mRequestQueue = null;
    private String mPublishingStatus = null;
    private int mSavePublishingStatus = 0;
    private int mSavePublishingCounter = 0;


    public ARequestNotifier() {
        mPublishingQueuePriority = Thread.currentThread().getPriority();
    }

    public ARequestNotifier (ICertificateAuthority ca) {
        mCA = ca;
        if (mCA != null) mRequestQueue = mCA.getRequestQueue();
    }

    public void setPublishingQueue (boolean isPublishingQueueEnabled,
                                    int publishingQueuePriorityLevel,
                                    int maxNumberOfPublishingThreads,
                                    int publishingQueuePageSize,
                                    int savePublishingStatus) {
        CMS.debug("setPublishingQueue:  Publishing Queue Enabled: " + isPublishingQueueEnabled+
                  "  Priority Level: " + publishingQueuePriorityLevel+
                  "  Maximum Number of Threads: " + maxNumberOfPublishingThreads+
                  "  Page Size: "+ publishingQueuePageSize);
        mIsPublishingQueueEnabled = isPublishingQueueEnabled;
        mMaxThreads = maxNumberOfPublishingThreads;
        mMaxRequests = publishingQueuePageSize;
        mSavePublishingStatus = savePublishingStatus;

        // Publishing Queue Priority Levels:  2 - maximum, 1 - higher, 0 - normal, -1 - lower, -2 - minimum
        if (publishingQueuePriorityLevel > 1) {
            mPublishingQueuePriority = Thread.MAX_PRIORITY;
        } else if (publishingQueuePriorityLevel > 0) {
            mPublishingQueuePriority = (Thread.currentThread().getPriority() + Thread.MAX_PRIORITY) / 2;
        } else if (publishingQueuePriorityLevel < -1) {
            mPublishingQueuePriority = Thread.MIN_PRIORITY;
        } else if (publishingQueuePriorityLevel < 0) {
            mPublishingQueuePriority = (Thread.currentThread().getPriority() + Thread.MIN_PRIORITY) / 2;
        } else {
            mPublishingQueuePriority = Thread.currentThread().getPriority();
        }

        if (mCA != null && mRequestQueue == null) mRequestQueue = mCA.getRequestQueue();
        if (mIsPublishingQueueEnabled && mSavePublishingStatus > 0 && mRequestQueue != null) {
            mPublishingStatus = mRequestQueue.getPublishingStatus();
            BigInteger status = new BigInteger("-2");
            try {
                status = new BigInteger(mPublishingStatus);
                if (status.compareTo(BigInteger.ZERO) > -1) {
                    recoverPublishingQueue(mPublishingStatus);
                }
            } catch (Exception e) {
            }
        }
        
    }

    /**
     * Registers a request listener.
     *
     * @param listener listener to be registered
     */
    public void registerListener(IRequestListener listener) {
        // XXX should check for duplicates here or allow listeners
        // to register twice and call twice ? 
        mListeners.put(listener.getClass().getName(), listener);
    }

    /**
     * Registers a request listener.
     *
     * @param name listener name
     * @param listener listener to be registered
     */
    public void registerListener(String name, IRequestListener listener) {
        mListeners.put(name, listener);
    }

    /**
     * Removes listener from the list of registered listeners.
     *
     * @param listener listener to be removed from the list
     */
    public void removeListener(IRequestListener listener) {
        // XXX should check for duplicates here or allow listeners
        // to register twice and call twice ? 
        mListeners.remove(listener.getClass().getName());
    }

    /**
     * Gets list of listener names.
     *
     * @return enumeration of listener names
     */
    public Enumeration getListenerNames() {
        return mListeners.keys();
    }

    /**
     * Removes listener from the list of registered listeners.
     *
     * @param name listener name to be removed from the list
     */
    public void removeListener(String name) {
        mListeners.remove(name);
    }

    /**
     * Gets listener from the list of registered listeners.
     *
     * @param name listener name
     * @return listener
     */
    public IRequestListener getListener(String name) {
        return (IRequestListener) mListeners.get(name);
    }

    /**
     * Gets list of listeners.
     *
     * @return enumeration of listeners
     */
    public Enumeration getListeners() {
        return mListeners.elements();
    }


    private Object publishingCounterMonitor = new Object();

    public void updatePublishingStatus(String id) {
        if (mRequestQueue != null) {
            synchronized (publishingCounterMonitor) {
                if (mSavePublishingCounter == 0) {
                    CMS.debug("updatePublishingStatus  requestId: "+id);
                    mRequestQueue.setPublishingStatus(id);
                }
                mSavePublishingCounter++;
                CMS.debug("updatePublishingStatus  mSavePublishingCounter: "+mSavePublishingCounter+
                          " mSavePublishingStatus: "+mSavePublishingStatus);
                if (mSavePublishingCounter >= mSavePublishingStatus) {
                    mSavePublishingCounter = 0;
                }
            }
        } else {
            CMS.debug("updatePublishingStatus  mRequestQueue == null");
        }
    }

    /**
     * Gets request from publishing queue.
     *
     * @return request
     */
    public synchronized IRequest getRequest() {
       IRequest r = null;
       String id = null;

        CMS.debug("getRequest  mRequests=" + mRequests.size() + "  mSearchForRequests=" + mSearchForRequests);
        if (mSearchForRequests && mRequests.size() == 1) {
            id = (String)mRequests.elementAt(0);
            if (mCA != null && mRequestQueue == null) mRequestQueue = mCA.getRequestQueue();
            if (id != null && mRequestQueue != null) {
                CMS.debug("getRequest  request id=" + id);
                IRequestVirtualList list = mRequestQueue.getPagedRequestsByFilter(
                                               new RequestId(id),
                                               "(requeststate=complete)", mMaxRequests, "requestId");
                int s = list.getSize() - list.getCurrentIndex();
                CMS.debug("getRequest  list size: "+s);
                for (int i = 0; i < s; i++) {
                    r = null;
                    try {
                        r = list.getElementAt(i);
                    } catch (Exception e) {
                        // handled below
                    }
                    if (r == null) {
                        continue;
                    }
                    String requestType = r.getRequestType();
                    if (requestType == null) {
                        continue;
                    }
                    if (!(requestType.equals(IRequest.ENROLLMENT_REQUEST) ||
                          requestType.equals(IRequest.RENEWAL_REQUEST) ||
                          requestType.equals(IRequest.REVOCATION_REQUEST) ||
                          requestType.equals(IRequest.CMCREVOKE_REQUEST) ||
                          requestType.equals(IRequest.UNREVOCATION_REQUEST))) {
                        continue;
                    }
                    if (i == 0 && id.equals(r.getRequestId().toString())) {
                        if (s == 1) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (mRequests.size() < mMaxRequests) {
                        mRequests.addElement(r.getRequestId().toString());
                        CMS.debug("getRequest  added "+r.getRequestType()+" request "+r.getRequestId().toString()+
                                  " to mRequests: " + mRequests.size()+" ("+mMaxRequests+")");
                    } else {
                        break;
                    }
                }
                CMS.debug("getRequest  done with adding requests to mRequests: " + mRequests.size());
            } else {
                CMS.debug("getRequest  has no access to the request queue");
            }
        }
        if (mRequests.size() > 0) {
            id = (String)mRequests.elementAt(0);
            if (id != null) {
                CMS.debug("getRequest  getting request: " + id);
                if (mCA != null && mRequestQueue == null) mRequestQueue = mCA.getRequestQueue();
                if (mRequestQueue != null) {
                    try {
                        r = mRequestQueue.findRequest(new RequestId(id));
                        mRequests.remove(0);
                        CMS.debug("getRequest  request "+ id + ((r != null)?" found":" not found"));
                        //updatePublishingStatus(id);
                    } catch (EBaseException e) {
                        CMS.debug("getRequest  EBaseException " + e.toString());
                    }
                } else {
                    CMS.debug("getRequest  has no access to the request queue");
                }
            }
            if (mRequests.size() == 0) {
                mSearchForRequests = false;
            }
        }
        CMS.debug("getRequest  mRequests=" + mRequests.size() + "  mSearchForRequests=" + mSearchForRequests + " done");

        return r;
    }

    /**
     * Gets number of requests in publishing queue.
     *
     * @return number of requests in publishing queue
     */
    public int getNumberOfRequests() {
        return mRequests.size();
    }

    /**
     * Checks if publishing queue is enabled.
     *
     * @return true if publishing queue is enabled, false otherwise
     */
    public boolean isPublishingQueueEnabled() {
        return mIsPublishingQueueEnabled;
    }

    /**
     * Removes a notifier thread from the pool of publishing queue threads.
     *
     * @param notifierThread Thread
     */
    public void removeNotifierThread(Thread notifierThread) {
        if (mNotifierThreads.size() > 0) {
            mNotifierThreads.remove(notifierThread);
            if (mNotifierThreads.size() == 0) {
                mRequestQueue.setPublishingStatus("-1");
            }
        }
        CMS.debug("Number of publishing threads: " + mNotifierThreads.size());
    }

    /**
     * Notifies all registered listeners about request.
     *
     * @param r request
     */
    public void notify(IRequest r) {
        CMS.debug("ARequestNotifier  notify mIsPublishingQueueEnabled="+mIsPublishingQueueEnabled+
                  " mMaxThreads="+mMaxThreads);
        if (mIsPublishingQueueEnabled) {
            addToNotify(r);
        } else if (mMaxThreads == 0) {
            Enumeration listeners = mListeners.elements();
            if (listeners != null && r != null) {
                while (listeners.hasMoreElements()) {
                    IRequestListener l = (IRequestListener) listeners.nextElement();
                    CMS.debug("RunListeners: IRequestListener = " + l.getClass().getName());
                    l.accept(r);
                }
            }
        } else {
            // spawn a seperate thread to call the listeners and return.
            try {
                new Thread(new RunListeners(r, mListeners.elements())).start();
            } catch (Throwable e) {

            /*
             CMS.getLogger().log(
             ILogger.EV_SYSTEM, ILogger.S_REQQUEUE, ILogger.LL_FAILURE, 
             "Could not run listeners for request " + r.getRequestId() +
             ". Error " + e + ";" + e.getMessage());
             */
            }
        }
    }

    /**
     * Checks for available publishing connections
     *
     * @return true if there are available publishing connections, false otherwise
     */
    private boolean checkAvailablePublishingConnections() {
        boolean availableConnections = false;

        IPublisherProcessor pp = null;
        if (mCA != null) pp = mCA.getPublisherProcessor();
        if (pp != null && pp.enabled()) {
            ILdapConnModule ldapConnModule = pp.getLdapConnModule();
            if (ldapConnModule != null) {
                ILdapConnFactory ldapConnFactory = ldapConnModule.getLdapConnFactory();
                if (ldapConnFactory != null) {
                    CMS.debug("checkAvailablePublishingConnections  maxConn: " + ldapConnFactory.maxConn() +
                                                               "  totalConn: " + ldapConnFactory.totalConn());
                    if (ldapConnFactory.maxConn() > ldapConnFactory.totalConn()) {
                        availableConnections = true;
                    }
                } else {
                    CMS.debug("checkAvailablePublishingConnections  ldapConnFactory is not accessible");
                }
            } else {
                CMS.debug("checkAvailablePublishingConnections  ldapConnModule is not accessible");
            }
        } else {
            CMS.debug("checkAvailablePublishingConnections  PublisherProcessor is not " + 
                      ((pp != null)?"enabled":"accessible"));
        }

        return availableConnections;
    }

    /**
     * Checks if more publishing threads can be added.
     *
     * @return true if more publishing threads can be added, false otherwise
     */
    private boolean morePublishingThreads() {
        boolean moreThreads = false;

        if (mNotifierThreads.size() == 0) {
            moreThreads = true;
        } else if (mNotifierThreads.size() < mMaxThreads) {
            CMS.debug("morePublishingThreads  ("+mRequests.size()+">"+
                      ((mMaxRequests * mNotifierThreads.size()) / mMaxThreads)+
                      " "+"("+mMaxRequests+"*"+mNotifierThreads.size()+"):"+mMaxThreads);
            // gradually add new publishing threads
            if (mRequests.size() > ((mMaxRequests * mNotifierThreads.size()) / mMaxThreads)) {
                // check for available publishing connections
                if (checkAvailablePublishingConnections()) {
                    moreThreads = true;
                }
            }
        }
        CMS.debug("morePublishingThreads  moreThreads: " + moreThreads);

        return moreThreads;
    }


    /**
     * Notifies all registered listeners about request.
     *
     * @param r request
     */
    public synchronized void addToNotify(IRequest r) {
        if (!mSearchForRequests) {
            if (mRequests.size() < mMaxRequests) {
                mRequests.addElement(r.getRequestId().toString());
                CMS.debug("addToNotify  extended buffer to "+mRequests.size()+"("+mMaxRequests+")"+
                          " requests by adding request "+r.getRequestId().toString());
                if (morePublishingThreads()) {
                    try {
                        Thread notifierThread = new Thread(new RunListeners((IRequestNotifier)this));
                        if (notifierThread != null) {
                            mNotifierThreads.addElement(notifierThread);
                            CMS.debug("Number of publishing threads: " + mNotifierThreads.size());
                            if (mPublishingQueuePriority > 0) {
                                notifierThread.setPriority(mPublishingQueuePriority);
                            }
                            notifierThread.start();
                        }
                    } catch (Throwable e) {
                        CMS.debug("addToNotify  exception: " + e.toString());
                    }
                }
            } else {
                mSearchForRequests = true;
            }
        }
    }


    /**
     * Recovers publishing queue.
     *
     * @param id request request
     */
    public void recoverPublishingQueue(String id) {
        CMS.debug("recoverPublishingQueue  mRequests.size()="+mRequests.size()+"("+mMaxRequests+")"+
                      " requests by adding request "+id);
        if (mRequests.size() == 0) {
            mRequests.addElement(id);
            CMS.debug("recoverPublishingQueue  extended buffer to "+mRequests.size()+"("+mMaxRequests+")"+
                      " requests by adding request "+id);
            if (morePublishingThreads()) {
                mSearchForRequests = true;
                try {
                    Thread notifierThread = new Thread(new RunListeners((IRequestNotifier)this));
                    if (notifierThread != null) {
                        mNotifierThreads.addElement(notifierThread);
                        CMS.debug("Number of publishing threads: " + mNotifierThreads.size());
                        if (mPublishingQueuePriority > 0) {
                            notifierThread.setPriority(mPublishingQueuePriority);
                        }
                        notifierThread.start();
                    }
                } catch (Throwable e) {
                    CMS.debug("recoverPublishingQueue  exception: " + e.toString());
                }
            }
        }
    }
}


/**
 * The RunListeners class implements Runnable interface.
 * This class executes notification of registered listeners.
 */
class RunListeners implements Runnable {
    IRequest mRequest = null;
    Enumeration mListeners = null;
    IRequestNotifier mRequestNotifier = null;

    /**
     * RunListeners class constructor.
     *
     * @param r request
     * @param listeners list of listeners
     */
    public RunListeners(IRequest r, Enumeration listeners) {
        mRequest = r;
        mListeners = listeners;
    }

    /**
     * RunListeners class constructor.
     *
     * @param r request
     * @param listeners list of listeners
     */
    public RunListeners(IRequestNotifier requestNotifier) {
        mRequestNotifier = requestNotifier;
        mListeners = mRequestNotifier.getListeners();
    }

    /**
     * RunListeners thread implementation.
     */
    public void run() {
        CMS.debug("RunListeners::"+((mRequestNotifier != null && mRequestNotifier.getNumberOfRequests() > 0)?" Queue: "+mRequestNotifier.getNumberOfRequests():" noQueue")+
                  " "+((mRequest != null)?" SingleRequest":" noSingleRequest"));
        do {
            if (mRequestNotifier != null) mRequest = (IRequest)mRequestNotifier.getRequest();
            if (mListeners != null && mRequest != null) {
                while (mListeners.hasMoreElements()) {
                    IRequestListener l = (IRequestListener) mListeners.nextElement();
                    CMS.debug("RunListeners: IRequestListener = " + l.getClass().getName());
                    l.accept(mRequest);
                }
                if (mRequestNotifier != null) {
                    CMS.debug("RunListeners: mRequest = " + mRequest.getRequestId().toString());
                    mRequestNotifier.updatePublishingStatus(mRequest.getRequestId().toString());
                }
            }
            CMS.debug("RunListeners: "+((mRequestNotifier != null && mRequestNotifier.getNumberOfRequests() > 0)?" Queue: "+mRequestNotifier.getNumberOfRequests():" noQueue")+
                      " "+((mRequest != null)?" SingleRequest":" noSingleRequest"));
            if (mRequestNotifier != null) mListeners = mRequestNotifier.getListeners();
        } while (mRequestNotifier != null && mRequestNotifier.getNumberOfRequests() > 0);

        if (mRequestNotifier != null) mRequestNotifier.removeNotifierThread(Thread.currentThread());
    }
}
