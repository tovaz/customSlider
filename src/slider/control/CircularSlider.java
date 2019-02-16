package slider.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.SizeConverter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.Slider;

public class CircularSlider extends Slider {
	/* properties from NumberAxis (Slider):
	 *
	 * -fx-tick-length
	 * (-fx-show-tick-marks)
	 *
	 * -fx-minor-tick-length
	 * (fx-minor-tick-count)
	 * -fx-minor-tick-visible
	 *
	 * (-fx-major-tick-unit) (default 60 seconds)
	 *
	 * contains ticks of types axis-major-tick-mark, axis-minor-tick-mark of type path
	 */

    // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------

	/** The length of tick mark lines */
    private DoubleProperty tickLength = new StyleableDoubleProperty(8) {
        @Override protected void invalidated() {
            if (tickLength.get() < 0 && !tickLength.isBound()) {
                tickLength.set(0);
            }
        }

        @Override
        public CssMetaData<CircularSlider,Number> getCssMetaData() {
            return StyleableProperties.TICK_LENGTH;
        }
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "tickLength";
        }
    };
    public final double getTickLength() { return tickLength.get(); }
	public final void setTickLength(double value) { tickLength.set(value); }
    public final DoubleProperty tickLengthProperty() { return tickLength; }


    /** The length of minor tick mark lines. Set to 0 to not display minor tick marks. */
    private DoubleProperty minorTickLength = new StyleableDoubleProperty(5) {
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "minorTickLength";
        }

        @Override
        public CssMetaData<CircularSlider,Number> getCssMetaData() {
            return StyleableProperties.MINOR_TICK_LENGTH;
        }
    };
    public final double getMinorTickLength() { return minorTickLength.get(); }
    public final void setMinorTickLength(double value) { minorTickLength.set(value); }
    public final DoubleProperty minorTickLengthProperty() { return minorTickLength; }


    /** true if minor tick marks should be displayed */
    private BooleanProperty minorTickVisible = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "minorTickVisible";
        }

        @Override
        public CssMetaData<CircularSlider,Boolean> getCssMetaData() {
            return StyleableProperties.MINOR_TICK_VISIBLE;
        }
    };
    
    //************************************************** START TEXTFIELD PROPERTIES
    public final boolean isTextfieldVisible() { return TextfieldVisible.get(); }
    public final void setTextfieldVisible(boolean value) { TextfieldVisible.set(value); }
    public final BooleanProperty TextfieldVisibleProperty() { return TextfieldVisible; }
    /** true show value on textfield */
    private BooleanProperty TextfieldVisible = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "Show Textfield";
        }

        @Override
        public CssMetaData<CircularSlider,Boolean> getCssMetaData() {
            return StyleableProperties.SHOW_TEXTFIELD;
        }
    };
    
    
    
    //************************************************* END TEXTFIELD PROPERTIES
    public final boolean isMinorTickVisible() { return minorTickVisible.get(); }
    public final void setMinorTickVisible(boolean value) { minorTickVisible.set(value); }
    public final BooleanProperty minorTickVisibleProperty() { return minorTickVisible; }

    /** true if tick marks should be displayed */
    private BooleanProperty tickMarkVisible = new StyleableBooleanProperty(true) {
        @Override
        public CssMetaData<CircularSlider,Boolean> getCssMetaData() {
            return StyleableProperties.TICK_MARK_VISIBLE;
        }
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "tickMarkVisible";
        }
    };
    
    public final boolean isTickMarkVisible() { return tickMarkVisible.get(); }
    public final void setTickMarkVisible(boolean value) { tickMarkVisible.set(value); }
    public final BooleanProperty tickMarkVisibleProperty() { return tickMarkVisible; }


    /** The circle size in the range 0.0 to 1.0 */
    private DoubleProperty diameter = new StyleableDoubleProperty(0.8) {
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "diameter";
        }

        @Override
        public CssMetaData<CircularSlider,Number> getCssMetaData() {
            return StyleableProperties.DIAMETER;
        }
    };
    public final double getDiameter() { return diameter.get(); }
    public final void setDiameter(double value) { diameter.set(value); }
    public final DoubleProperty diameterProperty() { return diameter; }

    /** The circle size in the range 0.0 to 1.0 */
    private DoubleProperty thickness = new StyleableDoubleProperty(0.08) {
        @Override
        public Object getBean() {
            return CircularSlider.this;
        }

        @Override
        public String getName() {
            return "thickness";
        }

        @Override
        public CssMetaData<CircularSlider,Number> getCssMetaData() {
            return StyleableProperties.THICKNESS;
        }
    };
    public final double getThickness() { return thickness.get(); }
    public final void setThickness(double value) { thickness.set(value); }
    public final DoubleProperty thicknessProperty() { return thickness; }



    // -------------- CONSTRUCTORS -------------------------------------------------------------------------------------

    public CircularSlider() {
        getStyleClass().setAll("progress-bar", "slider", "axis", "circular-slider");
        showTickMarksProperty().bindBidirectional(tickMarkVisible);
	}


    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
    	return getClassCssMetaData();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
    	return new CircularSliderSkin(this);
    }



    private static class StyleableProperties {
    	private static final CssMetaData<CircularSlider,Number> TICK_LENGTH =
            new CssMetaData<CircularSlider,Number>("-fx-tick-length",
                SizeConverter.getInstance(), 8.0) {

            @Override
            public boolean isSettable(CircularSlider n) {
                return n.tickLength == null || !n.tickLength.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.tickLengthProperty();
            }
        };

        private static final CssMetaData<CircularSlider,Number> TICK_UNIT =
            new CssMetaData<CircularSlider,Number>("-fx-tick-unit",
                SizeConverter.getInstance(), 5.0) {

            @Override
            public boolean isSettable(CircularSlider n) {
            	return !n.majorTickUnitProperty().isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.majorTickUnitProperty();
            }
        };

        private  static final CssMetaData<CircularSlider,Number> MINOR_TICK_LENGTH =
            new CssMetaData<CircularSlider,Number>("-fx-minor-tick-length",
                SizeConverter.getInstance(), 5.0) {

            @Override
            public boolean isSettable(CircularSlider n) {
                return n.minorTickLength == null || !n.minorTickLength.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.minorTickLengthProperty();
            }
        };

        private static final CssMetaData<CircularSlider,Number> MINOR_TICK_COUNT =
            new CssMetaData<CircularSlider,Number>("-fx-minor-tick-count",
                SizeConverter.getInstance(), 5) {

            @Override
            public boolean isSettable(CircularSlider n) {
            	return !n.minorTickCountProperty().isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.minorTickCountProperty();
            }
        };

         private static final CssMetaData<CircularSlider,Boolean> MINOR_TICK_VISIBLE =
            new CssMetaData<CircularSlider,Boolean>("-fx-minor-tick-visible",
                 BooleanConverter.getInstance(), Boolean.TRUE) {

            @Override
            public boolean isSettable(CircularSlider n) {
                return n.minorTickVisible == null || !n.minorTickVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Boolean>)(WritableValue<Boolean>)n.minorTickVisibleProperty();
            }
        };
         
        
        private static final CssMetaData<CircularSlider,Boolean> SHOW_TEXTFIELD =
            new CssMetaData<CircularSlider,Boolean>("-fx-minor-tick-visible",
                 BooleanConverter.getInstance(), Boolean.TRUE) {

            @Override
            public boolean isSettable(CircularSlider n) {
                return n.TextfieldVisible == null || !n.TextfieldVisible.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Boolean>)(WritableValue<Boolean>)n.minorTickVisibleProperty();
            }
        }; 

        private static final CssMetaData<CircularSlider,Boolean> TICK_MARK_VISIBLE =
            new CssMetaData<CircularSlider,Boolean>("-fx-tick-mark-visible",
                BooleanConverter.getInstance(), Boolean.TRUE) {

            @Override
            public boolean isSettable(CircularSlider n) {
            	return !n.showTickMarksProperty().isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Boolean>)(WritableValue<Boolean>)n.showTickMarksProperty();
            }
        };

        private  static final CssMetaData<CircularSlider,Number> DIAMETER =
            new CssMetaData<CircularSlider,Number>("-fx-diameter",
                SizeConverter.getInstance(), 0.8) {

            @Override
            public boolean isSettable(CircularSlider n) {
                return n.diameter == null || !n.diameter.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.diameterProperty();
            }
        };

        private  static final CssMetaData<CircularSlider,Number> THICKNESS =
            new CssMetaData<CircularSlider,Number>("-fx-thickness",
                SizeConverter.getInstance(), 0.08) {

            @Override
            public boolean isSettable(CircularSlider n) {
                return n.thickness == null || !n.thickness.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(CircularSlider n) {
                return (StyleableProperty<Number>)(WritableValue<Number>)n.thicknessProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
         static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(Slider.getClassCssMetaData());
            styleables.add(TICK_MARK_VISIBLE);
            styleables.add(MINOR_TICK_COUNT);
            styleables.add(MINOR_TICK_LENGTH);
            styleables.add(MINOR_TICK_COUNT);
            styleables.add(MINOR_TICK_VISIBLE);
            styleables.add(TICK_UNIT);
            styleables.add(TICK_LENGTH);
            styleables.add(DIAMETER);
            styleables.add(THICKNESS);
            STYLEABLES = Collections.unmodifiableList(styleables);
         }
	}

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}



