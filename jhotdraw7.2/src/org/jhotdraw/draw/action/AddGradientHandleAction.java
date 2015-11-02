package org.jhotdraw.draw.action;

import java.awt.event.ActionEvent;
import org.jhotdraw.draw.DrawingEditor;

/**
 *
 * @author peter
 */
public class AddGradientHandleAction extends AbstractSelectedAction  {

    public AddGradientHandleAction(DrawingEditor editor) {
        super(editor);
    }

    public void actionPerformed(ActionEvent ae) {
        System.out.println("Add gradient handle");
    }
    
}
