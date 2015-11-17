/*
 * @(#)SelectionColorChooserAction.java  2.0  2006-06-07
 *
 * Copyright (c) 1996-2006 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package org.jhotdraw.draw.action;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.jhotdraw.draw.*;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.SVGAttributeKeys;
/**
 * 
 */
public class SelectionGradientColorChooserAction extends AttributeAction {
    protected AttributeKey<Gradient> key;
    protected int stop;
    protected Color color;
    protected boolean chooseColor = false;

    protected static JColorChooser colorChooser;
    
    /** Creates a new instance. */
    public SelectionGradientColorChooserAction(DrawingEditor editor, int stop, AttributeKey<Gradient> key) {
        this(editor, stop, key, null, null);
    }
    /** Creates a new instance. */
    public SelectionGradientColorChooserAction(DrawingEditor editor, int stop, AttributeKey<Gradient> key, Icon icon) {
        this(editor, stop,  key, null, icon);
    }
    /** Creates a new instance. */
    public SelectionGradientColorChooserAction(DrawingEditor editor, int stop, AttributeKey<Gradient> key, String name) {
        this(editor, stop,  key, name, null);
    }
    public SelectionGradientColorChooserAction(DrawingEditor editor, int stop, final AttributeKey<Gradient> key, String name, Icon icon) {
        this(editor, stop,  key, name, icon, new HashMap<AttributeKey,Object>(), false);
    }
    
    public SelectionGradientColorChooserAction(DrawingEditor editor, int stop, final AttributeKey<Gradient> key, String name, Icon icon, Map<AttributeKey,Object> fixedAttributes, boolean chooseColor) {
        super(editor, fixedAttributes, name, icon);
        this.key = key;
        this.stop = stop;
        if(icon instanceof ColorIcon) {
            this.color = ((ColorIcon) icon).getColor();
        }
        this.chooseColor = chooseColor;
        putValue(AbstractAction.NAME, name);
        putValue(AbstractAction.SMALL_ICON, icon);
        super.setEnabled(true);
    }
    
    protected Color getInitialColor() {
        Color initialColor = null;
        
        DrawingView v = getEditor().getActiveView();
        if (v != null && v.getSelectedFigures().size() == 1) {
            Figure f = v.getSelectedFigures().iterator().next();
            Gradient g = key.get(f);
            Color[] colors = g.getStopColors();
            
            if(colors.length > stop) {
                initialColor = colors[stop];
            }
        }
        if (initialColor == null) {
            initialColor = getColorStop(getEditor().getDefaultAttribute(key).getStopColors(), stop);
            if (initialColor == null) {
                initialColor = Color.red;
            }
        }
        return initialColor;
    }
    
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (colorChooser == null) {
            colorChooser = new JColorChooser();
        }
        Color initialColor = getInitialColor();
        Color chosenColor = null;
        
        if(chooseColor) {
            // FIXME - Reuse colorChooser object instead of calling static method here.
            chosenColor = colorChooser.showDialog((Component) e.getSource(), labels.getString("attribute.color.text"), initialColor);
        }
        else if(color != null) {
            chosenColor = color;
        }
        
        if (chosenColor != null) {
            for(final Figure f : getView().getSelectedFigures()) {
                final Gradient newGradient = key.get(f);
                final Gradient oldGradient = key.get(f);

                f.willChange();
                Color[] colors = newGradient.getStopColors();
                colors[stop] = chosenColor;
                newGradient.setStops(newGradient.getStopOffsets(), colors, newGradient.getStopOpacities());
                f.setAttribute(SVGAttributeKeys.FILL_GRADIENT, newGradient);

                UndoableEdit edit = new AbstractUndoableEdit() {
                    @Override
                    public String getPresentationName() {
                        // TODO: Do this properly.
                        return "test"/*labels.getString("edit.ungroupSelection.text")*/;
                    }

                    @Override
                    public void redo() throws CannotRedoException {
                        super.redo();
                        f.setAttribute(SVGAttributeKeys.FILL_GRADIENT, newGradient);
                        getView().repaintHandles();
                    }

                    @Override
                    public void undo() throws CannotUndoException {
                        f.setAttribute(SVGAttributeKeys.FILL_GRADIENT, oldGradient);
                        getView().repaintHandles();
                        super.undo();
                    }
                };
                fireUndoableEditHappened(edit);
                getView().repaintHandles();
                f.changed();
            }
            
        }
    }
    
    private Color getColorStop(Color[] colors, int stop) {
        if(colors.length > stop) {
            return colors[stop];
        }
        return null;
    }

    public void selectionChanged(FigureSelectionEvent evt) {
        //setEnabled(getView().getSelectionCount() > 0);
    }
}
