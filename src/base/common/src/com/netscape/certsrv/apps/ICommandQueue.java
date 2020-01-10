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
package com.netscape.certsrv.apps;


import java.util.Hashtable;
import java.util.Enumeration;
import java.util.TimeZone;
import  com.netscape.certsrv.apps.*;


/**
 * This interface represents a command queue for registeration
 * and unregisteration proccess for clean shutdown
 * 
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public interface ICommandQueue {

    /**
     * Registers a thread into the command queue.
     *
     * @param currentRequest request object
     * @param currentServlet servlet that serves the request object
     */
    public boolean registerProcess(Object currentRequest, Object currentServlet);
    /**
     * UnRegisters a thread from the command queue.
     *
     * @param currentRequest request object
     * @param currentServlet servlet that serves the request object
     */
    public void unRegisterProccess(Object currentRequest, Object currentServlet);
            
} // CommandQueue
