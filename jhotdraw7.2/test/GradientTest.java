import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JSlider;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.action.ChangeGradientAction;
import org.jhotdraw.draw.action.ColorIcon;
import org.jhotdraw.draw.action.SelectionGradientColorChooserAction;
import org.jhotdraw.draw.action.SelectionGradientOpacityChooserAction;
import org.jhotdraw.gui.JAttributeSlider;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_GRADIENT;
import org.jhotdraw.samples.svg.figures.SVGRectFigure;
import org.jhotdraw.samples.svg.gui.FillToolBar;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Peter G. Andersen <peand13@student.sdu.dk>
 */
public class GradientTest {
    
    DrawingView drawingView = new DefaultDrawingView();
    DefaultDrawing drawing = new DefaultDrawing();
    List<Figure> figureList = new LinkedList<Figure>();
    DefaultDrawingEditor editor = new DefaultDrawingEditor();
    
    ActionEvent dummyAction = new ActionEvent(this, 0, "");
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        editor.setActiveView(drawingView);
        figureList.add(new SVGRectFigure());
        drawingView.setDrawing(drawing);
        drawingView.addToSelection(figureList);
    }
    
    @After
    public void tearDown() {
        drawingView.clearSelection();
        figureList.clear();
    }
    
    @Test
    public void testChangeLinearGradientColor() {
        // Make sure all figures get changed to linear gradient fill.
        Action toLinearGradientFillAction = new ChangeGradientAction(editor, FillToolBar.FillType.LINEAR_GRADIENT, new JPopupButton());
        toLinearGradientFillAction.actionPerformed(dummyAction);
        for(Figure f : figureList) {
            boolean isLinearGradient = (FILL_GRADIENT.get(f) instanceof LinearGradient);
            Assert.assertTrue("Figure did not get filled with a linear gradient.", isLinearGradient);
        }
        
        // Test color change.
        int stop = 0;
        Color color = Color.GREEN;
        Action toColorAction = new SelectionGradientColorChooserAction(editor, stop, FILL_GRADIENT, "wat", new ColorIcon(color), null, false);
        toColorAction.actionPerformed(dummyAction);
        for(Figure f : figureList) {
            Color[] colors = FILL_GRADIENT.get(f).getStopColors();
            Assert.assertEquals("Figure with linear gradient did not get changed to the right color.", colors[stop], color);
        }
    }
    
    @Test
    public void testChangeRadialGradientOpacity() {
        // Make sure all figures get changed to radial gradient fill.
        Action toRadialGradientFillAction = new ChangeGradientAction(editor, FillToolBar.FillType.RADIAL_GRADIENT, new JPopupButton());
        toRadialGradientFillAction.actionPerformed(dummyAction);
        for(Figure f : figureList) {
            boolean isRadialGradient = (FILL_GRADIENT.get(f) instanceof RadialGradient);
            Assert.assertTrue("Figure did not get filled with a radial gradient.", isRadialGradient);
        }
        
        // Test opacity change.
        int stop = 0;
        double opacity = 50;
        JAttributeSlider opacitySlider = new JAttributeSlider(JSlider.VERTICAL, 0, 100, 100);
        opacitySlider.setAttributeValue(opacity);
        Action toOpacityAction = new SelectionGradientOpacityChooserAction(editor, stop, opacitySlider, FILL_GRADIENT);
        toOpacityAction.actionPerformed(dummyAction);
        for(Figure f : figureList) {
            double[] opacities = FILL_GRADIENT.get(f).getStopOpacities();
            Assert.assertEquals("Figure with radial gradient did not get changed to the right opacity.", opacities[stop], opacity, 0.5);
        }
    }
}