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
package com.netscape.cmscore.registry;


import java.util.*;
import com.netscape.certsrv.apps.*;
import com.netscape.certsrv.base.*;
import com.netscape.certsrv.registry.*;


public class PluginRegistry implements IPluginRegistry {

    private static final String PROP_TYPES = "types";
    private static final String PROP_IDS = "ids";
    private static final String PROP_NAME = "name";
    private static final String PROP_DESC = "desc";
    private static final String PROP_CLASSPATH = "class";
    private static final String PROP_FILE = "file";

    private IConfigStore mConfig = null;
    private IConfigStore mFileConfig = null;
    private ISubsystem mOwner = null;
    private Hashtable mTypes = new Hashtable();

    public PluginRegistry() {
    }

    /**
     * Retrieves the name of this subsystem.
     */
    public String getId() {
        return null; 
    }

    /**
     * Sets specific to this subsystem.
     */
    public void setId(String id) throws EBaseException {
    }

    /**
     * Initializes this subsystem with the given configuration
     * store.
     * <P>
     *
     * @param owner owner of this subsystem
     * @param config configuration store
     * @exception EBaseException failed to initialize
     */
    public void init(ISubsystem owner, IConfigStore config)
        throws EBaseException {
        CMS.debug("RegistrySubsystem: start init");
        mConfig = config;
        mOwner = owner;

        mFileConfig = CMS.createFileConfigStore(
                    mConfig.getString(PROP_FILE));

        String types_str = null;

        try {
            types_str = mFileConfig.getString(PROP_TYPES, null);
        } catch (EBaseException e) {
        }
        if (types_str == null) {
            CMS.debug("PluginRegistry: no types");
            return;
        }
        StringTokenizer st = new StringTokenizer(types_str, ",");

        while (st.hasMoreTokens()) {
            String type = st.nextToken();

            loadPlugins(config, type);
        }
    }

    /**
     * Load plugins of the given type.
     */
    public void loadPlugins(IConfigStore config, String type)
        throws EBaseException {
        String ids_str = null;

        try {
            ids_str = mFileConfig.getString(type + "." + PROP_IDS, null);
        } catch (EBaseException e) {
        }
        if (ids_str == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(ids_str, ",");

        while (st.hasMoreTokens()) {
            String id = st.nextToken();

            loadPlugin(config, type, id);
        }
    }


    public IPluginInfo createPluginInfo(String name, String desc, String classPath) {
        return new PluginInfo(name, desc, classPath);
    }

    /**
     * Load plugins of the given type.
     */
    public void loadPlugin(IConfigStore config, String type, String id)
        throws EBaseException {
        String name = null;

        try {
            name = mFileConfig.getString(type + "." + id + "." + PROP_NAME, null);
        } catch (EBaseException e) {
        }
        String desc = null;

        try {
            desc = mFileConfig.getString(type + "." + id + "." + PROP_DESC, null);
        } catch (EBaseException e) {
        }
        String classpath = null;

        try {
            classpath = mFileConfig.getString(type + "." + id + "." + PROP_CLASSPATH, 
                        null);
        } catch (EBaseException e) {
        }
        PluginInfo info = new PluginInfo(name, desc, classpath);

        addPluginInfo(type, id, info);
    }

    public void removePluginInfo(String type, String id)
        throws ERegistryException {
        Hashtable plugins = (Hashtable)mTypes.get(type);
        if (plugins == null)
          return;
        plugins.remove(id);
        Locale locale = Locale.getDefault();
        rebuildConfigStore(locale);
    }

    public void addPluginInfo(String type, String id, IPluginInfo info)
        throws ERegistryException {
        Hashtable plugins = (Hashtable) mTypes.get(type); 

        if (plugins == null) {
            plugins = new Hashtable();
            mTypes.put(type, plugins);
        }
        Locale locale = Locale.getDefault();

        CMS.debug("added plugin " + type + " " + id + " " + 
            info.getName(locale) + " " + info.getDescription(locale) + " " +
            info.getClassName());
        plugins.put(id, info);

        // rebuild configuration store
        rebuildConfigStore(locale);
    }

    public void rebuildConfigStore(Locale locale)
        throws ERegistryException {
        Enumeration types = mTypes.keys();
        StringBuffer typesBuf = new StringBuffer();

        while (types.hasMoreElements()) {
            String type = (String) types.nextElement();

            typesBuf.append(type);
            if (types.hasMoreElements()) {
                typesBuf.append(",");
            }
            Hashtable mPlugins = (Hashtable) mTypes.get(type);
            StringBuffer idsBuf = new StringBuffer();
            Enumeration plugins = mPlugins.keys();

            while (plugins.hasMoreElements()) {
                String id = (String) plugins.nextElement();

                idsBuf.append(id);
                if (plugins.hasMoreElements()) {
                    idsBuf.append(",");
                }
                IPluginInfo plugin = (IPluginInfo) mPlugins.get(id);

                mFileConfig.putString(type + "." + id + ".class", 
                    plugin.getClassName());
                mFileConfig.putString(type + "." + id + ".name", 
                    plugin.getName(locale));
                mFileConfig.putString(type + "." + id + ".desc", 
                    plugin.getDescription(locale));
            }
            mFileConfig.putString(type + ".ids", idsBuf.toString());
        }
        mFileConfig.putString("types", typesBuf.toString());
        try {
          mFileConfig.commit(false);
        } catch (EBaseException e) {
          CMS.debug("PluginRegistry: failed to commit registry.cfg");
        }
    }

    /**
     * Notifies this subsystem if owner is in running mode.
     */
    public void startup() throws EBaseException {
        CMS.debug("RegistrySubsystem: startup");
    }

    /**
     * Stops this system. The owner may call shutdown
     * anytime after initialization.
     * <P>
     */
    public void shutdown() {
        mTypes.clear();
        mTypes = null;
    }

    /**
     * Returns the root configuration storage of this system.
     * <P>
     *
     * @return configuration store of this subsystem
     */
    public IConfigStore getConfigStore() {
        return mConfig;
    }

    public IConfigStore getFileConfigStore() {
        return mFileConfig;
    }

    /**
     * Returns all type names.
     */
    public Enumeration getTypeNames() {
        return mTypes.keys();
    }

    /**
     * Returns a list of identifiers of the given type.
     */
    public Enumeration getIds(String type) {
        Hashtable plugins = (Hashtable) mTypes.get(type);

        if (plugins == null)
            return null;
        return plugins.keys();
    }

    /**
     * Retrieves the plugin information.
     */
    public IPluginInfo getPluginInfo(String type, String id) {
        Hashtable plugins = (Hashtable) mTypes.get(type);

        if (plugins == null)
            return null;
        return (IPluginInfo) plugins.get(id);
    }

}
