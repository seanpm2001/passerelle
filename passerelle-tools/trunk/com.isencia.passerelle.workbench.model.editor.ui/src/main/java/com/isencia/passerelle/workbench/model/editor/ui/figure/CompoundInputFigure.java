package com.isencia.passerelle.workbench.model.editor.ui.figure;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.IBody;

public class CompoundInputFigure extends CompoundIOFigure {
	public static final String INPUT_PORT_NAME = "input";
	public final static ImageDescriptor IMAGE_DESCRIPTOR_ARROW = Activator
	.getImageDescriptor("icons/arrow-r.gif");

	public CompoundInputFigure(String name,Image image) {
		super(name,IMAGE_DESCRIPTOR_ARROW.createImage());
		addOutput(INPUT_PORT_NAME,INPUT_PORT_NAME);
		setBackgroundColor(ColorConstants.black);
	}

	@Override
	protected Color getBackGroundcolor() {
		return ColorConstants.white;
	}
	private class Body extends RectangleFigure implements IBody {
		/**
		 * @param s
		 */
		public Body() {
			BorderLayout layout = new BorderLayout();
			setLayoutManager(layout);
	
			setBackgroundColor(ColorConstants.white);
			setOpaque(true);
		}

	
		public void initClickable(Clickable clickable) {
			if (clickable != null) {
				add(clickable, BorderLayout.BOTTOM);
			}
		}

		protected void fillShape(Graphics graphics) {
			graphics.pushState();
			graphics.setForegroundColor(ColorConstants.white);
			graphics.setBackgroundColor(ColorConstants.white);
//			graphics.fillGradient(getBounds(), true);
			graphics.popState();
			
			graphics.setForegroundColor(ColorConstants.black);
			int centerx = bounds.getTop().x;
			int centery = bounds.y + (bounds.getBottom().y - bounds.getTop().y)/2;

			PointList arrow = new PointList();
			arrow.addPoint(centerx - (centerx - bounds.getLeft().x)/2, centery
					- (bounds.getBottom().y - centery) / 4);
			arrow.addPoint(centerx, centery
					- (bounds.getBottom().y - centery) / 4);

			arrow.addPoint(centerx,
					centery - (bounds.getBottom().y - centery) / 2);
			arrow.addPoint(centerx + (bounds.right() - centerx)/2,centery);
			arrow.addPoint(centerx ,
					centery + (bounds.getBottom().y - centery) / 2);
			arrow.addPoint(centerx, centery
					+ (bounds.getBottom().y - centery) / 4);
			arrow.addPoint(centerx - (centerx - bounds.getLeft().x)/2, centery
					+ (bounds.getBottom().y - centery) / 4);
			graphics.fillPolygon(arrow);
			graphics.drawPolyline(arrow);

		}

		public Dimension getPreferredSize(int wHint, int hHint) {
			Dimension size = getParent().getSize().getCopy();
			return size;
		}


		@Override
		public void initImage(Image image) {
				
		}

	}

	protected IFigure generateBody(Image image, Clickable clickable) {
		Body body = new Body();
		body.initImage(image);
		body.setBorder(new LineBorder());
		body.initClickable(clickable);

//		Triangle body = new Triangle();
//		body.setBackgroundColor(ColorConstants.white);
//		body.setForegroundColor(ColorConstants.black);
//		body.setSize(PORT_WIDTH,(DEFAULT_HEIGHT * 2)/3);
//		body.setDirection(PositionConstants.EAST);
//		body.setPreferredSize(PORT_WIDTH,DEFAULT_HEIGHT);
//		ImageFigure imageFigure = new ImageFigure(image);
//		imageFigure.setAlignment(PositionConstants.WEST);
//		imageFigure.setBorder(new MarginBorder(5, 5, 0, 0));
//		body.add(imageFigure, BorderLayout.TOP);
		return (body);
	}
	
}
