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
package com.netscape.cms.crl;


import java.io.*;
import java.util.*;
import netscape.security.x509.PKIXExtensions;
import netscape.security.x509.CRLExtensions;
import netscape.security.x509.Extension;
import netscape.security.x509.HoldInstructionExtension;
import netscape.security.util.ObjectIdentifier;
import com.netscape.certsrv.base.IConfigStore;
import com.netscape.certsrv.base.IExtendedPluginInfo;
import com.netscape.certsrv.base.EBaseException;
import com.netscape.certsrv.base.EPropertyNotFound;
import com.netscape.certsrv.common.NameValuePairs;
import com.netscape.certsrv.dbs.crldb.*;
import com.netscape.certsrv.ca.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.*;


/**
 * This represents a hold instruction extension.
 *
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $
 */
public class CMSHoldInstructionExtension
    implements ICMSCRLExtension, IExtendedPluginInfo {
    public static final String PROP_INSTR = "instruction";
    public static final String PROP_INSTR_NONE = "none";
    public static final String PROP_INSTR_CALLISSUER = "callissuer";
    public static final String PROP_INSTR_REJECT = "reject";

    private ILogger mLogger = CMS.getLogger();

    public CMSHoldInstructionExtension() {
    }

    public Extension setCRLExtensionCriticality(Extension ext,
        boolean critical) {
        HoldInstructionExtension holdInstrExt = null;

        try {
            ObjectIdentifier holdInstr =
                ((HoldInstructionExtension) ext).getHoldInstructionCode();

            holdInstrExt = new HoldInstructionExtension(Boolean.valueOf(critical),
                        holdInstr);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CRL_CREATE_HOLD_INSTR_EXT", e.toString()));
        }
        return holdInstrExt;
    }

    public Extension getCRLExtension(IConfigStore config,
        Object ip,
        boolean critical) {
        HoldInstructionExtension holdInstrExt = null;
        String instruction = null;

        ICRLIssuingPoint crlIssuingPoint = (ICRLIssuingPoint) ip;

        try {
            instruction = config.getString(PROP_INSTR);
        } catch (EPropertyNotFound e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CRL_CREATE_HOLD_UNDEFINED", e.toString()));
        } catch (EBaseException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CRL_CREATE_HOLD_INVALID", e.toString()));
        }

        ObjectIdentifier holdInstr = HoldInstructionExtension.NONE_HOLD_INSTR_OID;

        if (instruction != null) {
            if (instruction.equalsIgnoreCase(PROP_INSTR_CALLISSUER)) {
                holdInstr = HoldInstructionExtension.CALL_ISSUER_HOLD_INSTR_OID;
            } else if (instruction.equalsIgnoreCase(PROP_INSTR_REJECT)) {
                holdInstr = HoldInstructionExtension.REJECT_HOLD_INSTR_OID;
            }
        }
        try {
            holdInstrExt = new HoldInstructionExtension(Boolean.valueOf(critical),
                        holdInstr);
        } catch (IOException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CRL_CREATE_HOLD_INSTR_EXT", e.toString()));
        }

        return holdInstrExt;
    }

    public String getCRLExtOID() {
        return PKIXExtensions.HoldInstructionCode_Id.toString();
    }

    public void getConfigParams(IConfigStore config, NameValuePairs nvp) {
        String instruction = null;

        try {
            instruction = config.getString(PROP_INSTR);
        } catch (EPropertyNotFound e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CRL_CREATE_HOLD_UNDEFINED", e.toString()));
        } catch (EBaseException e) {
            log(ILogger.LL_FAILURE, CMS.getLogMessage("CRL_CREATE_HOLD_INVALID", e.toString()));
        }
        if (instruction != null) {
            if (!(instruction.equalsIgnoreCase(PROP_INSTR_NONE) ||
                    instruction.equalsIgnoreCase(PROP_INSTR_CALLISSUER) ||
                    instruction.equalsIgnoreCase(PROP_INSTR_REJECT))) {
                instruction = PROP_INSTR_NONE;
            }
        } else {
            instruction = PROP_INSTR_NONE;
        }
        nvp.add(PROP_INSTR, instruction);
    }

    public String[] getExtendedPluginInfo(Locale locale) {
        String[] params = {
                //"type;choice(CRLExtension,CRLEntryExtension);"+
                //"CRL Entry Extension type. This field is not editable.",
                "enable;boolean;Check to enable Hold Instruction CRL entry extension.",
                "critical;boolean;Set criticality for Hold Instruction CRL entry extension.",
                PROP_INSTR + ";choice(" + PROP_INSTR_NONE + "," + PROP_INSTR_CALLISSUER + "," +
                PROP_INSTR_REJECT + ");Select hold instruction code.",
                IExtendedPluginInfo.HELP_TOKEN +
                ";configuration-ca-edit-crlextension-holdinstruction",
                IExtendedPluginInfo.HELP_TEXT +
                ";The hold instruction code is a non-critical CRL entry " +
                "extension that provides a registered instruction identifier " +
                "which indicates the action to be taken after encountering " +
                "a certificate that has been placed on hold."
            };

        return params;
    }

    private void log(int level, String msg) {
        mLogger.log(ILogger.EV_SYSTEM, null, ILogger.S_CA, level,
            "CMSHoldInstructionExtension - " + msg);
    }
} 
