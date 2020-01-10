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
package com.netscape.cmsutil.ocsp;

import java.io.*;
import org.mozilla.jss.asn1.*;
import java.security.Signer;
import org.mozilla.jss.asn1.OCTET_STRING;
import org.mozilla.jss.asn1.BIT_STRING;
import org.mozilla.jss.pkix.primitive.AlgorithmIdentifier;
import org.mozilla.jss.pkix.cert.Certificate;

/**
 * RFC 2560:
 *
 * <pre>
 * BasicOCSPResponse       ::= SEQUENCE {
 *    tbsResponseData      ResponseData,
 *    signatureAlgorithm   AlgorithmIdentifier,
 *    signature            BIT STRING,
 *    certs                [0] EXPLICIT SEQUENCE OF Certificate OPTIONAL }
 * </pre>
 *
 * @version $Revision: 1482 $ $Date: 2010-11-08 21:42:13 -0800 (Mon, 08 Nov 2010) $
 */
public class BasicOCSPResponse implements Response
{
    private byte mData[] = null;
    private ResponseData _rd = null;
    private AlgorithmIdentifier _signAlg = null;
    private BIT_STRING _signature = null;
    private Certificate _certs[] = null;

    public BasicOCSPResponse(ResponseData rd, AlgorithmIdentifier signAlg,
            BIT_STRING signature, Certificate certs[])
    {
        _rd = rd;
        _signAlg = signAlg;
        _signature = signature;
        _certs = certs;
    }

    public BasicOCSPResponse(OCTET_STRING os)
    {
        this(os.toByteArray());
    }

    public BasicOCSPResponse(byte data[])
    {
        mData = data;

        // extract _rd, _signAlg, _signature and _certs
        try {
            BasicOCSPResponse resp = (BasicOCSPResponse) getTemplate().decode(new ByteArrayInputStream(data));
            _rd = resp.getResponseData();
            _signAlg = resp.getSignatureAlgorithm();
            _signature = resp.getSignature();
            _certs = resp.getCerts();
         } catch (Exception e) {
            // exception in decoding byte data
         } 
    }

    private static final Tag TAG = SEQUENCE.TAG;

    public Tag getTag()
    {
        return TAG;
    }

    public void encode(Tag t, OutputStream os) throws IOException
    {
       if (mData != null) {
           os.write(mData);
       } else {
            SEQUENCE seq = new SEQUENCE();
            seq.addElement(_rd);
            seq.addElement(_signAlg);
            seq.addElement(_signature);
            if (_certs != null) {
                SEQUENCE certsSeq = new SEQUENCE();
                for (Certificate c : _certs) {
                    certsSeq.addElement(c);
                }
                EXPLICIT certsExplicit = new EXPLICIT(new Tag(0),certsSeq);
                seq.addElement(certsExplicit);
            }
            seq.encode(t,os);
       }
    }

    public void encode(OutputStream os) throws IOException
    {
        encode(TAG, os);
    }

    public OCTET_STRING getBytes()
    {
        return null;
    }

    public ResponseData getResponseData()
    {
        return _rd;
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return _signAlg;
    }

    public BIT_STRING getSignature()
    {
        return _signature;
    }

    public int getCertsCount()
    {
        return (_certs != null) ? _certs.length : 0;
    }

    public  Certificate[] getCerts()
    {
        return  _certs;
    }

    public Certificate getCertificateAt(int pos)
    {
        return (_certs != null) ? _certs[pos] : null;
    }

    private static final Template templateInstance = new Template();

    public static Template getTemplate() {
        return templateInstance;
    }

    /**
     * A Template for decoding <code>ResponseBytes</code>.
     */
    public static class Template implements ASN1Template
    {
        private SEQUENCE.Template seqt;

        public Template()
        {
            seqt = new SEQUENCE.Template(); 
            seqt.addElement( ResponseData.getTemplate() );
            seqt.addElement( AlgorithmIdentifier.getTemplate() );
            seqt.addElement( BIT_STRING.getTemplate() );
            seqt.addOptionalElement( new EXPLICIT.Template( 
                new Tag(0), new SEQUENCE.OF_Template( 
            Certificate.getTemplate())) );
         }

         public boolean tagMatch(Tag tag)
         {
             return TAG.equals(tag);
         }

         public ASN1Value decode(InputStream istream)
             throws InvalidBERException, IOException
         {
             return decode(TAG, istream);
         }

         public ASN1Value decode(Tag implicitTag, InputStream istream)
             throws InvalidBERException, IOException
         {
             SEQUENCE seq = (SEQUENCE) seqt.decode(implicitTag, istream);

             ResponseData rd = (ResponseData)seq.elementAt(0);
             AlgorithmIdentifier alg =  (AlgorithmIdentifier)seq.elementAt(1);
             BIT_STRING bs =  (BIT_STRING)seq.elementAt(2);
             Certificate[] certs = null;
             if (seq.size() == 4) {
                 // optional certificates are present
                 EXPLICIT certSeqExplicit = (EXPLICIT) seq.elementAt(3);
                 SEQUENCE certSeq = (SEQUENCE) certSeqExplicit.getContent();
                 if (certSeq != null) {
                     certs = new Certificate[certSeq.size()];
                     for (int x = 0; x < certSeq.size(); x++) {
                         certs[x] = (Certificate) certSeq.elementAt(x);
                     }
                 }
             }

             return new BasicOCSPResponse(rd, alg, bs, certs);
          }
    }
}
