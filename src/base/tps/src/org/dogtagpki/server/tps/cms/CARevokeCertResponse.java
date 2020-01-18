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
// (C) 2014 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---

package org.dogtagpki.server.tps.cms;

import java.util.Hashtable;

import org.dogtagpki.server.connector.IRemoteRequest;

/**
 * CARevokeCertResponse is the class for the response to
 * CA Remote Request: revoteCertificate()
 *
 */
public class CARevokeCertResponse extends RemoteResponse
{
    public CARevokeCertResponse(Hashtable<String, Object> ht) {
        nameValTable = ht;
    }

    public CARevokeCertResponse(String connid, Hashtable<String, Object> ht) {
        setConnID(connid);
        nameValTable = ht;
    }

    public String getErrorString() {
        return (String) nameValTable.get(IRemoteRequest.RESPONSE_ERROR_STRING);
    }
}
