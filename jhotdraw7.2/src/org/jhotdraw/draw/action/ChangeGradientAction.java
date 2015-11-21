package org.jhotdraw.draw.action;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import javax.swing.Action;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;
import org.jhotdraw.samples.svg.SVGAttributeKeys;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_GRADIENT;
import org.jhotdraw.samples.svg.gui.FillToolBar.FillType;

/**
 *
 * @author Peter G. Andersen <peand13@student.sdu.dk>
 */
public class ChangeGradientAction extends AbstractSelectedAction {

    private FillType fillState;
    private JPopupButton popupButton;
    
    public ChangeGradientAction(DrawingEditor editor, FillType fillState, JPopupButton popupButton) {
        super(editor);
        this.fillState = fillState;
        this.popupButton = popupButton;
        putValue(Action.DEFAULT, fillState.getPrettyName());
        putValue(Action.NAME, fillState.getPrettyName());
    }

    public void actionPerformed(ActionEvent ae) {
        popupButton.setText(fillState.getPrettyName());
        
        Gradient g = null;
        
        switch(this.fillState) {
            case LINEAR_GRADIENT:
                g = LinearGradient.createDefaultGradient();
                break;

            case RADIAL_GRADIENT:
                g = RadialGradient.createDefaultGradient();
                break;
        }
        
        getEditor().setDefaultAttribute(FILL_GRADIENT, g);
        
        for(final Figure f : getView().getSelectedFigures()) {
            final Gradient newGradient = g;
            final Gradient oldGradient = f.getAttribute(SVGAttributeKeys.FILL_GRADIENT);
            
            if(newGradient == null && oldGradient == null) {
                continue;
            }
            if(newGradient != null && oldGradient != null && newGradient.getClass() == oldGradient.getClass()) {
                continue;
            }
            f.willChange();
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
            getView().setHandleDetailLevel(-1);
            getView().repaintHandles();
            f.changed();
        }
    }
}
