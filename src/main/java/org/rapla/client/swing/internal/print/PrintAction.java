/*--------------------------------------------------------------------------*
 | Copyright (C) 2014 Christopher Kohlhaas, Bettina Lademann                |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.client.swing.internal.print;
import java.awt.*;
import java.awt.print.PageFormat;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import org.rapla.client.swing.extensionpoints.SwingViewFactory;
import org.rapla.client.swing.images.RaplaImages;
import org.rapla.components.iolayer.IOInterface;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.framework.RaplaContext;
import org.rapla.client.swing.RaplaAction;


public class PrintAction extends RaplaAction {
    CalendarSelectionModel model;
    PageFormat m_pageFormat;
    final Map<String,SwingViewFactory> factoryMap;
    private final IOInterface printInterface;
    private final RaplaImages raplaImages;
    @Inject
    public PrintAction(RaplaContext sm, Map<String, SwingViewFactory> factoryMap, IOInterface printInterface, RaplaImages raplaImages) {
        super( sm);
        this.factoryMap = factoryMap;
        this.printInterface = printInterface;
        this.raplaImages = raplaImages;
        setEnabled(false);
        putValue(NAME,getString("print"));
        putValue(SMALL_ICON, raplaImages.getIconFromKey("icon.print"));
    }

    public void setModel(CalendarSelectionModel settings) {
        this.model = settings;
        setEnabled(settings != null);
    }


    public void setPageFormat(PageFormat pageFormat) {
        m_pageFormat = pageFormat;
    }


    public void actionPerformed() {
        Component parent = getMainComponent();
        try {
            boolean modal = true;
            CalendarPrintDialog dialog = new CalendarPrintDialog(getContext(),(Frame)parent, printInterface, raplaImages);

            dialog.init(modal,factoryMap,model,m_pageFormat);
            final Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setSize(new Dimension(
                                        Math.min(dimension.width,900)
                                        ,Math.min(dimension.height-10,700)
                                        )
                          );
            
            SwingUtilities.invokeLater( new Runnable() {
                public void run()
                {
                    dialog.setSize(new Dimension(
                                             Math.min(dimension.width,900)
                                             ,Math.min(dimension.height-11,699)
                                             )
                               );
                }
                
            }
            );
            dialog.startNoPack();
            
        } catch (Exception ex) {
            showException(ex, parent);
        }
    }
}

