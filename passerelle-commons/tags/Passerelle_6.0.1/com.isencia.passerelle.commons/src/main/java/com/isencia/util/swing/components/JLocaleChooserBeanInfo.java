/*
* 02/02/2002 - 20:54:54
*
* JLocaleChooserBeanInfo.java  - Bean Info for JLocaleChooser
* Copyright (C) 2002 Kai Toedter
* kai@toedter.com
* www.toedter.com
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package com.isencia.util.swing.components;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.beans.SimpleBeanInfo;
import java.util.Locale;

import javax.swing.JComboBox;


/**
 * A BeanInfo class for the JLocaleChooser bean.
 *
 * @version 1.1 02/04/02
 * @author  Kai Toedter
 */
public class JLocaleChooserBeanInfo extends SimpleBeanInfo {

    /** 16x16 color icon. */
    Image icon;
    /** 32x32 color icon. */
    Image icon32;
    /** 16x16 mono icon. */
    Image iconM;
    /** 32x32 mono icon. */
    Image icon32M;

    /**
     * Constructs a new BeanInfo class for the JLocaleChooser bean.
     */
    public JLocaleChooserBeanInfo() {
        icon = loadImage ("be.isencia.util.swing.images/JLocaleChooserColor16.gif");
        icon32 = loadImage ("be.isencia.util.swing.images/JLocaleChooserColor32.gif");
        iconM = loadImage ("be.isencia.util.swing.images/JLocaleChooserMono16.gif");
        icon32M = loadImage ("be.isencia.util.swing.images/JLocaleChooserMono32.gif");
    }

    /**
     * This method returns an image object that can be used
     * to represent the bean in toolboxes, toolbars, etc.
     */
    public Image getIcon(int iconKind) {
        switch (iconKind) {
        case ICON_COLOR_16x16:
            return icon;
        case ICON_COLOR_32x32:
            return icon32;
        case ICON_MONO_16x16:
            return iconM;
        case ICON_MONO_32x32:
            return icon32M;
        }
        return null;
    }

    /**
     * This method returns an array of PropertyDescriptors describing
     * the editable properties supported by this bean.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            if (PropertyEditorManager.findEditor(Locale.class) == null) {
                BeanInfo beanInfo =
                        Introspector.getBeanInfo(JComboBox.class);
                PropertyDescriptor[] p = beanInfo.getPropertyDescriptors();

                int length = p.length;
                PropertyDescriptor[] propertyDescriptors =
                        new PropertyDescriptor[length + 1];
                for (int i = 0; i < length; i++)
                    propertyDescriptors[i + 1] = p[i];

                propertyDescriptors [0] = new PropertyDescriptor("locale",
                        JLocaleChooser.class);
                propertyDescriptors [0].setBound(true);
                propertyDescriptors [0].setConstrained(false);
                propertyDescriptors [0].setPropertyEditorClass(
                        LocaleEditor.class);
                return propertyDescriptors;
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return null;
    }
}


