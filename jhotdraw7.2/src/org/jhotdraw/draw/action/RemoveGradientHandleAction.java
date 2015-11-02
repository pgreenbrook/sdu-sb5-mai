package org.jhotdraw.draw.action;

import java.awt.event.ActionEvent;
import org.jhotdraw.draw.DrawingEditor;

/**
 *
 * @author peter
 */
public class RemoveGradientHandleAction extends AbstractSelectedAction  {

    public RemoveGradientHandleAction(DrawingEditor editor) {
        super(editor);
    }

    public void actionPerformed(ActionEvent ae) {
        System.out.println("Remove gradient handle");
    }
    
}