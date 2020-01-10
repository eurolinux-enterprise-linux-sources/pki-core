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
package com.netscape.cms.logging;


import java.io.*;
import java.util.*;
import java.text.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.*;


/**
 * A log entry of LogFile
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class LogEntry {
    private String mEntry;
    private String mLevel;
    private String mSource;
    private String mDetail;
    private String mDate;
    private String mTime;
    private Vector mRow;

    private final String DATE_PATTERN = "dd/MMM/yyyy:HH:mm:ss z";

    /**
     * Constructor for a LogEntry.
     *
     */
    public LogEntry(String entry) throws ParseException {
        mEntry = entry;
        mRow = parse();
    }

    /**
     * parse a log entry
     *
     * return a vector of the segments of the entry
     */
     
    public Vector parse() throws ParseException {
        int x = mEntry.indexOf("[");

        if (x == -1)
            throw new ParseException(mEntry, 0);
        String temp = mEntry.substring(x + 1);

        x = temp.indexOf("]");
        if (x == -1)
            throw new ParseException(mEntry, 0);

        String dateStr = temp.substring(0, x);
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        Date date = format.parse(dateStr);

        mDate = DateFormat.getDateInstance().format(date);
        mTime = DateFormat.getTimeInstance().format(date);

        temp = temp.substring(x + 2);
        x = temp.indexOf("]");
        if (x == -1)
            throw new ParseException(mEntry, 0);
        mSource = temp.substring(1, x);

        temp = temp.substring(x + 2);
        x = temp.indexOf("]");
        if (x == -1)
            throw new ParseException(mEntry, 0);
        mLevel = temp.substring(1, x);

        mDetail = temp.substring(x + 2);

        Vector row = new Vector();

        row.addElement(mSource);
        row.addElement(mLevel);
        row.addElement(mDate);
        row.addElement(mTime);
        row.addElement(mDetail);

        //System.out.println(mSource +"," + mLevel +","+ mDate+","+mTime+","+mDetail);
        return row;

    }

    public String getSource() {
        return mSource;
    }

    public String getLevel() {
        return mLevel;
    }

    public String getDetail() {
        return mDetail;
    }

    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public Vector getRow() {
        return mRow;
    }

    public String getEntry() {
        return mEntry;
    }

    public void appendDetail(String msg) {
        mDetail = mDetail + "\n" + msg;
        mEntry = mEntry + "\n" + msg;
    }
}
