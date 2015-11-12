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
import org.jhotdraw.text.JavaNumberFormatter;
import javax.swing.border.*;
import org.jhotdraw.gui.*;
import org.jhotdraw.util.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.SliderUI;
import org.jhotdraw.app.JHotDrawFeatures;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.gui.plaf.palette.*;
import org.jhotdraw.text.ColorFormatter;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.*;

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

    public enum FillState {
        SOLID(" Solid Fill "),
        LINEAR_GRADIENT(" Linear Gradient "),
        RADIAL_GRADIENT(" Radial Gradient ");
        
        private String prettyName;
        
        private FillState(String prettyName) {
            this.prettyName = prettyName;
        }
        
        public String getPrettyName() {
            return prettyName;
        }
    }
    
    private SelectionComponentDisplayer displayer;
    private FillState fillState;

    /** Creates new instance. */
    public FillToolBar() {
        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.samples.svg.Labels");
        setName(labels.getString(getID() + ".toolbar"));
        setDisclosureStateCount(3);
        setFillState(FillState.SOLID);
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

        switch (state) {
            case 1:
                 {
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
                    gbc.insets = new Insets(0, 3, 0, 0);
                    selectorPanel.add(btn, gbc);
                    
                    // Create fill panel
                    JPanel fillPanel = FillPanelFactory.createPanel(fillState, state);
                    
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
                break;

            case 2:
                 {
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
                    btn = ButtonFactory.createGradientHandleSelectorButton(labels);
                    btn = ButtonFactory.createGradientHandleSelectorButton(getEditor(), this, labels);
                    btn.setText(fillState.getPrettyName());
                    gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.anchor = GridBagConstraints.FIRST_LINE_START;
                    selectorPanel.add(btn, gbc);
                    
                    // Create fill panel
                    JPanel fillPanel = FillPanelFactory.createPanel(fillState, state);

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
                break;
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
    
    public void setFillState(FillState fillState) {
        this.fillState = fillState;
        
        // Force UI rebuild if FillToolBar is shown.
        if(getParent() != null) {
            this.setDisclosureState(getDisclosureState());
        }
    }
    
    public FillState getFillState() {
        return fillState;
    }
    
    public static class FillPanelFactory {

        public static JPanel createPanel(FillToolBar.FillState fillState, int toolbarState) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            switch(fillState) {
                case SOLID:
                    panel = createSolidFillPanel(toolbarState);
                    break;
                    
                case LINEAR_GRADIENT:
                    panel = createLinearGradientPanel(toolbarState);
                    break;
                    
                case RADIAL_GRADIENT:
                    panel = createRadialGradientPanel(toolbarState);
                    break;
            }
            return panel;
        }
        
        private static JPanel createSolidFillPanel(int toolbarState) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            
            return panel;
        }
        
        private static JPanel createLinearGradientPanel(int toolbarState) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            
            return panel;
        }
        
        private static JPanel createRadialGradientPanel(int toolbarState) {
            JPanel panel = new JPanel(new GridBagLayout());
            
            
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
