package org.jhotdraw.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;
import org.jhotdraw.samples.svg.Gradient;
import org.jhotdraw.samples.svg.LinearGradient;
import org.jhotdraw.samples.svg.RadialGradient;
import org.jhotdraw.samples.svg.SVGAttributeKeys;
import static org.jhotdraw.samples.svg.SVGAttributeKeys.FILL_GRADIENT;

/**
 *
 * @author peter
 */
public class GradientHandleKit {
    
    public GradientHandleKit() {
        
    }
    
    static public void addGradientHandles(Figure f, Collection<Handle> handles) {
        Gradient g = f.getAttribute(SVGAttributeKeys.FILL_GRADIENT);
        
        if(g == null) {
            return;
        }
        
        if(g instanceof LinearGradient) {
            handles.add(new LinearGradientHandleOne(f));
            handles.add(new LinearGradientHandleTwo(f));
        }
        else if(g instanceof RadialGradient) {
            
        }
    }
    
    private static class LinearGradientHandleOne extends AbstractHandle {
        
        public LinearGradientHandleOne(Figure owner) {
            super(owner);
        }

        protected Rectangle basicGetBounds() {
            Rectangle r = new Rectangle(locate());
            r.grow(getHandlesize() / 2 + 1, getHandlesize() / 2 + 1);
            return r;
        }
    
        private Point locate() {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                r.x + (owner.getPreferredSize().getWidth() * g.getX1()),
                r.y + (owner.getPreferredSize().getHeight() * g.getY1())
            );
            if (TRANSFORM.get(owner) != null) {
                TRANSFORM.get(owner).transform(p, p);
            }
            return view.drawingToView(p);
        }

        public void trackStart(Point anchor, int modifiersEx) {
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Point2D.Double p = view.viewToDrawing(lead);
            owner.willChange();
            
            double x = (p.x - owner.getBounds().x) / owner.getPreferredSize().getWidth();
            double y = (p.y - owner.getBounds().y) / owner.getPreferredSize().getHeight();
            g.setGradientVector(x, y, g.getX2(), g.getY2());
            
            if (TRANSFORM.get(owner) != null) {
                try {
                    TRANSFORM.get(owner).inverseTransform(p, p);
                }
                catch (NoninvertibleTransformException ex) {
                }
                owner.changed();
            }
        }

        public void trackEnd(Point anchor, Point lead, int modifiersEx) {
            // TODO fireUndoableEdit
        }

        @Override
        public void draw(Graphics2D g) {
            drawCircle(g, Color.ORANGE, Color.BLACK);
        }
        
    }
    
    private static class LinearGradientHandleTwo extends AbstractHandle {
        
        public LinearGradientHandleTwo(Figure owner) {
            super(owner);
        }

        protected Rectangle basicGetBounds() {
            Rectangle r = new Rectangle(locate());
            r.grow(getHandlesize() / 2 + 1, getHandlesize() / 2 + 1);
            return r;
        }
    
        private Point locate() {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                    r.x + (owner.getPreferredSize().getWidth() * g.getX2()),
                    r.y + (owner.getPreferredSize().getHeight() * g.getY2())
                    );
            if (TRANSFORM.get(owner) != null) {
                TRANSFORM.get(owner).transform(p, p);
            }
            return view.drawingToView(p);
        }

        public void trackStart(Point anchor, int modifiersEx) {
            
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Point2D.Double p = view.viewToDrawing(lead);
            owner.willChange();
            
            double x = (p.x - owner.getBounds().x) / owner.getPreferredSize().getWidth();
            double y = (p.y - owner.getBounds().y) / owner.getPreferredSize().getHeight();
            g.setGradientVector(g.getX1(), g.getY1(), x, y);
            
            if (TRANSFORM.get(owner) != null) {
                try {
                    TRANSFORM.get(owner).inverseTransform(p, p);
                } catch (NoninvertibleTransformException ex) {

                }
            }
            owner.changed();
        }

        public void trackEnd(Point anchor, Point lead, int modifiersEx) {
            // TODO fireUndoableEdit
        }

        @Override
        public void draw(Graphics2D g) {
            drawCircle(g, Color.ORANGE, Color.BLACK);
        }
        
    }
}