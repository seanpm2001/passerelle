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

	public CompoundInputFigure(String name,Class type) {
		super(name,type);
		addOutput(INPUT_PORT_NAME, INPUT_PORT_NAME);
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
			// graphics.fillGradient(getBounds(), true);
			graphics.popState();

			graphics.setForegroundColor(ColorConstants.black);
			int centerx = bounds.getTop().x;
			int centery = bounds.y + (bounds.getBottom().y - bounds.getTop().y)
					/ 2;

			PointList arrow = new PointList();
			arrow.addPoint(centerx - (centerx - bounds.getLeft().x) / 2,
					centery - (bounds.getBottom().y - centery) / 4);
			arrow.addPoint(centerx, centery - (bounds.getBottom().y - centery)
					/ 4);

			arrow.addPoint(centerx, centery - (bounds.getBottom().y - centery)
					/ 2);
			arrow.addPoint(centerx + (bounds.right() - centerx) / 2, centery);
			arrow.addPoint(centerx, centery + (bounds.getBottom().y - centery)
					/ 2);
			arrow.addPoint(centerx, centery + (bounds.getBottom().y - centery)
					/ 4);
			arrow.addPoint(centerx - (centerx - bounds.getLeft().x) / 2,
					centery + (bounds.getBottom().y - centery) / 4);
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

	protected IFigure generateBody(Image image, Clickable[] clickables) {
		Body body = new Body();
		body.setBorder(new LineBorder());
		for (Clickable clickable : clickables)
			body.initClickable(clickable);
		return (body);
	}

}
