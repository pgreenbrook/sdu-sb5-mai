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
 * Utility methods to create handles which control the gradients of a Figure.
 * No handles are added if the figure has no gradient as fill.
 * 
 * @author Peter G. Andersen <peand13@student.sdu.dk>
 */
public class GradientHandleKit {
    
    public static void addGradientHandles(Figure f, Collection<Handle> handles) {
        Gradient g = f.getAttribute(SVGAttributeKeys.FILL_GRADIENT);
        
        if(g == null) {
            return;
        }
        
        if(g instanceof LinearGradient) {
            handles.add(new LinearGradientHandleOne(f));
            handles.add(new LinearGradientHandleTwo(f));
        }
        else if(g instanceof RadialGradient) {
            handles.add(new RadialGradientCenterHandle(f));
            handles.add(new RadialGradientFocalPointHandle(f));
        }
    }
    
    
    private static abstract class AbstractGradientHandle extends AbstractHandle {
        
        public AbstractGradientHandle(Figure owner) {
            super(owner);
        }
        
        protected Rectangle basicGetBounds() {
            Rectangle r = new Rectangle(locate());
            r.grow(getHandlesize() / 2 + 1, getHandlesize() / 2 + 1);
            return r;
        }
        
        protected abstract Point locate();
        
        public Point transform(Point2D.Double point) {
            Figure owner = getOwner();
            if (TRANSFORM.get(owner) != null) {
                TRANSFORM.get(owner).transform(point, point);
            }
            return view.drawingToView(point);
        }
        
        public void inverseTransform(Point2D.Double point) {
            Figure owner = getOwner();
            if (TRANSFORM.get(owner) != null) {
                try {
                    TRANSFORM.get(owner).inverseTransform(point, point);
                } catch (NoninvertibleTransformException ex) {
                    // Ignore it.
                }
            }
        }
        
        public void trackStart(Point anchor, int modifiersEx) {
            // Do nothing.
        }
        
        public void trackEnd(Point anchor, Point lead, int modifiersEx) {
            // TODO fireUndoableEdit
        }
        
        @Override
        public void draw(Graphics2D g) {
            drawCircle(g, Color.ORANGE, Color.BLACK);
        }
    }
    
    
    
    private static class LinearGradientHandleOne extends AbstractGradientHandle {
        
        public LinearGradientHandleOne(Figure owner) {
            super(owner);
        }
    
        protected Point locate() {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                r.x + (owner.getPreferredSize().getWidth() * g.getX1()),
                r.y + (owner.getPreferredSize().getHeight() * g.getY1())
            );
            return transform(p);
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Point2D.Double p = view.viewToDrawing(lead);
            owner.willChange();
            
            double x = (p.x - owner.getBounds().x) / owner.getPreferredSize().getWidth();
            double y = (p.y - owner.getBounds().y) / owner.getPreferredSize().getHeight();
            g.setGradientVector(x, y, g.getX2(), g.getY2());
            
            inverseTransform(p);
            owner.changed();
        }
    }
    
    private static class LinearGradientHandleTwo extends AbstractGradientHandle {
        
        public LinearGradientHandleTwo(Figure owner) {
            super(owner);
        }
    
        protected Point locate() {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                r.x + (owner.getPreferredSize().getWidth() * g.getX2()),
                r.y + (owner.getPreferredSize().getHeight() * g.getY2())
            );
            return transform(p);
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            Figure owner = getOwner();
            LinearGradient g = (LinearGradient) owner.getAttribute(FILL_GRADIENT);
            
            Point2D.Double p = view.viewToDrawing(lead);
            owner.willChange();
            
            double x = (p.x - owner.getBounds().x) / owner.getPreferredSize().getWidth();
            double y = (p.y - owner.getBounds().y) / owner.getPreferredSize().getHeight();
            g.setGradientVector(g.getX1(), g.getY1(), x, y);
            
            inverseTransform(p);
            owner.changed();
        }
    }
    
    private static class RadialGradientCenterHandle extends AbstractGradientHandle {
        
        public RadialGradientCenterHandle(Figure owner) {
            super(owner);
        }
    
        protected Point locate() {
            Figure owner = getOwner();
            RadialGradient g = (RadialGradient) owner.getAttribute(FILL_GRADIENT);
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                r.x + (owner.getPreferredSize().getWidth() * g.getCX()),
                r.y + (owner.getPreferredSize().getHeight() * g.getCY())
            );
            return transform(p);
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            Figure owner = getOwner();
            RadialGradient g = (RadialGradient) owner.getAttribute(FILL_GRADIENT);
            
            Point2D.Double p = view.viewToDrawing(lead);
            owner.willChange();
            
            double x = (p.x - owner.getBounds().x) / owner.getPreferredSize().getWidth();
            double y = (p.y - owner.getBounds().y) / owner.getPreferredSize().getHeight();
            g.setGradientCircle(x, y, g.getR());
            
            double[] offsets = g.getStopOffsets();
            double a = g.getCX() - g.getFX();
            double b = g.getCY() - g.getFY();
            double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
           
            if(c < 1) {
                offsets[0] = c;
                g.setStops(offsets, g.getStopColors(), g.getStopOpacities());
            }
            
            inverseTransform(p);
            owner.changed();
        }
    }
    
    private static class RadialGradientFocalPointHandle extends AbstractGradientHandle {
        
        public RadialGradientFocalPointHandle(Figure owner) {
            super(owner);
        }
    
        protected Point locate() {
            Figure owner = getOwner();
            RadialGradient g = (RadialGradient) owner.getAttribute(FILL_GRADIENT);
            
            Rectangle2D.Double r = owner.getBounds();
            Point2D.Double p = new Point2D.Double(
                r.x + (owner.getPreferredSize().getWidth() * g.getFX()),
                r.y + (owner.getPreferredSize().getHeight() * g.getFY())
            );
            return transform(p);
        }

        public void trackStep(Point anchor, Point lead, int modifiersEx) {
            Figure owner = getOwner();
            RadialGradient g = (RadialGradient) owner.getAttribute(FILL_GRADIENT);
            
            Point2D.Double p = view.viewToDrawing(lead);
            owner.willChange();
            
            double x = (p.x - owner.getBounds().x) / owner.getPreferredSize().getWidth();
            double y = (p.y - owner.getBounds().y) / owner.getPreferredSize().getHeight();
            g.setFocalPoint(x, y);
            
            double[] offsets = g.getStopOffsets();
            double a = g.getCX() - g.getFX();
            double b = g.getCY() - g.getFY();
            double c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            
            if(c < 1) {
                offsets[0] = c;
                g.setStops(offsets, g.getStopColors(), g.getStopOpacities());
            }
            
            inverseTransform(p);
            owner.changed();
        }
    }
}