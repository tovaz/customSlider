package slider.control;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import sun.security.validator.SimpleValidator;

public class CircularSliderSkin extends SkinBase<CircularSlider> {
    private Shape foregroundMask; // invisible, but throws shadow
    private DropShadow shadow;
    private Circle centralClip;

    private double barRadius, barWidth; // effective values, depend on layout size

    private Group centralGroup;
    private Scale centralScale;

    // Ticks
    private List<TimeTick> ticks;
    private Group tickGroup;
    private double tickScale = 0.0005;
    private Duration tickAnimationLength = new Duration(1000);
    private DoubleProperty currentTickOpacy;

    // Bar
    private Region styledBarBox;
    private Path bar, oldBar;
    private FadeTransition oldBarFade;
    private StackPane cssThumb, thumb;
    private double filledAngle = Double.NaN;
    private boolean onBar; // mouse pressed on bar or dragged from bar
    private Duration barFadeToZeroDuration = new Duration(600);

    // Value tooltips
    private Tooltip mouseTooltip, barTooltip;
    private Timeline barTooltipAnimation, mouseTooltipAnimation;
    private long barTooltipFadeInLength = 500, barTooltipFadeOutLength = 300, mouseTooltipFadeInLength = 50, mouseTooltipFadeOutLength = 500;

    private TextField valueText;

    public CircularSliderSkin(CircularSlider control) {
        super(control);


        shadow = new DropShadow();
        shadow.setColor(new Color(0, 0, 0, 0.3));

        centralGroup = new Group();
        centralGroup.setClip(centralClip = new Circle(1));
        centralGroup.getTransforms().add(centralScale = new Scale(1, 1));
        getChildren().add(centralGroup);

        
        
        
        // Bar
        bar = new Path();
        bar.setMouseTransparent(true);
        bar.setStrokeLineCap(StrokeLineCap.BUTT);

        oldBar = new Path();
        oldBar.setMouseTransparent(true);
        oldBar.setStrokeLineCap(StrokeLineCap.BUTT);
        //oldBar.setOpacity(0);
        centralGroup.getChildren().addAll(oldBar, bar);

        tickGroup = new Group();
        tickGroup.setMouseTransparent(true);
        centralGroup.getChildren().add(tickGroup);

        styledBarBox = new Region();
        styledBarBox.getStyleClass().add("bar");
        styledBarBox.setStyle("-fx-background-color: linear-gradient(to top right, #69F0AE, #40C4FF);");
        styledBarBox.setVisible(false);
        styledBarBox.setManaged(false);
        
        
        getChildren().add(styledBarBox);
    
        // ************************** AGREGANDO EL TEXTFIELD EN EL CENTRO
        valueText = new TextField();
        valueText.setText(String.valueOf( (int)getSkinnable().getValue()));
        AnchorPane panel = new AnchorPane();
        panel.setPrefWidth(styledBarBox.getWidth());
        panel.setPrefHeight(styledBarBox.getHeight());
        
        
        valueText.setPrefSize((getSkinnable().getPrefWidth()/2)-20, 30);
        valueText.setLayoutX( (getSkinnable().getPrefWidth()/2) - (valueText.getPrefWidth()/2) );
        valueText.setLayoutY( (getSkinnable().getPrefHeight()/2) - (valueText.getPrefHeight()/2) );
        
        valueText.getStyleClass().add("bar");
        valueText.setVisible(getSkinnable().isTextfieldVisible());
        panel.getChildren().add(valueText);
        getChildren().add(panel);
        
        // ************************** 
        
        // Thumb
        cssThumb = new StackPane() {
            @Override
            public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
                switch (attribute) {
                    case VALUE: { valueText.setText( String.valueOf( getSkinnable().getValue()) ); return getSkinnable().getValue(); }
                    default: return super.queryAccessibleAttribute(attribute, parameters);
        } } };
        cssThumb.setVisible(false);
        cssThumb.setMouseTransparent(true);
        cssThumb.setManaged(false);
        cssThumb.getStyleClass().setAll("thumb");
        
        
        getChildren().add(cssThumb);

        thumb = new StackPane() {
            @Override
            public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
                switch (attribute) {
                    case VALUE: { valueText.setText( String.valueOf( getSkinnable().getValue()) ); return getSkinnable().getValue(); }
                    default: return super.queryAccessibleAttribute(attribute, parameters);
                }
            }
        };
        thumb.setMouseTransparent(true);
        thumb.setOpacity(0);
        thumb.getStyleClass().setAll("thumb");
        thumb.setAccessibleRole(AccessibleRole.THUMB);
        getChildren().add(thumb);


        // Value tooltips
        mouseTooltip = new Tooltip();
//		mouseTooltip.setAutoHide(true);
        //mouseTooltip.setOpacity(0);

        barTooltip = new Tooltip();
//		barTooltip.setAutoHide(true);
        //barTooltip.setOpacity(0);
        barTooltip.textProperty().bindBidirectional(control.valueProperty(), new StringConverter<Number>() {

            @Override
            public String toString(Number object) {
                Double x = (double) object;
                return String.valueOf( x.intValue() );
            }

            @Override
            public Number fromString(String string) {
                return getLabelFormatter().fromString(string);
            }
        });


        // Register listeners

        control.maxProperty().addListener((p,o,n) -> {rebuildTicks(); rebuildBar();});
        control.minProperty().addListener((p,o,n) -> {rebuildTicks(); rebuildBar();});
        control.valueProperty().addListener(e -> rebuildBar());

        control.tickLengthProperty().addListener(e -> rebuildTicks());
        control.minorTickLengthProperty().addListener(e -> rebuildTicks());
        control.majorTickUnitProperty().addListener(e -> rebuildTicks());
        control.minorTickCountProperty().addListener(e -> rebuildTicks());

        control.minorTickVisibleProperty().addListener(e -> updateTickVisibility());
        control.tickMarkVisibleProperty().addListener(e -> updateTickVisibility());
        
        control.TextfieldVisibleProperty().addListener( e -> updateTextfieldVisibility());

        styledBarBox.backgroundProperty().addListener(e -> updateBarStyle());

        getSkinnable().setOnMouseMoved(e -> updateMouseOver(e, false));
        getSkinnable().setOnMouseDragged(e -> updateMouseOver(e, true));

        getSkinnable().setOnMousePressed(e -> { 
            mouseClick = true;
        });
        
        getSkinnable().setOnMouseExited(e -> {
            mouseTooltipAnimation = fadeTooltip(mouseTooltipFadeOutLength, 0, mouseTooltip, mouseTooltipAnimation);
            thumb.setOpacity(0);
            barTooltipAnimation = fadeTooltip(barTooltipFadeOutLength, 0, barTooltip, barTooltipAnimation);
        });
        
        
        getSkinnable().setOnMouseEntered(e -> {
            updateMouseOver(e, false);
            //barTooltipAnimation = fadeTooltip(barTooltipFadeInLength, 1, barTooltip, barTooltipAnimation);
        });

        getSkinnable().setOnMouseReleased(e -> {
            mouseClick = false;
            if(onBar) {
                getSkinnable().setValue((int)getValueAt(e.getX(), e.getY()));
                valueText.setText( String.valueOf( (int)getSkinnable().getValue() ) );
                showTooltipAtPos(barTooltip, getSkinnable().getValue());
            }
        });
        
        valueText.setOnKeyReleased(e -> {
            if (!valueText.getText().isEmpty()) {
                int valor = Integer.parseInt( valueText.getText() );
                if ( valor >= 0) {
                    getSkinnable().setValue( valor );
                    showTooltipAtPos(barTooltip, getSkinnable().getValue());
                }
                else{
                    valor = 0;
                    valueText.setText("0");
                    getSkinnable().setValue( valor );
                    showTooltipAtPos(barTooltip, getSkinnable().getValue());
                }
            }
        });
        
    }

    private boolean mouseClick;
    private Timeline fadeTooltip(long duration, double targetOpacity, Tooltip barTooltip, Timeline barTooltipAnimation) {
        if(barTooltipAnimation != null && barTooltipAnimation.getStatus() == Animation.Status.RUNNING) {
            barTooltipAnimation.stop();
        }
        
        if(targetOpacity > 0 && !barTooltip.isShowing()) {
            showTooltipAtPos(barTooltip, getSkinnable().getValue());
        }
        
        barTooltipAnimation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(barTooltip.opacityProperty(), barTooltip.getOpacity())),
                new KeyFrame(new Duration(duration), new KeyValue(barTooltip.opacityProperty(), targetOpacity)));
        
        if(targetOpacity == 0) {
            barTooltipAnimation.setOnFinished(e -> {
                barTooltip.hide();
            });
        }
        
        barTooltipAnimation.play();
        return barTooltipAnimation;
         
    }


    private void updateMouseOver(MouseEvent e, boolean isDragged) {
        double angle = getAngle(e.getX(), e.getY());
        int pos = (int)getValueAt(e.getX(), e.getY());
        onBar = isDragged || isOnBar(e.getX(), e.getY());

        // update tooltip
        if(onBar) {
            mouseTooltip.setText(getLabelAt(pos));
            showTooltipAtAngle(mouseTooltip, angle);
            mouseTooltipAnimation = fadeTooltip(mouseTooltipFadeInLength, 1, mouseTooltip, mouseTooltipAnimation);
        } else {
            mouseTooltipAnimation = fadeTooltip(mouseTooltipFadeOutLength, 0, mouseTooltip, mouseTooltipAnimation);
        }

        // update thumb
        Point2D thumbLoc = getLocationFromAngle(angle, barRadius);
        thumb.setLayoutX(thumbLoc.getX() - thumb.getWidth()/2);
        thumb.setLayoutY(thumbLoc.getY() - thumb.getHeight()/2);
        thumb.setOpacity(onBar ? 1 : 0.4);
        
        if (mouseClick){
            getSkinnable().setValue((int)getValueAt(e.getX(), e.getY()));
            valueText.setText( String.valueOf( (int)getSkinnable().getValue() ) );
            showTooltipAtPos(barTooltip, getSkinnable().getValue());
        }

    }

    public double getAngle(double x, double y) {
        double dx = x - getSkinnable().getWidth()/2;
        double dy = y - getSkinnable().getHeight()/2;
        double angle = Math.atan2(dx, -dy);
        if(angle < 0) angle += 2*Math.PI;
        return angle;
    }

    private double getValueAt(double x, double y) {
        return getAngle(x,y) / (2*Math.PI) * (getSkinnable().getMax()-getSkinnable().getMin()) + getSkinnable().getMin();
    }

    private String getLabelAt(double pos) {
        return String.valueOf((int)pos);
    }

    private StringConverter<Double> getLabelFormatter() {
        StringConverter<Double> formatter = getSkinnable().getLabelFormatter();
        if(formatter == null) formatter = new DoubleStringConverter();
        return formatter;
    }

    private Point2D getLocationFromAngle(double angle, double rad) {
        return new Point2D(rad*Math.sin(angle)+getSkinnable().getWidth()/2, -rad*Math.cos(angle)+getSkinnable().getHeight()/2);
    }

    private void showTooltipAtPos(Tooltip tooltip, double pos) {
        double angle = 2*Math.PI * (pos-getSkinnable().getMin()) / (getSkinnable().getMax() - getSkinnable().getMin());
        if(Double.isNaN(angle)) angle = 0;
        showTooltipAtAngle(tooltip, (int)angle);
    }

    private void showTooltipAtAngle(Tooltip tooltip, double angle) {
        double rad = barRadius + barWidth/2;
        double relPos = angle / 2 / Math.PI;
        showTooltipAt(tooltip, getLocationFromAngle(angle, rad), relPos < 0.5, relPos > 0.25 && relPos < 0.75);
    }

    private void showTooltipAt(Tooltip tooltip, Point2D point, boolean left, boolean top) {
        CircularSlider control = getSkinnable();
        point = control.localToScreen(point);
        tooltip.setAnchorLocation(left ?
            (top ? AnchorLocation.WINDOW_TOP_LEFT : AnchorLocation.WINDOW_BOTTOM_LEFT) :
            (top ? AnchorLocation.WINDOW_TOP_RIGHT : AnchorLocation.WINDOW_BOTTOM_RIGHT));
        tooltip.setAnchorX(point.getX());
        tooltip.setAnchorY(point.getY());
        if(!tooltip.isShowing()) {
            tooltip.show(getSkinnable().getScene().getWindow());
        }
    }

    private boolean isOnBar(double x, double y) {
        double dx = x - getSkinnable().getWidth()/2;
        double dy = y - getSkinnable().getHeight()/2;
        double rad = Math.sqrt(dx*dx+dy*dy);
        return rad >= barRadius-barWidth/2 && rad <= barRadius+barWidth/2;
    }


    private void updateBarStyle() {
        Background bg = styledBarBox.getBackground();
        if(bg != null) {
            BackgroundFill fill = bg.getFills().get(bg.getFills().size()-1);
            bar.setStroke(fill.getFill());
            oldBar.setStroke(fill.getFill());
        }
    }

    private void updateTextfieldVisibility() {
        valueText.setVisible(getSkinnable().isTextfieldVisible());
    }
    
    private void updateTickVisibility() {
        boolean mjv = getSkinnable().isShowTickMarks();
        boolean mnv = getSkinnable().isMinorTickVisible();
        for(TimeTick tick : ticks) {
            tick.getNode().setVisible(mjv && (tick.isMajor() ? true : mnv));
        }
    }

    private void updateTicks() {
        if(ticks == null) {
            ticks = new ArrayList<>();
            rebuildTicks();
        }
    }

    private void rebuildTicks() {
    	// Fade old
    	if(currentTickOpacy != null) {
    		List<TimeTick> oldTicks = new ArrayList<>(ticks);
    		Timeline fadeOut = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(currentTickOpacy, 1)),
                    new KeyFrame(tickAnimationLength, new KeyValue(currentTickOpacy, 0)));
    		fadeOut.play();
    		fadeOut.setOnFinished(e -> {
    			tickGroup.getChildren().removeAll(oldTicks);
    		});
    	}

        ticks.clear();

        double min = getSkinnable().getMin();
        double max = getSkinnable().getMax();
        double majorUnit = getSkinnable().getMajorTickUnit();
        double minorUnit = getSkinnable().getMajorTickUnit() / getSkinnable().getMinorTickCount();

        currentTickOpacy = new SimpleDoubleProperty();

        for(double pos = Math.ceil(min / minorUnit) * minorUnit; pos < max; pos += minorUnit) {
            boolean major = isInt(pos/majorUnit, 1e-6);
            double preTransformLength = (major ? getSkinnable().getTickLength() : getSkinnable().getMinorTickLength());
            TimeTick tick = new TimeTick(pos,
                    major,
                    preTransformLength * preTransformLength * tickScale,
                    major ? "axis-tick-mark" : "axis-minor-tick-mark");
            tick.fitBounds(barWidth/barRadius);
            ticks.add(tick);
            tick.updatePosition(min, max);
            tick.getNode().opacityProperty().bind(currentTickOpacy);
            tickGroup.getChildren().add(tick.getNode());
        }

        updateTickVisibility();

        Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(currentTickOpacy, 0)),
                new KeyFrame(tickAnimationLength, new KeyValue(currentTickOpacy, 1)));
        fadeIn.play();

        foregroundMask.toFront();
    }

    private void rebuildBar() {
        bar.setStrokeWidth(barWidth / barRadius);
        oldBar.setStrokeWidth(barWidth / barRadius);

        double value = getSkinnable().getValue();
        double oldAngle = filledAngle;
        filledAngle = 2*Math.PI * (value - getSkinnable().getMin()) / (getSkinnable().getMax()-getSkinnable().getMin());

        buildBar(bar, filledAngle);

        if(oldAngle > 0 && filledAngle == 0) {
        	buildBar(oldBar, oldAngle);
            oldBar.setOpacity(1);
            if(oldBarFade != null && oldBarFade.getStatus() == Animation.Status.RUNNING) {
                oldBarFade.stop();
            }
            oldBarFade = new FadeTransition(barFadeToZeroDuration, oldBar);
            oldBarFade.setToValue(0);
            oldBarFade.play();
        }

        // Thumb
        thumb.resize(barWidth, barWidth);
        
    }

    private static void buildBar(Path bar, double filledAngle) {
    	bar.getElements().clear();
        bar.getElements().add(new MoveTo(0, -1));
        if(filledAngle <= Math.PI) {
            bar.getElements().add(new ArcTo(1, 1, 0, Math.sin(filledAngle), - Math.cos(filledAngle), filledAngle > Math.PI, true));
        } else if(filledAngle <= 2*Math.PI){
            bar.getElements().add(new ArcTo(1, 1, 0, 0, 1, true, true));
            bar.getElements().add(new ArcTo(1, 1, 0, Math.sin(filledAngle), - Math.cos(filledAngle), false, true));
        }
    }

    private static boolean isInt(double d, double tolerance) {
        return Math.abs(d%1) < tolerance;
    }


    /**
     * Called when the size of the control changes.
     * @param contentWidth
     * @param contentHeight
     * @param radius
     * @param arcWidth
     */
    private void recalculateShapes(double contentWidth, double contentHeight) {
        if(foregroundMask != null) {
            getChildren().remove(foregroundMask);
        }

        Rectangle fill = new Rectangle(contentWidth*2, contentHeight*2);
        fill.setX(-contentWidth);
        fill.setY(-contentHeight);

        foregroundMask = Shape.subtract(fill, arcMask());
        foregroundMask.setMouseTransparent(true);
        foregroundMask.setFill(Color.BLACK); // is clipped away
        foregroundMask.setEffect(shadow);
        foregroundMask.setClip(arcMask()); // do not draw shadow outside
        getChildren().add(foregroundMask);
        

        // Bar
        rebuildBar();

        // Ticks
        updateTicks();

        // Thumb
        if(cssThumb.getBackground() != null) {
        	BackgroundFill[] fills = cssThumb.getBackground().getFills().stream().map(f -> {
        		return new BackgroundFill(f.getFill(), new CornerRadii(barWidth), f.getInsets());
        	}).toArray(s -> new BackgroundFill[s]);

        	thumb.setBackground(new Background(fills));
        }
        
        
        
    }

    private Shape arcMask() {
        return Shape.subtract(new Circle(barRadius+barWidth/2), new Circle(barRadius-barWidth/2));
    }

    @Override
    protected double computeMinWidth(double height,
            double topInset, double rightInset,
            double bottomInset, double leftInset) {
        return height;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset,
            double leftInset) {
        return width;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset,
            double leftInset) {
        return Math.max(width, getSkinnable().getPrefHeight());
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset,
            double leftInset) {
        return Math.max(height, getSkinnable().getPrefWidth());
    }

    @Override
    protected void layoutChildren(double contentX, double contentY,
            double contentWidth, double contentHeight) {
        barRadius = Math.max(0, Math.min(contentWidth, contentHeight) * getSkinnable().getDiameter() / 2);
        barWidth = Math.min(contentWidth, contentHeight) * getSkinnable().getThickness();
        centralClip.setRadius(1+barWidth/2/barRadius);

        recalculateShapes(contentWidth, contentHeight);
        layoutInArea(foregroundMask, contentX, contentY, contentWidth, contentHeight, 0, HPos.CENTER, VPos.CENTER);
        centralGroup.setTranslateX(contentX+contentWidth/2);
        centralGroup.setTranslateY(contentY+contentHeight/2);
        centralScale.setX(barRadius);
        centralScale.setY(barRadius);
        
        //tvalor.setTranslateX( (contentWidth/2) - (tvalor.getWidth()/2) );
        //tvalor.setTranslateY( (contentHeight/2) - (tvalor.getHeight()/2) );
        
    }
}
