package org.jhotdraw.draw.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.samples.svg.gui.FillToolBar.FillState;

/**
 *
 * @author peter
 */
public class ChangeFillstateAction extends AbstractSelectedAction {

    private FillState fillState;
    private JPopupButton popupButton;
    
    public ChangeFillstateAction(DrawingEditor editor, FillState fillState, JPopupButton popupButton) {
        super(editor);
        this.fillState = fillState;
        this.popupButton = popupButton;
        putValue(Action.DEFAULT, fillState.getPrettyName());
        putValue(Action.NAME, fillState.getPrettyName());
    }

    public void actionPerformed(ActionEvent ae) {
        popupButton.setText(fillState.getPrettyName());
    }
    
}
