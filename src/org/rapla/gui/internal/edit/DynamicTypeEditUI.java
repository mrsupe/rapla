/*--------------------------------------------------------------------------*
 | Copyright (C) 2013 Christopher Kohlhaas                                  |
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
package org.rapla.gui.internal.edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.rapla.components.layout.TableLayout;
import org.rapla.components.util.Assert;
import org.rapla.components.util.Tools;
import org.rapla.entities.Annotatable;
import org.rapla.entities.IllegalAnnotationException;
import org.rapla.entities.MultiLanguageName;
import org.rapla.entities.UniqueKeyException;
import org.rapla.entities.dynamictype.Attribute;
import org.rapla.entities.dynamictype.AttributeAnnotations;
import org.rapla.entities.dynamictype.AttributeType;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.entities.dynamictype.DynamicTypeAnnotations;
import org.rapla.entities.dynamictype.internal.DynamicTypeImpl;
import org.rapla.framework.Container;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.AnnotationEditExtension;
import org.rapla.gui.EditComponent;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.internal.edit.annotation.AnnotationEditUI;
import org.rapla.gui.internal.edit.fields.MultiLanguageField;
import org.rapla.gui.internal.edit.fields.TextField;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.RaplaButton;


/****************************************************************
 * This is the controller-class for the DynamicType-Edit-Panel   *
 ****************************************************************/
class DynamicTypeEditUI extends RaplaGUIComponent
    implements
     EditComponent<DynamicType>
{
    public static String WARNING_SHOWED = DynamicTypeEditUI.class.getName() + "/Warning";
    DynamicType dynamicType;
    JPanel editPanel = new JPanel();
    JPanel annotationPanel = new JPanel();
    JLabel nameLabel = new JLabel();
    MultiLanguageField name;
    JLabel elementKeyLabel = new JLabel();
    TextField elementKey;
    AttributeEdit attributeEdit;

    JLabel annotationLabel = new JLabel();
    JLabel annotationDescription = new JLabel();
    
    JTextField annotationText = new JTextField();
    JTextField annotationTreeText = new JTextField();
    JComboBox colorChooser;
    
    RaplaButton annotationButton = new RaplaButton(RaplaButton.DEFAULT);
    
    JLabel locationLabel = new JLabel("location");
    JComboBox locationChooser;
    JLabel conflictLabel = new JLabel("conflict creation");
	JComboBox conflictChooser;
    boolean isResourceType;
    boolean isEventType;
    AnnotationEditUI annotationEdit;
    DialogUI dialog;

    public DynamicTypeEditUI(RaplaContext context) throws RaplaException {
        super(context);
        Collection<AnnotationEditExtension> annotationExtensions = context.lookup(Container.class).lookupServicesFor(AnnotationEditExtension.DYNAMICTYPE_ANNOTATION_EDIT);
        annotationEdit = new AnnotationEditUI(context, annotationExtensions);
        {
        	@SuppressWarnings("unchecked")
        	JComboBox jComboBox = new JComboBox(new String[] {getString("color.automated"),getString("color.manual"),getString("color.no")});
        	colorChooser = jComboBox;
        }
        {
        	@SuppressWarnings("unchecked")
        	JComboBox jComboBox = new JComboBox(new String[] {"yes","no"});
        	locationChooser = jComboBox;
        }
        {
        	@SuppressWarnings("unchecked")
        	JComboBox jComboBox = new JComboBox(new String[] {DynamicTypeAnnotations.VALUE_CONFLICTS_ALWAYS,DynamicTypeAnnotations.VALUE_CONFLICTS_NONE,DynamicTypeAnnotations.VALUE_CONFLICTS_WITH_OTHER_TYPES});
        	conflictChooser = jComboBox;
        }
        name = new MultiLanguageField(context,"name");
        elementKey = new TextField(context,"elementKey");
        attributeEdit = new AttributeEdit(context);
        nameLabel.setText(getString("dynamictype.name") + ":");
        elementKeyLabel.setText(getString("elementkey") + ":");
        attributeEdit.setEditKeys( true );
        annotationPanel.setVisible( true);
        double PRE = TableLayout.PREFERRED;
        double[][] sizes = new double[][] {
            {5,PRE,5,TableLayout.FILL,5}
            ,{PRE,5,PRE,5,PRE,5,PRE,5,TableLayout.FILL,5,PRE}
        };
        TableLayout tableLayout = new TableLayout(sizes);
        editPanel.setLayout(tableLayout);
        editPanel.add(nameLabel,"1,2");
        editPanel.add(name.getComponent(),"3,2");
        editPanel.add(elementKeyLabel,"1,4");
        editPanel.add(elementKey.getComponent(),"3,4");
        editPanel.add(attributeEdit.getComponent(),"1,6,3,6");

        // #FIXM Should be replaced by generic solution
        tableLayout.insertRow(7,5);
        tableLayout.insertRow(8,PRE);
        editPanel.add(annotationPanel,"1,8,3,8");
        annotationPanel.setLayout(new TableLayout(new double[][] {
            {PRE,5,TableLayout.FILL}
            ,{PRE,5,PRE,5,PRE, 5, PRE,5, PRE,5,PRE}
        }));
        addCopyPaste( annotationText);
        addCopyPaste(annotationTreeText);
        annotationPanel.add(annotationLabel,"0,0");
        annotationPanel.add(annotationText ,"2,0");
        annotationPanel.add(annotationDescription,"2,2");
        annotationPanel.add(annotationButton ,"2,4");
        annotationPanel.add(new JLabel(getString("color")),"0,6");
        annotationPanel.add(colorChooser,"2,6");
        annotationPanel.add(locationLabel,"0,8");
        annotationPanel.add(locationChooser,"2,8");
        annotationPanel.add(conflictLabel,"0,10");
        annotationPanel.add(conflictChooser,"2,10");
        annotationLabel.setText(getString("dynamictype.annotation.nameformat") + ":");
        annotationButton.setText(getString("edit"));
        annotationButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showAnnotationDialog();
                } catch (RaplaException ex) {
                    showException(ex, getComponent());
                }
                
            }
        });
     
        annotationDescription.setText(getString("dynamictype.annotation.nameformat.description"));
        float newSize = (float) (annotationDescription.getFont().getSize() * 0.8);
        annotationDescription.setFont(annotationDescription.getFont().deriveFont( newSize));
        attributeEdit.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e )
            {
                try {
                    updateAnnotations();
                } catch (RaplaException ex) {
                    showException(ex, getComponent());
                }
            }

        });
        
        colorChooser.addActionListener(new ActionListener() {
			
    			public void actionPerformed(ActionEvent e) {
    				try {
    					int selectedIndex = colorChooser.getSelectedIndex();
                        Attribute firstAttributeWithAnnotation = ((DynamicTypeImpl)dynamicType).getFirstAttributeWithAnnotation(AttributeAnnotations.KEY_COLOR);
                        if ( firstAttributeWithAnnotation != null || selectedIndex != 1)
    					{
    						return;
    					}
    					
    					DialogUI ui = DialogUI.create(getContext(), getMainComponent(), true, getString("color.manual"), getString("attribute_color_dialog"), new String[]{getString("yes"),getString("no")});
						ui.start();
						if (ui.getSelectedIndex() == 0)
						{
							Attribute colorAttribute = getModification().newAttribute(AttributeType.STRING);
							colorAttribute.setKey( "color");
							colorAttribute.setAnnotation(AttributeAnnotations.KEY_COLOR, "true");
							colorAttribute.getName().setName(getLocale().getLanguage(), getString("color"));
							colorAttribute.setAnnotation(AttributeAnnotations.KEY_EDIT_VIEW, AttributeAnnotations.VALUE_EDIT_VIEW_NO_VIEW);
							dynamicType.addAttribute( colorAttribute);
							attributeEdit.setDynamicType(dynamicType);
						}
						else
						{
							colorChooser.setSelectedIndex(2);
						}
    				} catch (RaplaException ex) {
						showException(ex, getMainComponent());
					}
    				
    			}
    		});
        
        /*
        annotationText.addFocusListener( new FocusAdapter() {

            public void focusLost( FocusEvent e )
            {
                try
                {
                    setAnnotations();
                }
                catch ( RaplaException ex )
                {
                    showException( ex, getComponent());
                }
            }

        });
*/
    }

    public JComponent getComponent() {
        return editPanel;
    }

    public void mapToObjects() throws RaplaException {
        MultiLanguageName newName = name.getValue();
		dynamicType.getName().setTo( newName);
        dynamicType.setKey(elementKey.getValue());
        attributeEdit.confirmEdits();
        validate();
        setAnnotations();
    }

    private void setAnnotations() throws RaplaException
    {
        try {
            dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_NAME_FORMAT, annotationText.getText().trim());
            String planningText = annotationTreeText.getText().trim();
            dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_NAME_FORMAT_PLANNING, planningText.length() > 0 ? planningText : null);
        } catch (IllegalAnnotationException ex) {
            throw ex;
        }
        String color= null;
        switch (colorChooser.getSelectedIndex())
        {
        	case 0:color = DynamicTypeAnnotations.VALUE_COLORS_AUTOMATED;break;
        	case 1:color = DynamicTypeAnnotations.VALUE_COLORS_COLOR_ATTRIBUTE;break;
        	case 2:color = DynamicTypeAnnotations.VALUE_COLORS_DISABLED;break;
        }
        dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_COLORS, color);
        if ( isResourceType)
        {
        	String location = null;
	        switch (locationChooser.getSelectedIndex())
	        {
	        	case 0:location = "true";break;
	        	case 1:location = "false";break;
	        }
	        if ( location == null || location.equals( "false"))
	        {
	        	dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_LOCATION, null);
	        }
	        else
	        {
	        	dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_LOCATION, location);
	        }
        }
        if ( isEventType)
        {
	        String conflicts = null;
	        switch (conflictChooser.getSelectedIndex())
	        {
	        	case 0:conflicts = DynamicTypeAnnotations.VALUE_CONFLICTS_ALWAYS;break;
	        	case 1:conflicts = DynamicTypeAnnotations.VALUE_CONFLICTS_NONE;break;
	        	case 2:conflicts = DynamicTypeAnnotations.VALUE_CONFLICTS_WITH_OTHER_TYPES;break;
	        }
            if ( conflicts == null || conflicts.equals( DynamicTypeAnnotations.VALUE_CONFLICTS_ALWAYS))
	        {
	        	dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_CONFLICTS, null);
	        }
	        else
	        {
	        	dynamicType.setAnnotation(DynamicTypeAnnotations.KEY_CONFLICTS, conflicts);
	        }
	
	        
        }
    }
    
    private void showAnnotationDialog() throws RaplaException
    {
        RaplaContext context = getContext();
        boolean modal = false;
        if (dialog != null)
        {
            dialog.close();
        }
        dialog = DialogUI.create(context
                ,getComponent()
                ,modal
                ,annotationEdit.getComponent()
                ,new String[] { getString("close")});

        dialog.getButton(0).setAction( new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public void actionPerformed(ActionEvent e) {
                List<Annotatable> asList = Arrays.asList((Annotatable)dynamicType);
                try {
                    annotationEdit.mapTo(asList);
                } catch (RaplaException e1) {
                    getLogger().error(e1.getMessage(), e1);
                }
                dialog.close();
            }
        });
        dialog.setTitle(getString("select"));
        dialog.start();
    }

    
    public List<DynamicType> getObjects() {
        List<DynamicType> types = Collections.singletonList(dynamicType);
        return types;
    }

    public void setObjects(List<DynamicType> o) throws RaplaException {
        dynamicType =  o.get(0);
        mapFromObjects();
    }
    
    public void mapFromObjects() throws RaplaException
    {
        name.setValue( dynamicType.getName());
        elementKey.setValue( dynamicType.getKey());
        attributeEdit.setDynamicType(dynamicType);
        String classificationType = dynamicType.getAnnotation(DynamicTypeAnnotations.KEY_CLASSIFICATION_TYPE);
		isEventType = classificationType != null && classificationType.equals( DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESERVATION);
		isResourceType = classificationType != null && classificationType.equals( DynamicTypeAnnotations.VALUE_CLASSIFICATION_TYPE_RESOURCE);
		conflictLabel.setVisible( isEventType);
		conflictChooser.setVisible( isEventType);
		locationLabel.setVisible( isResourceType);
		locationChooser.setVisible( isResourceType);

        updateAnnotations();
    }

    private void updateAnnotations() throws RaplaException {
        annotationText.setText( dynamicType.getAnnotation( DynamicTypeAnnotations.KEY_NAME_FORMAT ) );
        annotationTreeText.setText( dynamicType.getAnnotation( DynamicTypeAnnotations.KEY_NAME_FORMAT_PLANNING,"" ) );
        List<Annotatable> asList = Arrays.asList((Annotatable)dynamicType);
        annotationEdit.setObjects(asList);
        
        {
	        String annotation = dynamicType.getAnnotation( DynamicTypeAnnotations.KEY_COLORS); 
	        if (annotation  == null)
	        {
	        	annotation =  dynamicType.getAttribute("color") != null ? DynamicTypeAnnotations.VALUE_COLORS_COLOR_ATTRIBUTE: DynamicTypeAnnotations.VALUE_COLORS_AUTOMATED;
	        }
	        if ( annotation.equals(DynamicTypeAnnotations.VALUE_COLORS_AUTOMATED))
	        {
	        	colorChooser.setSelectedIndex(0);
	        }
	        else if ( annotation.equals( DynamicTypeAnnotations.VALUE_COLORS_COLOR_ATTRIBUTE))
	        {
	         	colorChooser.setSelectedIndex(1);
	        }
	        else if ( annotation.equals( DynamicTypeAnnotations.VALUE_COLORS_DISABLED))
	        {
	         	colorChooser.setSelectedIndex(2);
	        }
        }
        if ( isEventType)
        {
	        String annotation = dynamicType.getAnnotation( DynamicTypeAnnotations.KEY_CONFLICTS); 
	        if (annotation  == null)
	        {
	        	annotation =  DynamicTypeAnnotations.VALUE_CONFLICTS_ALWAYS;
	        }
	        if ( annotation.equals( DynamicTypeAnnotations.VALUE_CONFLICTS_ALWAYS))
	        {
	         	conflictChooser.setSelectedIndex(0);
	        }
	        else if ( annotation.equals(DynamicTypeAnnotations.VALUE_CONFLICTS_NONE))
	        {
	        	conflictChooser.setSelectedIndex(1);
	        }
	        else if ( annotation.equals( DynamicTypeAnnotations.VALUE_CONFLICTS_WITH_OTHER_TYPES))
	        {
	        	conflictChooser.setSelectedIndex(2);
	        }
        }
        if ( isResourceType)
        {
	        String annotation = dynamicType.getAnnotation( DynamicTypeAnnotations.KEY_LOCATION); 
	        if (annotation  == null)
	        {
	        	annotation =  "false";
	        }
	        if ( annotation.equals( "true"))
	        {
	        	locationChooser.setSelectedIndex(0);
	        }
	        else
	        {
	        	locationChooser.setSelectedIndex(1);
	        }
        }
    }

    private void validate() throws RaplaException {
        Assert.notNull(dynamicType);
        if ( getName( dynamicType ).length() == 0)
            throw new RaplaException(getString("error.no_name"));

        if (dynamicType.getKey().equals("")) {
            throw new RaplaException(getI18n().format("error.no_key",""));
        }
        checkKey(dynamicType.getKey());
        Attribute[] attributes = dynamicType.getAttributes();
        for (int i=0;i<attributes.length;i++) {
            String key = attributes[i].getKey();
            if (key == null || key.trim().equals(""))
                throw new RaplaException(getI18n().format("error.no_key","(" + i + ")"));
            checkKey(key);
            for (int j=i+1;j<attributes.length;j++) {
                if ((key.equals(attributes[j].getKey()))) {
                    throw new UniqueKeyException(getI18n().format("error.not_unique",key));
                }
            }
        }
    }

    private void checkKey(String key) throws RaplaException {
        if (key.length() ==0)
            throw new RaplaException(getString("error.no_key"));
        if (!Tools.isKey(key) || key.length()>50) 
        {
            Object[] param = new Object[3];
            param[0] = key;
            param[1] = "'-', '_'";
            param[2] = "'_'";
            throw new RaplaException(getI18n().format("error.invalid_key", param));
        }


    }
}