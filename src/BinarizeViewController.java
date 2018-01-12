
// IP Ue1 WS2017/18 Vorgabe
//
// Copyright (C) 2017 by Klaus Jung
// All rights reserved.
// Date: 2017-08-18

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;

public class BinarizeViewController {

	public enum MethodeType {
		COPY("Copy Image"), THRESHOLD("Threshold"), ISODATA("ISO Data"), FLOODFILLING("Flood Filling"), BORDER(
				"Border"), VECTOR("Vector");

		private final String name;

		MethodeType(String s) {
			name = s;
		}

		public String toString() {
			return this.name;
		}
	}

	public enum FillingType {
		DEPTH("Depth First"), BREADTH("Breadth First"), SEQUENTIAL("Sequential");

		private final String name;

		FillingType(String s) {
			name = s;
		}

		public String toString() {
			return this.name;
		}
	}

	private static final String initialFileName = "head-orig3.png";
	private static File fileOpenPath = new File(".");

	private int threshold;

	private double zoom;
	private List<Path> paths;
	private List<List<int[]>> vectorPaths;

	private int origImgHeight = 0;
	private int origImgWidth = 0;

	private String filenname = initialFileName;

	private double aMinValue;
	private double aMaxValue;
	private double factorValue;

	@FXML
	private CheckBox gridCheckbox;

	@FXML
	private CheckBox borderCheckbox;

	@FXML
	private Canvas overlayCanvas;

	@FXML
	private Canvas pathCanvas;

	@FXML
	private Canvas vectorCanvas;

	@FXML
	private ImageView originalImageView;

	@FXML
	private ImageView binarizedImageView;

	@FXML
	private ComboBox<MethodeType> methodeSelection;

	@FXML
	private ComboBox<FillingType> fillingSelection;

	@FXML
	private CheckBox outline;

	@FXML
	private Slider zoomSlider;

	@FXML
	private Slider thresholdSlider;

	@FXML
	private Label thresholdLabel;

	@FXML
	private Label messageLabel;

	// Bezier Slider

	@FXML
	private Slider aMinSlider;


	@FXML
	private Slider aMaxSlider;

	@FXML
	private Slider factor;


	@FXML
	private Label aMinLabel;

	@FXML
	private Label aMaxLabel;

	@FXML
	private Label factorLabel;

	@FXML
	private CheckBox fillCurves;

	@FXML
	private CheckBox showImage;

	@FXML
	private Button reset;

	@FXML
	public void initialize() {

		// set combo boxes items
		methodeSelection.getItems().addAll(MethodeType.values());
		methodeSelection.setValue(MethodeType.COPY);

		fillingSelection.getItems().addAll(FillingType.values());
		fillingSelection.setValue(FillingType.DEPTH);

		// initialize parameters
		methodeChanged();

		// init paths empty
		this.paths = new LinkedList<>();
		this.vectorPaths = new LinkedList<>();

		// load and process default image
		new RasterImage(new File(initialFileName)).setToView(originalImageView);
		processImage();

		// initialize thresholdslider
		threshold = 0;

		thresholdSlider.valueProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				thresholdLabel.textProperty().setValue(String.valueOf( thresholdSlider.getValue()));
				threshold = (int) thresholdSlider.getValue();
				processImage();
			}
		});

		// initialize zoomslider
		zoom = 0;

		zoomSlider.valueProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				zoom = zoomSlider.getValue();
				zoomChanged();
			}
		});

		gridCheckbox.selectedProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {

				System.out.println(gridCheckbox.selectedProperty().get());

				if (gridCheckbox.selectedProperty().get())
					zoomChanged();
				else {
					GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
					gc.clearRect(0, 0, 0, 0);
				}
			}

		});

		aMinSlider.setValue(0.55);
		aMaxLabel.setText("0.55");

		aMaxSlider.setValue(1.0);
		aMaxLabel.setText("1.0");

		factor.setValue(4/3);
		factorLabel.setText("1.33");

		aMinSlider.valueProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				aMinLabel.textProperty().setValue(String.valueOf( aMinSlider.getValue()));
				aMinValue = aMinSlider.getValue();
				drawVectorPaths();
			}
		});

		aMaxSlider.valueProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				aMaxLabel.textProperty().setValue(String.valueOf( aMaxSlider.getValue()));
				aMaxValue = aMaxSlider.getValue();
				drawVectorPaths();
			}
		});

		factor.valueProperty().addListener(new ChangeListener() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				factorLabel.textProperty().setValue(String.valueOf( factor.getValue()));
				factorValue = factor.getValue();
				drawVectorPaths();
			}
		});

		fillCurves.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				drawVectorPaths();
			}
		});

		showImage.setSelected(true);
		showImage.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				zoomChanged();
			}
		});

		reset.cancelButtonProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

				showImage.setSelected(true);

				aMinSlider.setValue(0.55);
				aMinSlider.adjustValue(0.55);
				aMaxLabel.setText("0.55");

				aMaxSlider.setValue(1.0);
				aMinSlider.adjustValue(1.0);
				aMaxLabel.setText("1.0");

				factor.setValue(4/3);
				factorLabel.setText("1.33");
			}
		});
	}

	@FXML
	void openImage() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(fileOpenPath);
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Images (*.jpg, *.png, *.gif)", "*.jpeg", "*.jpg", "*.png", "*.gif"));
		File selectedFile = fileChooser.showOpenDialog(null);
		if (selectedFile != null) {
			filenname = selectedFile.toString();
			System.out.println("filename: " + filenname);
			fileOpenPath = selectedFile.getParentFile();
			new RasterImage(selectedFile).setToView(originalImageView);
			processImage();
			messageLabel.getScene().getWindow().sizeToScene();

			origImgHeight = (int) originalImageView.getImage().getHeight();
			origImgWidth = (int) originalImageView.getImage().getWidth();
		}
	}

	@FXML
	void methodeChanged() {
		outline.setDisable(methodeSelection.getValue() == MethodeType.COPY
				|| methodeSelection.getValue() == MethodeType.FLOODFILLING);
		fillingSelection.setDisable(!(methodeSelection.getValue() == MethodeType.FLOODFILLING));
		thresholdSlider.setDisable(methodeSelection.getValue() == MethodeType.COPY
				|| methodeSelection.getValue() == MethodeType.FLOODFILLING);

		paths = new LinkedList<>();
		vectorPaths = new LinkedList<>();

		processImage();
	}

	@FXML
	void outlineChanged() {
		processImage();
	}

	private void processImage() {

		if (originalImageView.getImage() == null)
			return; // no image: nothing to do

		long startTime = System.currentTimeMillis();

		RasterImage origImg = new RasterImage(originalImageView);
		RasterImage binImg = new RasterImage(origImg); // create a clone of
		// origImg

		switch (methodeSelection.getValue()) {
			case THRESHOLD:
				binImg.binarizeWithThreshold(threshold);
				break;
			case ISODATA:
				threshold = binImg.binarizeWithIsoData();
				thresholdSlider.setValue(threshold);
				thresholdLabel.setText("" + threshold);
				binImg.binarizeWithThreshold(threshold);
				break;
			case FLOODFILLING:

				// Filter.methodSelection(fillingSelection.getValue(), binImg);
				// binImg.setToView(binarizedImageView);

				switch (fillingSelection.getValue()) {
					case DEPTH:
						Filter.depthFirst(binImg);
						binImg.setToView(binarizedImageView);
						break;
					case BREADTH:
						Filter.breadthFirst(binImg);
						binImg.setToView(binarizedImageView);
						break;
					case SEQUENTIAL:
						Filter.sequential(binImg);
						binImg.setToView(binarizedImageView);
						break;
					default:
						break;
				}
				break;
			case BORDER:
				paths = Filter.potrace(binImg);
				drawPath();
				// drawPath(Canvas pathCanvas, int zoomedWidth, int zoomedHeight,
				// Set<Path> paths, int imgWidth)
				// binImg.setToView(binarizedImageView);
				break;
			case VECTOR:
				if (paths.isEmpty()) {
					System.out.print("paths empty");
					paths = Filter.potrace(binImg);
					vectorPaths = Filter.vectorisation(paths, origImgWidth);
				} else {
					vectorPaths = Filter.vectorisation(paths, origImgWidth);
				}
				drawVectorPaths();
				break;
			default:
				break;

		}

		if (outline.isSelected() && methodeSelection.getValue() != MethodeType.COPY) {
			RasterImage outlineImg = new RasterImage(binImg.width, binImg.height);
			Filter.outline(binImg, outlineImg);
			outlineImg.setToView(binarizedImageView);
		} else {
			binImg.setToView(binarizedImageView);
		}

		messageLabel.setText(
				"Processing time: " + (System.currentTimeMillis() - startTime) + " ms, threshold = " + threshold);
	}

	private void zoomChanged() {

		if (origImgHeight == 0 || origImgWidth == 0) {
			origImgHeight = (int) binarizedImageView.getImage().getHeight();
			origImgWidth = (int) binarizedImageView.getImage().getWidth();
		}

		// System.out.println("zoom: "+zoom+" imgwidth:
		// "+binarizedImageView.getImage().getWidth()+ " imgheight:
		// "+binarizedImageView.getImage().getHeight());

		double zoomedWidth = Math.ceil(zoom * origImgWidth);
		double zoomedHeight = Math.ceil(zoom * origImgHeight);

		Image img = new Image(new File(filenname).toURI().toString(), zoomedWidth, zoomedHeight, false, false);

		if(showImage.isSelected())
			binarizedImageView.setImage(img);

		// drawOverlay(zoomedWidth, zoomedHeight);
		drawPath();
		drawVectorPaths();
	}

	private void drawOverlay(double zoomedWidth, double zoomedHeight) {
		overlayCanvas.setWidth(zoomedWidth);
		overlayCanvas.setHeight(zoomedHeight);

		// overlay example: draw a grit
		GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, zoomedWidth, zoomedHeight);
		gc.setStroke(Color.RED);
		gc.setLineWidth(1);

		double gritSpacingWidth = binarizedImageView.getImage().getWidth() / origImgWidth;
		double gritSpacingHeight = binarizedImageView.getImage().getHeight() / origImgHeight;

		for (double y = 0; y <= zoomedHeight; y += gritSpacingHeight) {
			gc.strokeLine(0, y, zoomedWidth, y);
			// System.out.println("y: "+y+" zoomedHeight: "+zoomedHeight+ "
			// gritspacing: "+gritSpacingHeight);

		}
		for (double x = 0; x <= zoomedWidth; x += gritSpacingWidth) {
			gc.strokeLine(x, 0, x, zoomedHeight);
			// System.out.println("iter: "+x+" zoomedWidth: "+zoomedWidth+ "
			// gritspacing: "+gritSpacingWidth);

		}
	}

	private void drawPath() {

		if (paths.isEmpty())
			return; // no paths: nothing to do

		if (origImgHeight == 0 || origImgWidth == 0) {
			origImgHeight = (int) binarizedImageView.getImage().getHeight();
			origImgWidth = (int) binarizedImageView.getImage().getWidth();
		}

		double zoomedWidth = Math.ceil(zoom * origImgWidth);
		double zoomedHeight = Math.ceil(zoom * origImgHeight);

		pathCanvas.setWidth(zoomedWidth);
		pathCanvas.setHeight(zoomedHeight);

		int gritPixelDistance = 16;
		GraphicsContext gc = pathCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, zoomedWidth, zoomedHeight);
		gc.setLineWidth(4);

		double gritSpacingWidth = binarizedImageView.getImage().getWidth() / origImgWidth;
		double gritSpacingHeight = binarizedImageView.getImage().getHeight() / origImgHeight;

		// System.out.println("zoom = ?: "+zoom+" . "+gritSpacingHeight);
		// System.out.println(paths.toString());

		for (Path path : paths) {
			// empty element to signal the end of a path --> for vectorisation
			if (path.getID() == "00")
				continue;

			Color color = (path.isOuterBorder()) ? Color.GREENYELLOW : Color.BLUEVIOLET;
			gc.setStroke(color);

			int black = path.getBlackPixel();
			int white = path.getWhitePixel();

			int bWidth = black % origImgWidth;
			int bHeight = black / origImgWidth;

			double normBWidth = bWidth * gritSpacingWidth;
			double normBHeight = bHeight * gritSpacingHeight;

			// left to right
			if (black == (white + 1)) {
				// System.out.println("black: "+black+" nBlackHeight:
				// "+normBHeight+" gritspac: "+gritSpacingHeight+" bwidth:
				// "+bWidth+" bHeight: "+bHeight);
				gc.strokeLine(normBWidth, normBHeight, normBWidth, normBHeight + gritSpacingHeight);
			}
			// up to down
			else if (black == (white + origImgWidth)) {
				gc.strokeLine(normBWidth, normBHeight, normBWidth + gritSpacingWidth, normBHeight);
			}
			// right to left
			else if (black == (white - 1)) {
				gc.strokeLine(normBWidth + gritSpacingWidth, normBHeight, normBWidth + gritSpacingWidth,
						normBHeight + gritSpacingHeight);
			}
			// down to up
			else if (black == white - origImgWidth) {
				gc.strokeLine(normBWidth, normBHeight + gritSpacingHeight, normBWidth + gritSpacingWidth,
						normBHeight + gritSpacingHeight);
			}

		}
	}

	private void drawVectorPaths() {

		if (vectorPaths.isEmpty())
			return; // no vector paths: nothing to do

		if (origImgHeight == 0 || origImgWidth == 0) {
			origImgHeight = (int) binarizedImageView.getImage().getHeight();
			origImgWidth = (int) binarizedImageView.getImage().getWidth();
		}

		double zoomedWidth = Math.ceil(zoom * origImgWidth);
		double zoomedHeight = Math.ceil(zoom * origImgHeight);

		vectorCanvas.setWidth(zoomedWidth);
		vectorCanvas.setHeight(zoomedHeight);

		GraphicsContext gc = vectorCanvas.getGraphicsContext2D();
		gc.clearRect(0, 0, zoomedWidth, zoomedHeight);

		double gritSpacingWidth = binarizedImageView.getImage().getWidth() / origImgWidth;
		double gritSpacingHeight = binarizedImageView.getImage().getHeight() / origImgHeight;

		double mP05x = 0;
		double mP05y = 0;

		double[] firstMidPoints = new double[2];
		double[] secondMidPoints = new double[2];

		for (List<int[]> vectorPath : vectorPaths) {


			int[] p1 = vectorPath.get(0);
			int[] p2 = p1;

			mP05x = p1[1] * gritSpacingWidth;
			mP05y = p1[0] * gritSpacingHeight;

			// the get the complete path, the connection between last and first point must be drawn
			vectorPath.add(p1);

			gc.beginPath();
			gc.moveTo(mP05x, mP05y);

			for (int[] point : vectorPath) {
				p1 = p2;
				p2 = point;


				double mP1x = p1[1] * gritSpacingWidth;
				double mP1y = p1[0] * gritSpacingHeight;

				double mP2x = p2[1] * gritSpacingWidth;
				double mP2y = p2[0] * gritSpacingHeight;

				// draw Line
				gc.setLineWidth(4);
				gc.setStroke(Color.BLUE);
				// gc.strokeLine(mP1x, mP1y, mP2x, mP2y);

				// draw a point  'Midpoints'
				gc.setLineWidth(8);
				gc.setStroke(Color.GREY);
				gc.strokeLine(mP2x, mP2y, mP2x, mP2y);

				/*
				 * BezierCurves
				 */

				// TODO folie 45 2. - 46 bzw 47
				// Methode defineDistanceToVertex speichert abstand von der
				// gerade zum punkt in distance (Wobei die werte teilweise nicht
				// hinkommen k�nnen siehe sysout in der methode) inwieweit die
				// stimmen wird sich zeigen


				// compute coordinates for beziercurves
				firstMidPoints = defineMidPoints(mP05x, mP05y, mP1x, mP1y);
				secondMidPoints = defineMidPoints(mP1x, mP1y, mP2x, mP2y);

				double distance = defineDistanceToVertex(firstMidPoints, secondMidPoints, mP1x, mP1y);

				double alpha = factorValue * (distance - 0.5) / distance;

				if(alpha < aMinValue)
				{
					alpha = aMinValue;
				}
				else if(alpha > aMaxValue)
				{
					// Curve
					gc.bezierCurveTo(firstMidPoints[0], firstMidPoints[1], mP1x, mP1y, secondMidPoints[0], secondMidPoints[1]);
					gc.setLineWidth(4);
					gc.setStroke(Color.GREEN);
					gc.stroke();
				}
				else
				{
					// Curve
					gc.bezierCurveTo(firstMidPoints[0]+alpha, firstMidPoints[1]+alpha, mP1x, mP1y, secondMidPoints[0], secondMidPoints[1]);
					gc.setLineWidth(4);
					gc.setStroke(Color.GREEN);
					gc.stroke();
				}

				// Curve Points
				gc.setLineWidth(8);
				gc.setStroke(Color.CHARTREUSE);
				gc.strokeLine(firstMidPoints[0], firstMidPoints[1], firstMidPoints[0], firstMidPoints[1]);

				mP05x = mP1x;
				mP05y = mP1y;
			}

			gc.closePath();

			if(fillCurves.isSelected())
			{
				Color fillColor = (p1[2] == 1) ? Color.BLACK : Color.WHITE;
				gc.setFill(Paint.valueOf(fillColor.toString()));
				gc.fill();
			}

		}
	}

	public double[] defineMidPoints(double mP1x, double mP1y, double mP2x, double mP2y) {

		double[] mP = new double[2];

		mP[0] = (mP1x + mP2x) / 2;
		mP[1] = (mP1y + mP2y) / 2;

		return mP;

	}

	public double defineDistanceToVertex(double[] firstMidPoints, double[] secondMidPoints, double mP1x, double mP1y) {

		double distance;
		double[] distanceVector = new double[2];

		distanceVector[0] = secondMidPoints[1] - firstMidPoints[1];
		distanceVector[1] = -(secondMidPoints[0] - firstMidPoints[0]);

		double unitVector = Math.sqrt(Math.pow(distanceVector[0], 2) + Math.pow(distanceVector[1], 2));

		distanceVector[0] = distanceVector[0] / unitVector;
		distanceVector[1] = distanceVector[1] / unitVector;

		distance = ((distanceVector[0] * (mP1x - firstMidPoints[0]))
				+ (distanceVector[1] * (mP1y - firstMidPoints[1])));

		System.out.println(distance);

		return distance;
	}
}
