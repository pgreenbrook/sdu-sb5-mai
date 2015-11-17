/*
 * @(#)FillToolBar.java  1.2  2008-05-23
 *
 * Copyright (c) 2007-2008 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */
package org.jhotdraw.samples.svg.gui;

import dk.sdu.mmmi.featuretracer.lib.FeatureEntryPoint;
import javax.swing.border.*;
import org.jhotdraw.util.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import static javax.swing.SwingConstants.SOUTH_EAST;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.SliderUI;
import org.jhotdraw.app.JHotDrawFeatures;
import org.jhotdraw.draw.*;
import static org.jhotdraw.draw.AttributeKeys.FILL_COLOR;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.FigureAttributeEditorHandler;
import org.jhotdraw.gui.JAttributeSlider;
import org.jhotdraw.gui.JAttributeTextField;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.gui.plaf.palette.*;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_GRADIENT;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_OPACITY;
import org.jhotdraw.text.ColorFormatter;
import org.jhotdraw.text.JavaNumberFormatter;

/**
 * FillToolBar.
 *
 * @author Werner Randelshofer
 * @version 1.2 2008-05-23 Hide the toolbar if nothing is selected, and no
 * creation tool is active. 
 * <br>1.1 2008-03-26 Don't draw button borders. 
 * <br>1.0 May 1, 2007 Created.
 */
public class FillToolBar extends AbstractToolBar {

    public enum GradientType {
        SOLID(" Solid Fill "),
        LINEAR_GRADIENT(" Linear Gradient "),
        RADIAL_GRADIENT(" Radial Gradient ");
        
        private String prettyName;
        
        private GradientType(String prettyName) {
            this.prettyName = prettyName;
        }
        
        public String getPrettyName() {
            return prettyName;
        }
    }
    
    private SelectionComponentDisplayer displayer;
    private GradientType fillState;

    /** Creates new instance. */
    public FillToolBar() {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.samples.svg.Labels");
        setName(labels.getString(getID() + ".toolbar"));
        setDisclosureStateCount(3);
        setFillState(GradientType.SOLID);
    }

    @Override
    public void setEditor(DrawingEditor newValue) {
        DrawingEditor oldValue = getEditor();
        if (displayer != null) {
            displayer.dispose();
            displayer = null;
        }
        super.setEditor(newValue);
        if (newValue != null) {
            displayer = new SelectionComponentDisplayer(editor, this);
        }
    }

    @Override
    @FeatureEntryPoint(JHotDrawFeatures.FILL_PALETTE)
    protected JComponent createDisclosedComponent(int state) {
        JPanel p = null;
        
        if(state == 1 || state == 2) {
            p = new JPanel();
            p.setOpaque(false);
            
            JPanel selectorPanel = new JPanel(new GridBagLayout());
            selectorPanel.setOpaque(false);
                    
            p.setBorder(new EmptyBorder(5, 5, 5, 8));
            p.removeAll();

            ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.samples.svg.Labels");
            GridBagLayout layout = new GridBagLayout();
            p.setLayout(layout);
            GridBagConstraints gbc;
            AbstractButton btn;
            
            // Gradient-handle selector
            btn = ButtonFactory.createGradientHandleSelectorButton(getEditor(), this, labels);
            btn.setUI((PaletteButtonUI) PaletteButtonUI.createUI(btn));
            btn.setText(fillState.getPrettyName());
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            selectorPanel.add(btn, gbc);
            
            // Create fill panel
            JPanel fillPanel = FillPanelFactory.createPanel(this, state, getEditor(), labels);
            fillPanel.setOpaque(false);
 
            // Add horizontal strips
            gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            p.add(selectorPanel, gbc);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 0, 0, 0);
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            p.add(fillPanel, gbc);
        }
        return p;
    }

    @Override
    protected String getID() {
        return "fill";
    }
    
    @Override
    protected int getDefaultDisclosureState() {
        return 1;
    }
    
    public void setFillState(GradientType fillState) {
        this.fillState = fillState;
        
        // Force rebuild of the FillToolBar panels.
        // Ensures that the fill-state is the same on all FillToolBar panels, they would get out of sync otherwise.
        if(getParent() != null) {
            super.rebuildPanels();
        }
    }
    
    public GradientType getFillState() {
        return fillState;
    }
    
    public static class FillPanelFactory {

        public static JPanel createPanel(FillToolBar fillToolBar, int toolbarState, DrawingEditor editor, ResourceBundleUtil labels) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            switch(fillToolBar.getFillState()) {
                case SOLID:
                    panel = createSolidFillPanel(fillToolBar, toolbarState, editor, labels);
                    break;
                     
                case LINEAR_GRADIENT:
                    panel = createLinearGradientPanel(fillToolBar, toolbarState, editor, labels);
                    break;
                     
                case RADIAL_GRADIENT:
                    panel = createRadialGradientPanel(fillToolBar, toolbarState, editor, labels);
                    break;
             }
            return panel;
        }
        
        private static JPanel createSolidFillPanel(FillToolBar fillToolBar, int toolbarState, DrawingEditor editor, ResourceBundleUtil labels) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            JPanel rowOne = new JPanel(new GridBagLayout());
            JPanel rowTwo = new JPanel(new GridBagLayout());
            
            // Row one, fill color field and button
            Map<AttributeKey, Object> defaultAttributes = new HashMap<AttributeKey, Object>();
            FILL_GRADIENT.set(defaultAttributes, null);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            if(toolbarState == 2) {
                JAttributeTextField<Color> colorField = new JAttributeTextField<Color>();
                colorField.setColumns(7);
                colorField.setToolTipText(labels.getString("attribute.fillColor.toolTipText"));
                colorField.putClientProperty("Palette.Component.segmentPosition", "first");
                colorField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(colorField));
                colorField.setFormatterFactory(ColorFormatter.createFormatterFactory());
                colorField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Color>(FILL_COLOR, defaultAttributes, colorField, editor, true);
                rowOne.add(colorField, gbc);
            }
            JButton btn = ButtonFactory.createSelectionColorButton(editor,
                    FILL_COLOR, ButtonFactory.HSV_COLORS, ButtonFactory.HSV_COLORS_COLUMN_COUNT,
                    "attribute.fillColor", labels, defaultAttributes, new Rectangle(3, 3, 10, 10));
            btn.setUI((PaletteButtonUI) PaletteButtonUI.createUI(btn));
            ((JPopupButton) btn).setAction(null, null);
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            rowOne.add(btn, gbc);
            
            
            // Row two, opacity field with slider
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.insets = new Insets(3, 0, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            if(toolbarState == 2) {
                JAttributeTextField<Double> opacityField = new JAttributeTextField<Double>();
                opacityField.setColumns(3);
                opacityField.setToolTipText(labels.getString("attribute.fillOpacity.toolTipText"));
                opacityField.putClientProperty("Palette.Component.segmentPosition", "first");
                opacityField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(opacityField));
                opacityField.setFormatterFactory(JavaNumberFormatter.createFormatterFactory(0d, 1d, 100d));
                opacityField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacityField, editor);
                rowTwo.add(opacityField, gbc);
            }
            JPopupButton opacityPopupButton = new JPopupButton();
            JAttributeSlider opacitySlider = new JAttributeSlider(JSlider.VERTICAL, 0, 100, 100);
            opacityPopupButton.add(opacitySlider);
            labels.configureToolBarButton(opacityPopupButton, "attribute.fillOpacity");
            opacityPopupButton.setUI((PaletteButtonUI) PaletteButtonUI.createUI(opacityPopupButton));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            opacityPopupButton.setIcon(
                    new SelectionOpacityIcon(editor, FILL_OPACITY, FILL_COLOR, null, fillToolBar.getClass().getResource(labels.getString("attribute.fillOpacity.icon")),
                    new Rectangle(5, 5, 6, 6), new Rectangle(4, 4, 7, 7)));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            new SelectionComponentRepainter(editor, opacityPopupButton);
            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.weighty = 1f;
            gbc.insets = new Insets(3, 0, 0, 0);
            rowTwo.add(opacityPopupButton, gbc);
            opacitySlider.setUI((SliderUI) PaletteSliderUI.createUI(opacitySlider));
            opacitySlider.setScaleFactor(100d);
            new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacitySlider, editor);
            
            gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            panel.add(rowOne, gbc);
            gbc = new GridBagConstraints();
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            panel.add(rowTwo, gbc);
            
            return panel;
        }
        
        private static JPanel createLinearGradientPanel(FillToolBar fillToolBar, int toolbarState, DrawingEditor editor, ResourceBundleUtil labels) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            JPanel rowOne = new JPanel(new GridBagLayout());
            JPanel rowTwo = new JPanel(new GridBagLayout());
            
            // Row one, fill color field and button, opacity slider. (stop 1)
            Map<AttributeKey, Object> defaultAttributes = new HashMap<AttributeKey, Object>();
            FILL_GRADIENT.set(defaultAttributes, null);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            JLabel label = new JLabel();
            label.setText("1: ");
            label.setUI((LabelUI) PaletteLabelUI.createUI(label));
            rowOne.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Color> colorField = new JAttributeTextField<Color>();
                colorField.setColumns(5);
                colorField.setToolTipText(labels.getString("attribute.fillColor.toolTipText"));
                colorField.putClientProperty("Palette.Component.segmentPosition", "first");
                colorField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(colorField));
                colorField.setFormatterFactory(ColorFormatter.createFormatterFactory());
                colorField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Color>(FILL_COLOR, defaultAttributes, colorField, editor, true);
                rowOne.add(colorField, gbc);
            }*/
            JButton btn = ButtonFactory.createSelectionGradientColorButton(editor,
                0, FILL_GRADIENT, ButtonFactory.HSV_COLORS, ButtonFactory.HSV_COLORS_COLUMN_COUNT,
                "attribute.fillColor", labels, defaultAttributes, new Rectangle(3, 3, 10, 10));
            btn.setUI((PaletteButtonUI) PaletteButtonUI.createUI(btn));
            ((JPopupButton) btn).setAction(null, null);
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            rowOne.add(btn, gbc);
            
            gbc = new GridBagConstraints();
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Double> opacityField = new JAttributeTextField<Double>();
                opacityField.setColumns(3);
                opacityField.setToolTipText(labels.getString("attribute.fillOpacity.toolTipText"));
                opacityField.putClientProperty("Palette.Component.segmentPosition", "first");
                opacityField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(opacityField));
                opacityField.setFormatterFactory(JavaNumberFormatter.createFormatterFactory(0d, 1d, 100d));
                opacityField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacityField, editor);
                rowOne.add(opacityField, gbc);
            }*/
            JPopupButton opacityPopupButton = new JPopupButton();
            JAttributeSlider opacitySlider = new JAttributeSlider(JSlider.VERTICAL, 0, 100, 100);
            opacityPopupButton.add(opacitySlider);
            labels.configureToolBarButton(opacityPopupButton, "attribute.fillOpacity");
            opacityPopupButton.setUI((PaletteButtonUI) PaletteButtonUI.createUI(opacityPopupButton));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            opacityPopupButton.setIcon(
                    new SelectionOpacityIcon(editor, FILL_OPACITY, FILL_COLOR, null, fillToolBar.getClass().getResource(labels.getString("attribute.fillOpacity.icon")),
                    new Rectangle(5, 5, 6, 6), new Rectangle(4, 4, 7, 7)));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            new SelectionComponentRepainter(editor, opacityPopupButton);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.gridx = 4;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.weighty = 1f;
            rowOne.add(opacityPopupButton, gbc);
            opacitySlider.setUI((SliderUI) PaletteSliderUI.createUI(opacitySlider));
            opacitySlider.setScaleFactor(100d);
            new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacitySlider, editor);
            
            
            // Row two, fill color field and button, opacity slider. (stop 2)
            FILL_GRADIENT.set(defaultAttributes, null);
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            label = new JLabel();
            label.setText("2: ");
            label.setUI((LabelUI) PaletteLabelUI.createUI(label));
            rowOne.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Color> colorField = new JAttributeTextField<Color>();
                colorField.setColumns(5);
                colorField.setToolTipText(labels.getString("attribute.fillColor.toolTipText"));
                colorField.putClientProperty("Palette.Component.segmentPosition", "first");
                colorField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(colorField));
                colorField.setFormatterFactory(ColorFormatter.createFormatterFactory());
                colorField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Color>(FILL_COLOR, defaultAttributes, colorField, editor, true);
                rowOne.add(colorField, gbc);
            }*/
            btn = ButtonFactory.createSelectionGradientColorButton(editor,
                1, FILL_GRADIENT, ButtonFactory.HSV_COLORS, ButtonFactory.HSV_COLORS_COLUMN_COUNT,
                "attribute.fillColor", labels, defaultAttributes, new Rectangle(3, 3, 10, 10));
            btn.setUI((PaletteButtonUI) PaletteButtonUI.createUI(btn));
            ((JPopupButton) btn).setAction(null, null);
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            rowOne.add(btn, gbc);
            
            gbc = new GridBagConstraints();
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Double> opacityField = new JAttributeTextField<Double>();
                opacityField.setColumns(3);
                opacityField.setToolTipText(labels.getString("attribute.fillOpacity.toolTipText"));
                opacityField.putClientProperty("Palette.Component.segmentPosition", "first");
                opacityField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(opacityField));
                opacityField.setFormatterFactory(JavaNumberFormatter.createFormatterFactory(0d, 1d, 100d));
                opacityField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacityField, editor);
                rowOne.add(opacityField, gbc);
            }*/
            opacityPopupButton = new JPopupButton();
            opacitySlider = new JAttributeSlider(JSlider.VERTICAL, 0, 100, 100);
            opacityPopupButton.add(opacitySlider);
            labels.configureToolBarButton(opacityPopupButton, "attribute.fillOpacity");
            opacityPopupButton.setUI((PaletteButtonUI) PaletteButtonUI.createUI(opacityPopupButton));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            opacityPopupButton.setIcon(
                    new SelectionOpacityIcon(editor, FILL_OPACITY, FILL_COLOR, null, fillToolBar.getClass().getResource(labels.getString("attribute.fillOpacity.icon")),
                    new Rectangle(5, 5, 6, 6), new Rectangle(4, 4, 7, 7)));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            new SelectionComponentRepainter(editor, opacityPopupButton);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.gridx = 4;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.weighty = 1f;
            rowOne.add(opacityPopupButton, gbc);
            opacitySlider.setUI((SliderUI) PaletteSliderUI.createUI(opacitySlider));
            opacitySlider.setScaleFactor(100d);
            new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacitySlider, editor);
            
            
            
            
            gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            panel.add(rowOne, gbc);
            gbc.insets = new Insets(3, 0, 0, 0);
            gbc = new GridBagConstraints();
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            panel.add(rowTwo, gbc);
            
            return panel;
        }
        
        private static JPanel createRadialGradientPanel(FillToolBar fillToolBar, int toolbarState, DrawingEditor editor, ResourceBundleUtil labels) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            JPanel rowOne = new JPanel(new GridBagLayout());
            JPanel rowTwo = new JPanel(new GridBagLayout());
            
            // Row one, fill color field and button, opacity slider. (stop 1)
            Map<AttributeKey, Object> defaultAttributes = new HashMap<AttributeKey, Object>();
            FILL_GRADIENT.set(defaultAttributes, null);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            JLabel label = new JLabel();
            label.setText("1: ");
            label.setUI((LabelUI) PaletteLabelUI.createUI(label));
            rowOne.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Color> colorField = new JAttributeTextField<Color>();
                colorField.setColumns(5);
                colorField.setToolTipText(labels.getString("attribute.fillColor.toolTipText"));
                colorField.putClientProperty("Palette.Component.segmentPosition", "first");
                colorField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(colorField));
                colorField.setFormatterFactory(ColorFormatter.createFormatterFactory());
                colorField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Color>(FILL_COLOR, defaultAttributes, colorField, editor, true);
                rowOne.add(colorField, gbc);
            }*/
            JButton btn = ButtonFactory.createSelectionGradientColorButton(editor,
                0, FILL_GRADIENT, ButtonFactory.HSV_COLORS, ButtonFactory.HSV_COLORS_COLUMN_COUNT,
                "attribute.fillColor", labels, defaultAttributes, new Rectangle(3, 3, 10, 10));
            btn.setUI((PaletteButtonUI) PaletteButtonUI.createUI(btn));
            ((JPopupButton) btn).setAction(null, null);
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            rowOne.add(btn, gbc);
            
            gbc = new GridBagConstraints();
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Double> opacityField = new JAttributeTextField<Double>();
                opacityField.setColumns(3);
                opacityField.setToolTipText(labels.getString("attribute.fillOpacity.toolTipText"));
                opacityField.putClientProperty("Palette.Component.segmentPosition", "first");
                opacityField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(opacityField));
                opacityField.setFormatterFactory(JavaNumberFormatter.createFormatterFactory(0d, 1d, 100d));
                opacityField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacityField, editor);
                rowOne.add(opacityField, gbc);
            }*/
            JPopupButton opacityPopupButton = new JPopupButton();
            JAttributeSlider opacitySlider = new JAttributeSlider(JSlider.VERTICAL, 0, 100, 100);
            opacityPopupButton.add(opacitySlider);
            labels.configureToolBarButton(opacityPopupButton, "attribute.fillOpacity");
            opacityPopupButton.setUI((PaletteButtonUI) PaletteButtonUI.createUI(opacityPopupButton));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            opacityPopupButton.setIcon(
                    new SelectionOpacityIcon(editor, FILL_OPACITY, FILL_COLOR, null, fillToolBar.getClass().getResource(labels.getString("attribute.fillOpacity.icon")),
                    new Rectangle(5, 5, 6, 6), new Rectangle(4, 4, 7, 7)));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            new SelectionComponentRepainter(editor, opacityPopupButton);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.gridx = 4;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.weighty = 1f;
            rowOne.add(opacityPopupButton, gbc);
            opacitySlider.setUI((SliderUI) PaletteSliderUI.createUI(opacitySlider));
            opacitySlider.setScaleFactor(100d);
            new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacitySlider, editor);
            
            
            // Row two, fill color field and button, opacity slider. (stop 2)
            FILL_GRADIENT.set(defaultAttributes, null);
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            label = new JLabel();
            label.setText("2: ");
            label.setUI((LabelUI) PaletteLabelUI.createUI(label));
            rowOne.add(label, gbc);
            
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Color> colorField = new JAttributeTextField<Color>();
                colorField.setColumns(5);
                colorField.setToolTipText(labels.getString("attribute.fillColor.toolTipText"));
                colorField.putClientProperty("Palette.Component.segmentPosition", "first");
                colorField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(colorField));
                colorField.setFormatterFactory(ColorFormatter.createFormatterFactory());
                colorField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Color>(FILL_COLOR, defaultAttributes, colorField, editor, true);
                rowOne.add(colorField, gbc);
            }*/
            btn = ButtonFactory.createSelectionGradientColorButton(editor,
                1, FILL_GRADIENT, ButtonFactory.HSV_COLORS, ButtonFactory.HSV_COLORS_COLUMN_COUNT,
                "attribute.fillColor", labels, defaultAttributes, new Rectangle(3, 3, 10, 10));
            btn.setUI((PaletteButtonUI) PaletteButtonUI.createUI(btn));
            ((JPopupButton) btn).setAction(null, null);
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            rowOne.add(btn, gbc);
            
            gbc = new GridBagConstraints();
            gbc.gridx = 3;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            /*if(toolbarState == 2) {
                JAttributeTextField<Double> opacityField = new JAttributeTextField<Double>();
                opacityField.setColumns(3);
                opacityField.setToolTipText(labels.getString("attribute.fillOpacity.toolTipText"));
                opacityField.putClientProperty("Palette.Component.segmentPosition", "first");
                opacityField.setUI((PaletteFormattedTextFieldUI) PaletteFormattedTextFieldUI.createUI(opacityField));
                opacityField.setFormatterFactory(JavaNumberFormatter.createFormatterFactory(0d, 1d, 100d));
                opacityField.setHorizontalAlignment(JTextField.LEFT);
                new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacityField, editor);
                rowOne.add(opacityField, gbc);
            }*/
            opacityPopupButton = new JPopupButton();
            opacitySlider = new JAttributeSlider(JSlider.VERTICAL, 0, 100, 100);
            opacityPopupButton.add(opacitySlider);
            labels.configureToolBarButton(opacityPopupButton, "attribute.fillOpacity");
            opacityPopupButton.setUI((PaletteButtonUI) PaletteButtonUI.createUI(opacityPopupButton));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            opacityPopupButton.setIcon(
                    new SelectionOpacityIcon(editor, FILL_OPACITY, FILL_COLOR, null, fillToolBar.getClass().getResource(labels.getString("attribute.fillOpacity.icon")),
                    new Rectangle(5, 5, 6, 6), new Rectangle(4, 4, 7, 7)));
            opacityPopupButton.setPopupAnchor(SOUTH_EAST);
            new SelectionComponentRepainter(editor, opacityPopupButton);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 3, 0, 0);
            gbc.gridx = 4;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.weighty = 1f;
            rowOne.add(opacityPopupButton, gbc);
            opacitySlider.setUI((SliderUI) PaletteSliderUI.createUI(opacitySlider));
            opacitySlider.setScaleFactor(100d);
            new FigureAttributeEditorHandler<Double>(FILL_OPACITY, opacitySlider, editor);
            
            
            
            gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            panel.add(rowOne, gbc);
            gbc.insets = new Insets(3, 0, 0, 0);
            gbc = new GridBagConstraints();
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            panel.add(rowTwo, gbc);
            
            return panel;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setOpaque(false);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
