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
package netscape.security.x509;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.DSAParams;

import netscape.security.util.*;


/**
 * This class identifies DSS/DSA Algorithm variants, which are distinguished
 * by using different algorithm parameters <em>P, Q, G</em>.  It uses the
 * NIST/IETF standard DER encoding.  These are used to implement the Digital
 * Signature Standard (DSS), FIPS 186.
 *
 * <P><em><b>NOTE:</b>  At this time, DSS/DSA Algorithm IDs must always
 * include these parameters.  Use of DSS/DSA in modes where parameters are
 * either implicit (e.g. a default applicable to a site or a larger scope),
 * or are derived from some Certificate Authority's DSS certificate, is
 * not currently supported. </em>
 *
 * @version 1.31
 * @author David Brownell
 */
public final
class AlgIdDSA extends AlgorithmId implements DSAParams
{
    /*
     * The three unsigned integer parameters.
     */
    private BigInteger	p , q, g;

    /** Returns the DSS/DSA parameter "P" */
    public BigInteger	getP () { return p; }

    /** Returns the DSS/DSA parameter "Q" */
    public BigInteger	getQ () { return q; }

    /** Returns the DSS/DSA parameter "G" */
    public BigInteger	getG () { return g; }

    /**
     * Default constructor.  The OID and parameters must be
     * deserialized before this algorithm ID is used.
     */
    // XXX deprecated for general use
    public AlgIdDSA () {}

    AlgIdDSA (DerValue val) throws IOException
	{ super(val.getOID()); }

    /**
     * Construct an AlgIdDSA from an X.509 encoded byte array. 
     */
    public AlgIdDSA (byte[] encodedAlg) throws IOException
	{ super (new DerValue(encodedAlg).getOID()); }

    /**
     * Constructs a DSS/DSA Algorithm ID from unsigned integers that
     * define the algorithm parameters.  Those integers are encoded
     * as big-endian byte arrays.
     *
     * @param p the DSS/DSA paramter "P"
     * @param q the DSS/DSA paramter "Q"
     * @param g the DSS/DSA paramter "G"
     */
    public AlgIdDSA (byte p [], byte q [], byte g [])
    throws IOException
    {
        this (new BigInteger (1, p),
	    new BigInteger (1, q),
	    new BigInteger (1, g));
    }

    /**
     * Constructs a DSS/DSA Algorithm ID from numeric parameters.
     *
     * @param p the DSS/DSA paramter "P"
     * @param q the DSS/DSA paramter "Q"
     * @param g the DSS/DSA paramter "G"
     */
    public AlgIdDSA (BigInteger p, BigInteger q, BigInteger g)
    {
        super (DSA_oid);

	try {
	    this.p = p;
	    this.q = q;
	    this.g = g;
	    initializeParams ();

	} catch (IOException e) {
	    /* this should not happen */
	    throw new ProviderException ("Construct DSS/DSA Algorithm ID");
	}
    }

    /**
     * Returns "DSA", indicating the Digital Signature Algorithm (DSA) as
     * defined by the Digital Signature Standard (DSS), FIPS 186.
     */
    public String getName ()
	{ return "DSA"; }


    /*
     * For algorithm IDs which haven't been created from a DER encoded
     * value, "params" must be created.
     */
    private void initializeParams ()
    throws IOException
    {
	DerOutputStream	out = new DerOutputStream ();

	out.putInteger (new BigInt(p.toByteArray()));
	out.putInteger (new BigInt(q.toByteArray()));
	out.putInteger (new BigInt(g.toByteArray()));
	params = new DerValue (DerValue.tag_Sequence,out.toByteArray ());
    }

    /**
     * Parses algorithm parameters P, Q, and G.  They're found
     * in the "params" member, which never needs to be changed.
     */
    protected void decodeParams () 
    throws IOException
    {
	if (params == null || params.tag != DerValue.tag_Sequence)
	    throw new  IOException("DSA alg parsing error");

	params.data.reset ();

	this.p = params.data.getInteger ().toBigInteger();
	this.q = params.data.getInteger ().toBigInteger();
	this.g = params.data.getInteger ().toBigInteger();

	if (params.data.available () != 0)
	    throw new IOException ("AlgIdDSA params, extra="+
				   params.data.available ());
    }


    /*
     * Returns a formatted string describing the parameters.
     */
    public String toString ()
	{ return paramsToString (); }

    /*
     * Returns a string describing the parameters.
     */
    protected String paramsToString ()
    {
	return  
	    "\n    p:\n" + (new BigInt(p)).toString() +
	    "\n    q:\n" + (new BigInt(q)).toString() +
	    "\n    g:\n" + (new BigInt(g)).toString() +
	    "\n";
    }
}
