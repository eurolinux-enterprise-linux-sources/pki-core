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
package com.netscape.cmscore.base;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.mozilla.jss.util.Password;
import org.mozilla.jss.util.PasswordCallback;
import org.mozilla.jss.util.PasswordCallbackInfo;
import org.mozilla.jss.*;
import org.mozilla.jss.crypto.CryptoToken;
import javax.swing.border.EmptyBorder;


/**
 * A class to retrieve passwords through a modal Java dialog box
 */
public class JDialogPasswordCallback implements PasswordCallback {

    public Password getPasswordFirstAttempt(PasswordCallbackInfo info)
        throws PasswordCallback.GiveUpException {
        return getPW(info, false);
    }

    public Password getPasswordAgain(PasswordCallbackInfo info)
        throws PasswordCallback.GiveUpException {
        return getPW(info, true);
    }

    // This structure holds information local to a getPW() call, for use
    // by action listeners
    private static class PWHolder {
        public Password password = null;
        public boolean cancelled = true;
    }

    private void resetGBC(GridBagConstraints gbc) {
        gbc.gridx = gbc.RELATIVE;
        gbc.gridy = gbc.RELATIVE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.fill = gbc.HORIZONTAL;
        gbc.anchor = gbc.CENTER;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
    }

    /**
     * This function can be overriden if different heading is required.
     */
    public String getPrompt(PasswordCallbackInfo info) {
        return "Enter the password for \"" + info.getName() + "\": ";
    }

    /**
     * This method does the work of displaying the dialog box,
     * extracting the information, and returning it.
     */
    private Password getPW(PasswordCallbackInfo info, boolean retry)
        throws PasswordCallback.GiveUpException {
        // These need to final so they can be accessed from action listeners
        final PWHolder pwHolder = new PWHolder();
        final JFrame f = new JFrame("Password Dialog");
        final JPasswordField pwField = new JPasswordField(15);

        ///////////////////////////////////////////////////
        // Panel
        ///////////////////////////////////////////////////
        JPanel contentPane = new JPanel(new GridBagLayout());

        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();

        ////////////////////////////////////////////////////
        // Labels
        ////////////////////////////////////////////////////

        if (retry) {
            JLabel warning = new JLabel("Password incorrect.");

            warning.setForeground(Color.red);
            resetGBC(c);
            c.anchor = c.NORTHWEST;
            c.gridwidth = c.REMAINDER;
            // Setting this to NULL causes nasty Exception stack traces
            // to be printed, although the program still seems to work
            //warning.setHighlighter(null);
            contentPane.add(warning, c);
        }
            
        String prompt = getPrompt(info);
        JLabel label = new JLabel(prompt);

        label.setForeground(Color.black);
        // Setting this to NULL causes nasty Exception stack traces
        // to be printed, although the program still seems to work
        //label.setHighlighter(null);
        resetGBC(c);
        c.anchor = c.NORTHWEST;
        c.gridwidth = c.REMAINDER;
        contentPane.add(label, c);

        ///////////////////////////////////////////////////
        // Password text field
        ///////////////////////////////////////////////////

        // Listener for the text field
        ActionListener getPasswordListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //input = (JPasswordField)e.getSource();

                    // XXX!!! Change to char[] in JDK 1.2
                    String pwString = pwField.getText();

                    pwHolder.password = new Password(pwString.toCharArray());
                    pwHolder.cancelled = false;
                    f.dispose();
                }
            };

        // There is a bug in JPasswordField. The cursor is advanced by the
        // width of the character you type, but a '*' is echoed, so the
        // cursor does not stay lined up with the end of the text.
        // We use a monospaced font to workaround this.

        pwField.setFont(new Font("Monospaced", Font.PLAIN, 
                pwField.getFont().getSize()));
        pwField.setEchoChar('*');
        pwField.addActionListener(getPasswordListener);
        resetGBC(c);
        c.anchor = c.CENTER;
        c.fill = c.NONE;
        c.insets = new Insets(16, 0, 0, 0);
        c.gridwidth = c.REMAINDER;
        //c.gridy++;
        contentPane.add(pwField, c);

        ///////////////////////////////////////////////////
        // Cancel button
        ///////////////////////////////////////////////////

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        JButton ok = new JButton(" OK ");

        ok.addActionListener(getPasswordListener);
        resetGBC(c);
        c.fill = c.NONE;
        c.anchor = c.CENTER;
        c.gridheight = c.REMAINDER;
        c.insets = new Insets(10, 0, 0, 4);
        buttonPanel.add(ok, c);

        JButton cancel = new JButton("Cancel");
        ActionListener buttonListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pwHolder.cancelled = true;
                    f.dispose();
                }
            };

        cancel.addActionListener(buttonListener);
        resetGBC(c);
        c.fill = c.NONE;
        c.anchor = c.CENTER;
        c.insets = new Insets(10, 4, 0, 0);
        c.gridheight = c.REMAINDER;
        c.gridwidth = c.REMAINDER;
        buttonPanel.add(cancel, c);

        resetGBC(c);
        c.fill = c.NONE;
        c.anchor = c.CENTER;
        c.gridwidth = c.REMAINDER;
        c.gridheight = c.REMAINDER;
        c.insets = new Insets(0, 0, 0, 0);
        contentPane.add(buttonPanel, c);

        ///////////////////////////////////////////////////
        // Create modal dialog
        ///////////////////////////////////////////////////
        JDialog d = new JDialog(f, "Fedora Certificate System", true);

        WindowListener windowListener = new WindowAdapter() {
                public void windowOpened(WindowEvent e) {
                    pwField.requestFocus();
                }
            };

        d.addWindowListener(windowListener);

        d.setContentPane(contentPane);
        d.pack();
        Dimension screenSize = d.getToolkit().getScreenSize();
        Dimension paneSize = d.getSize();

        d.setLocation((screenSize.width - paneSize.width) / 2,
            (screenSize.height - paneSize.height) / 2);
        d.getRootPane().setDefaultButton(ok);

        // toFront seems to cause the dialog to go blank on unix!
        //d.toFront();

        d.show();

        ///////////////////////////////////////////////////
        // Return results
        ///////////////////////////////////////////////////
        if (pwHolder.cancelled) {
            throw new PasswordCallback.GiveUpException();
        }

        return pwHolder.password;
    }

    // Test program
    public static void main(String args[]) {
        try {
            CryptoManager manager;

            CryptoManager.InitializationValues iv = new
                CryptoManager.InitializationValues(args[0]);

            CryptoManager.initialize(iv);
            manager = CryptoManager.getInstance();

            CryptoToken tok = manager.getInternalKeyStorageToken();

            tok.login(new JDialogPasswordCallback());
            System.out.println("Logged in!!!");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
