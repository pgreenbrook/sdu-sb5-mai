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
import org.jhotdraw.samples.svg.gui.FillToolBar.GradientType;

/**
 *
 * @author peter
 */
public class ChangeGradientAction extends AbstractSelectedAction {

    private GradientType fillState;
    private JPopupButton popupButton;
    
    public ChangeGradientAction(DrawingEditor editor, GradientType fillState, JPopupButton popupButton) {
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
                LinearGradient linearGradient = new LinearGradient();
                linearGradient.setRelativeToFigureBounds(true);
                linearGradient.setGradientVector(0.6, 0.5, 0.4, 0.5);
                linearGradient.setStops(new double[]{0.4, 0.6}, new Color[]{Color.BLUE, Color.RED}, new double[]{1.0, 1.0});
                linearGradient.setTransform(AffineTransform.getRotateInstance(0.0));
                g = linearGradient;
                break;

            case RADIAL_GRADIENT:
                RadialGradient radialGradient = new RadialGradient();
                radialGradient.setRelativeToFigureBounds(true);
                radialGradient.setGradientCircle(0.6, 0.5, 0.4);
                radialGradient.setStops(new double[]{0.4, 0.6}, new Color[]{Color.BLUE, Color.RED}, new double[]{1.0, 1.0});
                radialGradient.setTransform(AffineTransform.getRotateInstance(0.0));
                g = radialGradient;
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
