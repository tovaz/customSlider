package slider.control;

import javafx.scene.Node;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;

public class TimeTick {
	private double position; // central time of this tick in seconds
	private boolean major;
	private double angleSpan;
	private Path arc;
	private Rotate rotate;



	public TimeTick(double position, boolean major, double angleSpan, String cssClass) {
		this.position = position;
		this.major = major;
		this.angleSpan = angleSpan;

		arc = new Path();
		arc.setMouseTransparent(true);
		arc.getStyleClass().add(cssClass);
		arc.setStrokeLineCap(StrokeLineCap.BUTT);
//		arc.setFill(null);
//		arc.setStroke(paint);
		arc.getTransforms().add(rotate = new Rotate());
	}


	public void fitBounds(double relativeArcWidth) {
		double sin2 = Math.sin(angleSpan / 2);
		double cos2 = Math.cos(angleSpan / 2);

		arc.setStrokeWidth(relativeArcWidth);
		arc.getElements().clear();
		arc.getElements().add(new MoveTo(-sin2, - cos2));
		arc.getElements().add(new ArcTo(1, 1, 0,
				sin2, - cos2,
				false, true));
	}

	public void updatePosition(double min, double max) {
		double frac = (position-min) / (max-min);
		rotate.setAngle(frac * 360);
	}

	public Node getNode() {
		return arc;
	}

	public boolean isMajor() {
		return major;
	}
}
