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
package com.netscape.cmscore.dbs;


import java.util.*;
import netscape.ldap.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.logging.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.dbs.*;
 

/**
 * A class represents a registry where all the
 * schema (object classes and attribute) information 
 * is stored.
 *
 * Attribute mappers can be registered with this
 * registry.
 *
 * Given the schema information stored, this registry
 * has knowledge to convert a Java object into a
 * LDAPAttributeSet or vice versa.
 *
 * @author thomask
 * @version $Revision: 1211 $, $Date: 2010-08-18 10:15:37 -0700 (Wed, 18 Aug 2010) $ 
 */
public class DBRegistry implements IDBRegistry, ISubsystem {

    private IConfigStore mConfig = null;
    private Hashtable mOCclassNames = new Hashtable();
    private Hashtable mOCldapNames = new Hashtable();
    private Hashtable mAttrufNames = new Hashtable();
    private IFilterConverter mConverter = null;
    private Vector mDynAttrMappers = new Vector();

    private ILogger mLogger = CMS.getLogger();

    /**
     * Constructs registry.
     */
    public DBRegistry() {
    }

    /**
     * Retrieves subsystem identifier.
     */
    public String getId() {
        return "dbsregistry";
    }

    /**
     * Sets subsystem identifier. This is an internal
     * subsystem, and is not loadable.
     */
    public void setId(String id) throws EBaseException {
        throw new EBaseException(CMS.getUserMessage("CMS_BASE_INVALID_OPERATION"));
    }

    /**
     * Initializes the internal registery. Connects to the 
     * data source, and create a pool of connection of which 
     * applications can use. Optionally, check the integrity
     * of the database.
     */
    public void init(ISubsystem owner, IConfigStore config) 
        throws EBaseException {
        mConfig = config;
        mConverter = new LdapFilterConverter(mAttrufNames);
    }
	
    /**
     * Retrieves configuration store.
     */
    public IConfigStore getConfigStore() {
        return mConfig;
    }

    /**
     * Starts up this subsystem.
     */
    public void startup() throws EBaseException {
    }

    /**
     * Shutdowns this subsystem gracefully.
     */
    public void shutdown() {
        mOCclassNames.clear();
        mOCclassNames = null;
        mOCldapNames.clear();
        mOCldapNames = null;
        mAttrufNames.clear();
        mAttrufNames = null;
        mConverter = null;
    }

    /**
     * Registers object class.
     */
    public void registerObjectClass(String className, String ldapNames[])
        throws EDBException {
        try {
            Class c = Class.forName(className);

            mOCclassNames.put(className, ldapNames);
            mOCldapNames.put(sortAndConcate(
                    ldapNames).toLowerCase(), 
                new NameAndObject(className, c));
        } catch (ClassNotFoundException e) {

            /*LogDoc
             *
             * @phase db startup
             * @reason failed to register object class
             * @message DBRegistry: <exception thrown>
             */
            mLogger.log(ILogger.EV_SYSTEM, ILogger.S_DB,
                ILogger.LL_FAILURE, CMS.getLogMessage("OPERATION_ERROR", e.toString()));
            throw new EDBException(
                    CMS.getUserMessage("CMS_DBS_INVALID_CLASS_NAME", className));
        }
    }

    /**
     * See if an object class is registered.
     */
    public boolean isObjectClassRegistered(String className) {
        return mOCclassNames.containsKey(className);
    }

    /**
     * Registers attribute mapper.
     */
    public void registerAttribute(String ufName, IDBAttrMapper mapper) 
        throws EDBException {
        // should not allows 'objectclass' as attribute; it has
        // special meaning
        mAttrufNames.put(ufName.toLowerCase(), mapper);
    }

    /**
     * See if an attribute is registered.
     */
    public boolean isAttributeRegistered(String ufName) {
        return mAttrufNames.containsKey(ufName.toLowerCase());
    }

    public void registerDynamicMapper(IDBDynAttrMapper mapper) {
        mDynAttrMappers.add(mapper);
    }

    /**
     * Creates LDAP-based search filters with help of
     * registered mappers.
     * Parses filter from filter string specified in RFC1558.
     * <pre>
     * <filter> ::= '(' <filtercomp> ')'
     * <filtercomp> ::= <and> | <or> | <not> | <item>
     * <and> ::= '&' <filterlist>
     * <or> ::= '|' <filterlist>
     * <not> ::= '!' <filter>
     * <filterlist> ::= <filter> | <filter> <filterlist>
     * <item> ::= <simple> | <present> | <substring>
     * <simple> ::= <attr> <filtertype> <value>
     * <filtertype> ::= <equal> | <approx> | <greater> | <less>
     * <equal> ::= '='
     * <approx> ::= '~='
     * <greater> ::= '>='
     * <less> ::= '<='
     * <present> ::= <attr> '=*'
     * <substring> ::= <attr> '=' <initial> <any> <final>
     * <initial> ::= NULL | <value>
     * <any> ::= '*' <starval>
     * <starval> ::= NULL | <value> '*' <starval>
     * <final> ::= NULL | <value>
     * </pre>
     */
    public String getFilter(String filter) throws EBaseException {
        return getFilter(filter, mConverter);
    }

    public String getFilter(String filter, IFilterConverter c) 
        throws EBaseException {
        String f = filter;

        f = f.trim();
        if (f.startsWith("(") && f.endsWith(")")) {
            return "(" + getFilterComp(f.substring(1, 
                        f.length() - 1), c) + ")";
        } else {
            return getFilterComp(filter, c);
        }
    }

    private String getFilterComp(String f, IFilterConverter c) 
        throws EBaseException {
        f = f.trim();
        if (f.startsWith("&")) {        // AND operation
            return "&" + getFilterList(f.substring(1, 
                        f.length()), c);
        } else if (f.startsWith("|")) { // OR operation
            return "|" + getFilterList(f.substring(1, 
                        f.length()), c);
        } else if (f.startsWith("!")) { // NOT operation
            return "!" + getFilter(f.substring(1, f.length()), c);
        } else {                        // item
            return getFilterItem(f, c);
        }
    }
	
    private String getFilterList(String f, IFilterConverter c) 
        throws EBaseException {
        f = f.trim();
        int level = 0;
        int start = 0;
        int end = 0;
        Vector v = new Vector();

        for (int i = 0; i < f.length(); i++) {
            if (f.charAt(i) == '(') {
                if (level == 0) {
                    start = i;
                }
                level++;
            }
            if (f.charAt(i) == ')') {
                level--;
                if (level == 0) {
                    end = i;
                    String filter = getFilter(f.substring(start, end + 1), c);

                    v.addElement(filter);
                }
            }
        }
        String result = "";

        for (int i = 0; i < v.size(); i++) {
            result += (String) v.elementAt(i);
        }
        return result;
    }

    /**
     * So, here we need to separate item into name, op, value.
     */
    private String getFilterItem(String f, IFilterConverter c) 
        throws EBaseException {
        f = f.trim();
        int idx = f.indexOf('=');

        if (idx == -1) {
            throw new EDBException(
                    CMS.getUserMessage("CMS_DBS_INVALID_FILTER_ITEM", "="));
        }

        String type = f.substring(0, idx).trim();
        String value = f.substring(idx + 1).trim(); // skip '='

        // make decision by looking at the type
        type = type.trim();
        if (type.endsWith("~")) {
            // approximate match
            String name = type.substring(0, type.length() - 1).trim();

            return c.convert(name, "~=", value);
        } else if (type.endsWith("!")) {
            String name = type.substring(0, type.length() - 1).trim();

            return c.convert(name, "!=", value);
        } else if (type.endsWith(">")) {
            // greater than
            String name = type.substring(0, type.length() - 1).trim();

            return c.convert(name, ">=", value);
        } else if (type.endsWith("<")) {
            String name = type.substring(0, type.length() - 1).trim();

            return c.convert(name, "<=", value);
        }

        // for those that are not simple
        if (value.startsWith("*") && value.length() == 1) {
            return c.convert(type, "=", "*");
        }

        // if value contains no '*', then it is equality
        if (value.indexOf('*') == -1) {
            if (type.equals("objectclass")) {
                String ldapNames[] = (String[])
                    mOCclassNames.get(value);

                if (ldapNames == null)
                    throw new EDBException(
                            CMS.getUserMessage("CMS_DBS_INVALID_FILTER_ITEM", f));
                String filter = "";

                for (int g = 0; g < ldapNames.length; g++) {
                    filter += "(objectclass=" + 
                            ldapNames[g] + ")";	
                }
                return "&" + filter;
            } else {
                return c.convert(type, "=", value);
            }
        }
        // XXX - does not support substring!!
        return c.convert(type, "=", value);
    }

    /**
     * Maps object into LDAP attribute set.
     */
    public void mapObject(IDBObj parent, String name, Object obj, 
        LDAPAttributeSet attrs) throws EBaseException {
        IDBAttrMapper mapper = (IDBAttrMapper) mAttrufNames.get(
                name.toLowerCase());

        if (mapper == null) {
            return; // no mapper found, just skip this attribute
        }	
        mapper.mapObjectToLDAPAttributeSet(parent, name, obj, attrs);
    }

    /**
     * Retrieves a list of LDAP attributes that are associated
     * with the given attributes.
     * This method is used for searches, to map the database attributes
     * to LDAP attributes.
     */
    public String[] getLDAPAttributes(String attrs[]) 
        throws EBaseException {
        IDBAttrMapper mapper;
	    
        if (attrs == null)
            return null;
        Vector v = new Vector();

        for (int i = 0; i < attrs.length; i++) {

            if (attrs[i].equals("objectclass")) {
                v.addElement("objectclass");
                continue;
            }

            if (isAttributeRegistered(attrs[i])) {
                mapper = (IDBAttrMapper)
                        mAttrufNames.get(attrs[i].toLowerCase());
                if (mapper == null) {
                    throw new EDBException(CMS.getUserMessage("CMS_DBS_INVALID_ATTRS"));
                }
                Enumeration e = mapper.getSupportedLDAPAttributeNames();

                while (e.hasMoreElements()) {
                    String s = (String) e.nextElement();

                    if (!v.contains(s)) {
                        v.addElement(s);
                    }
                }
            } else {
                IDBDynAttrMapper matchingDynAttrMapper = null;
                // check if a dynamic mapper can handle the attribute
                for (Iterator dynMapperIter = mDynAttrMappers.iterator();
                     dynMapperIter.hasNext();) {
                    IDBDynAttrMapper dynAttrMapper =
                            (IDBDynAttrMapper)dynMapperIter.next();
                    if (dynAttrMapper.supportsLDAPAttributeName(attrs[i])) {
                        matchingDynAttrMapper = dynAttrMapper;
                        break;
                    }
                }
                if (matchingDynAttrMapper != null) {
                    v.addElement(attrs[i]);
                } else {
                    /*LogDoc
                     *
                     * @phase retrieve ldap attr
                     * @reason failed to get registered object class
                     * @message DBRegistry: <attr> is not registered
                     */
                    mLogger.log(ILogger.EV_SYSTEM, ILogger.S_DB,
                        ILogger.LL_FAILURE, CMS.getLogMessage("CMSCORE_DBS_ATTR_NOT_REGISTER", attrs[i]));
                    throw new EDBException(CMS.getLogMessage("CMSCORE_DBS_ATTR_NOT_REGISTER", attrs[i]));
                }
            }

        }
        if (v.size() == 0)
            return null;
        String ldapAttrs[] = new String[v.size()];

        v.copyInto(ldapAttrs);
        return ldapAttrs;
    }

    /**
     * Creates attribute set from object.
     */
    public LDAPAttributeSet createLDAPAttributeSet(IDBObj obj) 
        throws EBaseException {
        Enumeration e = obj.getSerializableAttrNames();
        LDAPAttributeSet attrs = new LDAPAttributeSet();

        // add object class to attribute set
        String className = ((Object) obj).getClass().getName();
        String vals[] = (String[]) mOCclassNames.get(className);

        attrs.add(new LDAPAttribute("objectclass", vals));

        // give every attribute a chance to put stuff in attr set
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();

            if (obj.get(name) != null) {
                mapObject(obj, name, obj.get(name), attrs);
            }
        }
        return attrs;
    }

    /**
     * Creates object from attribute set.
     */
    public IDBObj createObject(LDAPAttributeSet attrs)
        throws EBaseException {
        // map object class attribute to object
        LDAPAttribute attr = attrs.getAttribute("objectclass");

        //CMS.debug("createObject: attrs " + attrs.toString());

        attrs.remove("objectclass");

        // sort the object class values
        Enumeration vals = attr.getStringValues();
        Vector v = new Vector();

        while (vals.hasMoreElements()) {
            v.addElement(vals.nextElement());
        }
        String s[] = new String[v.size()];

        v.copyInto(s);
        String sorted = sortAndConcate(s).toLowerCase();
        NameAndObject no = (NameAndObject) mOCldapNames.get(sorted);

        if (no == null) {
            throw new EDBException(
                    CMS.getUserMessage("CMS_DBS_INVALID_CLASS_NAME", sorted));
        }
        Class c = (Class) no.getObject();

        try {
            IDBObj obj = (IDBObj) c.newInstance();
            Enumeration ee = obj.getSerializableAttrNames();

            while (ee.hasMoreElements()) {
                String oname = (String) ee.nextElement();
                IDBAttrMapper mapper = (IDBAttrMapper)
                    mAttrufNames.get(
                        oname.toLowerCase());

                if (mapper == null) {
                    throw new EDBException(
                            CMS.getUserMessage("CMS_DBS_NO_MAPPER_FOUND", oname));
                }
                mapper.mapLDAPAttributeSetToObject(attrs, 
                    oname, obj);
            }
            return obj;
        } catch (Exception e) {

            /*LogDoc
             *
             * @phase create ldap attr
             * @reason failed to create object class
             * @message DBRegistry: <attr> is not registered
             */
            mLogger.log(ILogger.EV_SYSTEM, ILogger.S_DB,
                ILogger.LL_FAILURE, CMS.getLogMessage("OPERATION_ERROR", e.toString()));
            throw new EDBException(CMS.getUserMessage("CMS_DBS_INVALID_ATTRS"));
        }
    }

    /**
     * Sorts and concate given strings.
     */
    private String sortAndConcate(String s[]) {
        Vector v = new Vector();

        // sort it first
        for (int i = 0; i < s.length; i++) {
            for (int j = 0; j < v.size(); j++) {
                String t = (String) v.elementAt(j);

                if (s[i].compareTo(t) < 0) {
                    v.insertElementAt(s[i], j);
                    break;
                }
            }
            if (i != (v.size() - 1))
                v.addElement(s[i]);
        }

        // concate them
        String result = "";

        for (int i = 0; i < v.size(); i++) {
            result += ((String) v.elementAt(i) + "+");
        }
        return result;
    }
}


/**
 * Just a convenient container class.
 */
class NameAndObject {

    private String mN = null;
    private Object mO = null;

    public NameAndObject(String name, Object o) {
        mN = name;
        mO = o;
    }
	
    public String getName() {
        return mN;
    }

    public Object getObject() {
        return mO;
    }
}
