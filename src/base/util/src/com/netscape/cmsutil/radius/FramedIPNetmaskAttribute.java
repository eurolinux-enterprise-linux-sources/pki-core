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
package com.netscape.cmsutil.radius;

import java.io.IOException;

public class FramedIPNetmaskAttribute extends Attribute {
    private byte _mask[] = new byte[4];

    public FramedIPNetmaskAttribute(byte value[]) {
        super();
        _t = FRAMED_IP_NETMASK;
        _mask[0] = value[2];
        _mask[1] = value[3];
        _mask[2] = value[4];
        _mask[3] = value[5];
    }

    public byte[] getValue() throws IOException {
        return _mask;
    }
}
