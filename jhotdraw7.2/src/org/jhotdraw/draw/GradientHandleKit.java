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
            handles.add(new GradientHandle(f));
        }
        else if(g instanceof RadialGradient) {
            handles.add(new GradientHandle2(f));
        }
    }
    
    private static class GradientHandle extends AbstractHandle {
        
        public int dx, dy;
        
        public GradientHandle(Figure owner) {
            super(owner);
        }

        protected Rectangle basicGetBounds() {
            Rectangle r = new Rectangle(locate());
            r.grow(getHandlesize() / 2 + 1, getHandlesize() / 2 + 1);
            return r;
        }
    
        private Point locate() {
            Figure owner = getOwner();
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                    r.x + (owner.getPreferredSize().getWidth() / 2) + dx, 
                    r.y + (owner.getPreferredSize().getHeight() / 2) + dy
                    );
            if (TRANSFORM.get(owner) != null) {
                TRANSFORM.get(owner).transform(p, p);
            }
            return view.drawingToView(p);
        }

        public void trackStart(Point anchor, int modifiersEx) {
            System.out.println("TRACK start " + anchor + " " + modifiersEx);
                  
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            System.out.println("TRACK step " + anchor + " " + lead + " " + modifiersEx);
            dx = lead.x - anchor.x;
            dy = lead.y - anchor.y;
            
            Figure f = getOwner();
            Point2D.Double p = view.viewToDrawing(lead);
            f.willChange();
            if (TRANSFORM.get(f) != null) {
                try {
                    TRANSFORM.get(f).inverseTransform(p, p);
                } catch (NoninvertibleTransformException ex) {

                }
            }
            f.changed();
        }

        public void trackEnd(Point anchor, Point lead, int modifiersEx) {
            System.out.println("TRACK end " + anchor + " " + lead + " " + modifiersEx);
            dx = 0;
            dy = 0;
            
        }

        @Override
        public void draw(Graphics2D g) {
            drawCircle(g, Color.ORANGE, Color.BLACK);
        }
        
    }
    
    private static class GradientHandle2 extends AbstractHandle {
        
        public int dx, dy;
        
        public GradientHandle2(Figure owner) {
            super(owner);
        }

        protected Rectangle basicGetBounds() {
            Rectangle r = new Rectangle(locate());
            r.grow(getHandlesize() / 2 + 1, getHandlesize() / 2 + 1);
            return r;
        }
    
        private Point locate() {
            Figure owner = getOwner();
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                    r.x + (owner.getPreferredSize().getWidth() / 2) + dx, 
                    r.y + (owner.getPreferredSize().getHeight() / 2) + dy
                    );
            if (TRANSFORM.get(owner) != null) {
                TRANSFORM.get(owner).transform(p, p);
            }
            return view.drawingToView(p);
        }

        public void trackStart(Point anchor, int modifiersEx) {
            System.out.println("TRACK start " + anchor + " " + modifiersEx);
                  
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            System.out.println("TRACK step " + anchor + " " + lead + " " + modifiersEx);
            dx = lead.x - anchor.x;
            dy = lead.y - anchor.y;
            
            Figure f = getOwner();
            Point2D.Double p = view.viewToDrawing(lead);
            f.willChange();
            if (TRANSFORM.get(f) != null) {
                try {
                    TRANSFORM.get(f).inverseTransform(p, p);
                } catch (NoninvertibleTransformException ex) {

                }
            }
            f.changed();
        }

        public void trackEnd(Point anchor, Point lead, int modifiersEx) {
            System.out.println("TRACK end " + anchor + " " + lead + " " + modifiersEx);
            dx = 0;
            dy = 0;
            
        }

        @Override
        public void draw(Graphics2D g) {
            drawCircle(g, Color.BLUE, Color.BLACK);
        }
        
    }
}