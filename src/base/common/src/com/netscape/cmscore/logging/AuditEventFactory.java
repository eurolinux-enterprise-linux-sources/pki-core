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


import java.util.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.*;


/**
 * A log event object for handling audit messages
 * <P>
 *
 * @author mikep 
 * @author mzhao
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class AuditEventFactory implements ILogEventFactory {

    /**
     * List of supported properties.
     */
    public static final String PROP_BUNDLE = "bundleName";

    /**
     * Constructs a audit event factory.
     */
    public AuditEventFactory() {
    }

    /**
     * Creates an log event.
     * 
     * @param evtClass the event type
     * @param prop the resource bundle
     * @param source the subsystem ID who creates the log event
     * @param level the severity of the log event
     * @param multiline the log message has more than one line or not
     * @param msg the detail message of the log
     * @param params the parameters in the detail log message
     */
    public ILogEvent create(int evtClass, Properties prop, int source,
        int level, boolean multiline, String msg, Object params[]) {
        if (evtClass != ILogger.EV_AUDIT)
            return null;
        AuditEvent event = new AuditEvent(msg, params);

        event.setLevel(level);
        event.setSource(source);
        event.setMultiline(multiline);
        setProperties(prop, event);
        return event;
    }

    /**
     * Set the resource bundle of the log event.
     *
     * @param prop the properties 
     * @param event the log event
     */
    protected void setProperties(Properties prop, IBundleLogEvent event) {
        if (prop == null) {
            event.setBundleName(null);
        } else {
            String bundleName = (String) prop.get(PROP_BUNDLE);

            if (bundleName != null) {
                event.setBundleName(bundleName);
            }
        }
    }

    /**
     * Releases an log event.
     *
     * @param e the log event
     */
    public void release(ILogEvent e) {
        // do nothing
    }
}
