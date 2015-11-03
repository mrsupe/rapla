
/*--------------------------------------------------------------------------*
 | Copyright (C) 2006  Christopher Kohlhaas                                 |
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

package org.rapla.plugin.compactweekview.client.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.rapla.client.PopupContext;
import org.rapla.client.ReservationController;
import org.rapla.client.extensionpoints.ObjectMenuFactory;
import org.rapla.client.internal.RaplaClipboard;
import org.rapla.client.swing.InfoFactory;
import org.rapla.client.swing.MenuFactory;
import org.rapla.client.swing.images.RaplaImages;
import org.rapla.client.swing.toolkit.DialogUI;
import org.rapla.components.calendar.DateRenderer;
import org.rapla.components.calendar.DateRenderer.RenderingInfo;
import org.rapla.components.calendar.DateRendererAdapter;
import org.rapla.components.calendarview.Block;
import org.rapla.components.calendarview.swing.AbstractSwingCalendar;
import org.rapla.components.calendarview.swing.SwingCompactWeekView;
import org.rapla.components.calendarview.swing.ViewListener;
import org.rapla.components.util.DateTools;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.facade.CalendarModel;
import org.rapla.facade.CalendarOptions;
import org.rapla.facade.CalendarSelectionModel;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.abstractcalendar.AbstractRaplaBlock;
import org.rapla.plugin.abstractcalendar.GroupAllocatablesStrategy;
import org.rapla.plugin.abstractcalendar.RaplaBuilder;
import org.rapla.plugin.abstractcalendar.RaplaCalendarViewListener;
import org.rapla.plugin.abstractcalendar.client.swing.AbstractRaplaSwingCalendar;


public class SwingCompactWeekCalendar extends AbstractRaplaSwingCalendar
{
    public SwingCompactWeekCalendar(RaplaContext sm,CalendarModel settings, boolean editable, Set<ObjectMenuFactory>objectMenuFactories, MenuFactory menuFactory, Provider<DateRenderer> dateRendererProvider, CalendarSelectionModel calendarSelectionModel, RaplaClipboard clipboard, ReservationController reservationController, InfoFactory<Component, DialogUI> infoFactory, RaplaImages raplaImages) throws RaplaException {
        super( sm, settings, editable, objectMenuFactories, menuFactory, dateRendererProvider, calendarSelectionModel, clipboard, reservationController, infoFactory, raplaImages);
    }
    
    protected AbstractSwingCalendar createView(boolean showScrollPane) {
        final DateRendererAdapter dateRenderer = new DateRendererAdapter(dateRendererProvider.get(), getRaplaLocale().getTimeZone(), getRaplaLocale().getLocale());
        SwingCompactWeekView compactWeekView = new SwingCompactWeekView( showScrollPane ) {
            @Override
            protected JComponent createColumnHeader(Integer column) {
                JLabel component = (JLabel) super.createColumnHeader(column);
                if ( column != null ) {
                	Date date = getDateFromColumn(column);
                    boolean today = DateTools.isSameDay(getQuery().today().getTime(), date.getTime());
                    if ( today)
                    {
                        component.setFont(component.getFont().deriveFont( Font.BOLD));
                    }
                    if (isEditable()  ) {
                        component.setOpaque(true);
                        RenderingInfo renderingInfo = dateRenderer.getRenderingInfo( date);

                        if  ( renderingInfo.getBackgroundColor() != null)
                        {
                            component.setBackground(renderingInfo.getBackgroundColor());
                        }
                        if  ( renderingInfo.getForegroundColor() != null)
                        {
                            component.setForeground(renderingInfo.getForegroundColor());
                        }
                        component.setToolTipText(renderingInfo.getTooltipText());
                    }
                } 
                else 
                {
                	String calendarWeek = MessageFormat.format(getString("calendarweek.abbreviation"), getStartDate());
                	component.setText( calendarWeek);
                }
                return component;
            }
            protected int getColumnCount() 
        	{
            	return getDaysInView();
        	}

        };
		return compactWeekView;

    }
    

    
    protected ViewListener createListener() throws RaplaException {
        RaplaCalendarViewListener listener = new RaplaCalendarViewListener(getContext(), model, view.getComponent(), objectMenuFactories, menuFactory, calendarSelectionModel, clipboard, reservationController, infoFactory, raplaImages) {
            
            @Override
            public void selectionChanged(Date start, Date end) {
                if ( end.getTime()- start.getTime() == DateTools.MILLISECONDS_PER_DAY ) {
                    Calendar cal = getRaplaLocale().createCalendar();
                    cal.setTime ( start );
                    int worktimeStartMinutes = getCalendarOptions().getWorktimeStartMinutes();
                    cal.set( Calendar.HOUR_OF_DAY, worktimeStartMinutes / 60);
					cal.set( Calendar.MINUTE, worktimeStartMinutes%60);
                    start = cal.getTime();
                    end = new Date ( start.getTime() + 30 * DateTools.MILLISECONDS_PER_MINUTE );
                }
            	super.selectionChanged(start, end);
            }
            
            @Override
            protected Collection<Allocatable> getMarkedAllocatables() {
            	final List<Allocatable> selectedAllocatables = getSortedAllocatables();
				 
            	Set<Allocatable> allSelected = new HashSet<Allocatable>();
				if ( selectedAllocatables.size() == 1 ) {
					allSelected.add(selectedAllocatables.get(0));
				}
	               
				int i= 0;
				int daysInView = view.getDaysInView();
				for ( Allocatable alloc:selectedAllocatables)
				{
					boolean add = false;
					for (int slot = i*daysInView;slot< (i+1)*daysInView;slot++)
					{
						if ( view.isSelected(slot))
						{
							add = true;
						}
					}
					if ( add )
					{
						allSelected.add(alloc);
					}
					i++;
				}
				return allSelected;
            }
            
            @Override
			 public void moved(Block block, Point p, Date newStart, int slotNr) {
				 int index= slotNr / view.getDaysInView();//getIndex( selectedAllocatables, block );
				 if ( index < 0)
				 {
					 return;
				 }
				 
				 try 
				 {
					 final List<Allocatable> selectedAllocatables = getSortedAllocatables();
					 Allocatable newAlloc = selectedAllocatables.get(index);
					 AbstractRaplaBlock raplaBlock = (AbstractRaplaBlock)block;
					 Allocatable oldAlloc = raplaBlock.getGroupAllocatable();
					 if ( newAlloc != null && oldAlloc != null && !newAlloc.equals(oldAlloc))
					 {
						 AppointmentBlock appointmentBlock = raplaBlock.getAppointmentBlock();
						 PopupContext popupContext = createPopupContext(getMainComponent(),p);
						 reservationController.exchangeAllocatable(appointmentBlock, oldAlloc,newAlloc, newStart,popupContext);
					 }
					 else
					 {
						 super.moved(block, p, newStart, slotNr);
					 }
					 
				 } 
				 catch (RaplaException ex) {
					showException(ex, getMainComponent());
				}
			
			 }

        };
        listener.setKeepTime( true);
        return listener;
    }

    protected RaplaBuilder createBuilder() throws RaplaException {
        RaplaBuilder builder = super.createBuilder();
        
		builder.setSmallBlocks( true );
      
        String[] slotNames;
        final List<Allocatable> allocatables = getSortedAllocatables();
      	GroupAllocatablesStrategy strategy = new GroupAllocatablesStrategy( getRaplaLocale().getLocale() );
    	strategy.setFixedSlotsEnabled( true);
    	strategy.setResolveConflictsEnabled( false );
    	strategy.setAllocatables(allocatables) ;
    	builder.setBuildStrategy( strategy );
        slotNames = new String[ allocatables.size() ];
        for (int i = 0; i <allocatables.size(); i++ ) {
            slotNames[i] = allocatables.get(i).getName( getRaplaLocale().getLocale() );
        }
        builder.setSplitByAllocatables( true );
        ((SwingCompactWeekView)view).setLeftColumnSize( 150);
        ((SwingCompactWeekView)view).setSlots( slotNames );
        return builder;
    }

    protected void configureView() throws RaplaException {
        CalendarOptions calendarOptions = getCalendarOptions();
        Set<Integer> excludeDays = calendarOptions.getExcludeDays();
        view.setExcludeDays( excludeDays );
        view.setDaysInView( calendarOptions.getDaysInWeekview());
        int firstDayOfWeek = calendarOptions.getFirstDayOfWeek();
		view.setFirstWeekday( firstDayOfWeek);
        view.setToDate(model.getSelectedDate());
//        if ( !view.isEditable() ) {
//            view.setSlotSize( model.getSize());
//        } else {
//            view.setSlotSize( 200 );
//        }
    }

    public int getIncrementSize()
    {
        return Calendar.WEEK_OF_YEAR;
    }

  
    

}
