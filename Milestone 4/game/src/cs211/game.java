package cs211;

import processing.core.*;
import processing.event.*;
import processing.video.Capture;
import processing.video.Movie;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class game extends PApplet {

	final float boxLength = 350, boxHeight = 20, radius = 10,
			gravityConstant = 9.81f;
	float speed = 1;
	int k = 0;
	final float MAX_SPEED = 1.5f, MIN_SPEED = 0.2f; // goal
	int score = 0, lastScore = score, frame = 0;

	Mover ball = new Mover();
	boolean SHIFT_PRESSED = false;
	PVector gravityForce = new PVector(0, gravityConstant, 0);
	static float rotX, rotY, rotZ;

	// Obstacles
	float cylinderBaseSize = 20;
	float cylinderHeight = 50;
	int cylinderResolution = 50;
	float X = 0, Y = 0;
	PShape Cylinder = new PShape();
	PShape base = new PShape();
	ArrayList<PVector> array = new ArrayList<PVector>();
	ArrayList<PVector> ballLocation = new ArrayList<PVector>();

	// Tree
	PShape tree;

	// dataVisualizationSurfaceraphics top view scoreboard
	PGraphics pg;
	PGraphics topView;
	PGraphics scoreboard;
	PGraphics barChart;
	int border = 5;
	float topViewLength;
	float scorebordWidth;
	float barChartHeight;

	// barChart
	ArrayList<Integer> data = new ArrayList<Integer>();

	// HScrollbar
	HScrollbar hs;
	float hsX;
	float hsY;
	float hsW;
	float hsH = 20;

	// Image Processing
	PVector angle;
	Boolean camera = false;
	Boolean movieAvailable = true;
	Boolean scrollBar = false;

	// image, camera, result
	static PImage img; // image
	Capture cam; // camera
	PImage result;
	PImage img2;
	Movie movie;

	// ScrollBar 1 and 2
	HScrollbar thresholdBar1;
	HScrollbar thresholdBar2;

	// parametres
	int minVotes = 200;
	int nLines = 6;
	double treshold = 25;
	int hueMax = 138;
	int hueMin = 100;
	int brightMax = 245;
	int brightMin = 10;
	int satuMin = 100;
	int satuMax = 255;
	float sobelVal = 300;
	int intensityMax = 255;
	int intensityMin = 100;

	// Kernel
	float[][] kernel1 = { { 0, 0, 0 }, { 0, 2, 0 }, { 0, 0, 0 } };
	float[][] kernel2 = { { 0, 1, 0 }, { 1, 0, 1 }, { 0, 1, 0 } };
	float[][] gaussianKernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

	static List<PVector> linesList = new ArrayList<>();
	QuadGraph qGraph = new QuadGraph();
	TwoDThreeD transform = new TwoDThreeD(width, height);

	public void setup() {
		size(1000, 800, P3D);

		topViewLength = height / 6 - 2 * border;
		scorebordWidth = height / 8 - 2 * border;
		barChartHeight = height / 9 - 2;
		hsX = 3 * border + topViewLength + scorebordWidth;
		hsY = height - border - 22;
		hsW = width - height * 7 / 24 - 4 * border;
		pg = createGraphics(width, height / 6);
		topView = createGraphics(height / 6 - 2 * border, height / 6 - 2
				* border);
		scoreboard = createGraphics(height / 8 - 2 * border, height / 6 - 2
				* border);
		barChart = createGraphics(width - height * 7 / 24 - 4 * border,
				height / 9);
		stroke(0);
		fill(239, 195, 52);
		float angle;
		float[] x = new float[cylinderResolution + 1];
		float[] y = new float[cylinderResolution + 1];

		// get the x and y position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = (TWO_PI / cylinderResolution) * i;
			x[i] = sin(angle) * cylinderBaseSize;
			y[i] = cos(angle) * cylinderBaseSize;
		}
		Cylinder = createShape();
		Cylinder.beginShape(QUAD_STRIP);

		// draw the border of the cylinder
		for (int i = 0; i < x.length; i++) {
			Cylinder.vertex(x[i], y[i], 0);
			Cylinder.vertex(x[i], y[i], cylinderHeight);
		}
		Cylinder.endShape();

		// base
		base = createShape();
		base.beginShape(TRIANGLE_FAN);
		base.vertex(0, 0, cylinderHeight);
		for (int i = 0; i < x.length; i++) {
			base.vertex(x[i], y[i], cylinderHeight);
		}
		base.endShape();
		hs = new HScrollbar(hsX, hsY, hsW, hsH);

		// Tree
		tree = loadShape("simpleTree.obj");
		tree.scale(40);

		// ImageProcessing
		result = createImage(width, height, ALPHA);
		img = createImage(width, height, ALPHA);

		// Camera or Movie or Image
		if (!movieAvailable) {
			if (camera) {
				String[] cameras = Capture.list();
				if (cameras.length == 0) {
					println("There are no cameras available for capture.");
					exit();
				} else {
					println("Available cameras:");
					for (int i = 0; i < cameras.length; i++) {
						println(cameras[i]);
					}
					cam = new Capture(this, cameras[0]);
					cam.start();
				}
			}
		} else {
			movie = new Movie(this, "testvideo.mp4"); // Put the video in the
														// same directory
			movie.loop();
		}
	}

	public void draw() {
		PImage background = loadImage("gameBackground.jpg");
		background.resize(width, height);
		background(background);
		// background(204, 207, 253);
		directionalLight(50, 100, 125, 0, -1, 0);
		ambientLight(102, 102, 102);
		lights();
		pushMatrix();
		translate(width / 2, height / 2, 0);
		if (!SHIFT_PRESSED) {
			// start of Image Processing
			if ((frame % 15) == 5) {
				if (!movieAvailable) {
					if (camera) {
						if (cam.available() == true) {
							cam.read();
						}
						img = cam.get();
					} else {
						img = loadImage("board2.jpg");
					}
				} else {
					if (movie.available()) {
						movie.read();
					}
					img = movie.get();
				}
				result = sobel(
						intesityFilter((gaussianBlur(filterImage(img))),
								intensityMax, intensityMin), sobelVal);

				linesList = hough(result, nLines);

				qGraph.build(linesList, width, height);
				List<int[]> quads = new ArrayList<int[]>();
				quads = qGraph.findCycles();

				if (!quads.isEmpty()) {
					List<int[]> bestQuads = new ArrayList<int[]>();
					for (int[] quad : quads) {
						PVector l1 = linesList.get(quad[0]);
						PVector l2 = linesList.get(quad[1]);
						PVector l3 = linesList.get(quad[2]);
						PVector l4 = linesList.get(quad[3]);

						PVector c12 = intersection(l1, l2);
						PVector c23 = intersection(l2, l3);
						PVector c34 = intersection(l3, l4);
						PVector c41 = intersection(l4, l1);

						if (QuadGraph.isConvex(c12, c23, c34, c41)
								&& QuadGraph.validArea(c12, c23, c34, c41, 2
										* result.width * result.height, 0)
								&& QuadGraph.nonFlatQuad(c12, c23, c34, c41)) {
							bestQuads.add(quad);
						}

					}

					if (bestQuads.size() > 0) {
						// We take the quad with the best angle
						float minCos = 1.0f;
						float tempCos = 0.0f;
						int minIndex = 0;
						for (int i = 0; i < bestQuads.size(); i++) {
							// tempCos = QuadGraph.minCos(c12, c23, c34, c41);
							if (tempCos < minCos) {
								minCos = tempCos;
								minIndex = i;
							}
						}

						int[] bestQuad = bestQuads.get(minIndex);
						fill(color(0));
						PVector l1 = linesList.get(bestQuad[0]);
						PVector l2 = linesList.get(bestQuad[1]);
						PVector l3 = linesList.get(bestQuad[2]);
						PVector l4 = linesList.get(bestQuad[3]);

						PVector c12 = intersection(l1, l2);
						PVector c23 = intersection(l2, l3);
						PVector c34 = intersection(l3, l4);
						PVector c41 = intersection(l4, l1);
						List<PVector> corners = new ArrayList<PVector>();

						corners.add(c12);
						corners.add(c23);
						corners.add(c34);
						corners.add(c41);
						corners = sortCorners(corners);

						angle = transform.get3DRotations(corners);
						rotX = testAngle(angle.y);
						rotY = testAngle(angle.x);
						rotZ = testAngle(angle.z);
						float rx = (angle.x * 180 / PI);
						float ry = (angle.y * 180 / PI);
						float rz = (angle.z * 180 / PI);

						println("rx = " + rx + "; ry = " + ry + "; rz = " + rz);
					}
				}
			}
			// ent of image Processing

			rotateZ(rotZ);
			rotateX(rotY);
			rotateX(rotX);
			fill(33, 255, 17);
			box(boxLength, boxHeight, boxLength);
			for (int i = 0; i < array.size(); i++) {
				drawTree(array.get(i));
			}
			ball.checkedges();
			ball.checkCylinderCollision();
			ball.rolling();
			ball.display();
			frame++;
		} else {
			rotateX(-PI / 2);
			for (int i = 0; i < array.size(); i++) {
				drawTree(array.get(i));
			}
			pushMatrix();
			fill(33, 255, 17);
			box(boxLength, boxHeight, boxLength);
			popMatrix();
			ball.display();
		}
		popMatrix();
		// draw the data visualization surface
		drawpg();
		image(pg, 0, 5 * height / 6 + 1);
		// draw the top view surface
		drawTopView();
		image(topView, border, 5 * height / 6 + border);
		// draw the scoreboard
		drawScoreboard();
		image(scoreboard, 2 * border + topViewLength, 5 * height / 6 + border);

		// draw the barChart
		drawBarChart();
		image(barChart, 3 * border + topViewLength + scorebordWidth, 5 * height
				/ 6 + border);
	}

	public void drawpg() {
		pushMatrix();
		pg.beginDraw();
		pg.background(200, 242, 243);
		pg.noStroke();
		pg.noFill();
		pg.endDraw();
		popMatrix();
	}

	public void drawTopView() {
		topView.beginDraw();
		topView.background(33, 33, 137);
		if (!SHIFT_PRESSED) {
			if (ballLocation.size() <= 200) {
				ballLocation.add(new PVector(toTopView(ball.location.x)
						+ topViewLength / 2, 0, toTopView(ball.location.z)
						+ topViewLength / 2));
			} else {
				if (k > 200)
					k = 0;
				ballLocation.set(k, new PVector(toTopView(ball.location.x)
						+ topViewLength / 2, 0, toTopView(ball.location.z)
						+ topViewLength / 2));
				k++;
			}
		}

		// Trace
		for (int i = 0; i < ballLocation.size(); i++) {
			pushMatrix();
			topView.noStroke();
			topView.fill(49, 49, 127);
			topView.ellipse(ballLocation.get(i).x, ballLocation.get(i).z,
					toTopView(radius), toTopView(radius));
			popMatrix();
		}
		// Draw the ball
		pushMatrix();
		topView.fill(245, 16, 16);
		topView.ellipse(toTopView(ball.location.x) + topViewLength / 2,
				toTopView(ball.location.z) + topViewLength / 2,
				2 * toTopView(radius), 2 * toTopView(radius));
		popMatrix();
		// Draw Cylinders on TopView
		pushMatrix();
		topView.fill(125, 142, 69);
		for (int i = 0; i < array.size(); i++) {
			topView.ellipse(
					toTopView(array.get(i).x - (width - boxLength) / 2),
					toTopView(array.get(i).y - (height - boxLength) / 2),
					2 * toTopView(cylinderBaseSize),
					2 * toTopView(cylinderBaseSize));
			noStroke();
		}
		popMatrix();
		topView.endDraw();
	}

	// ratio box-->topView
	public float toTopView(float x) {
		return x * topViewLength / boxLength;
	}

	public void drawScoreboard() {
		scoreboard.beginDraw();
		pushMatrix();
		scoreboard.background(200, 242, 243);
		popMatrix();
		pushMatrix();
		scoreboard.noFill();
		scoreboard.stroke(0);
		scoreboard.rect(0, 0, scorebordWidth - 2, topViewLength - 2);
		popMatrix();
		// add text
		pushMatrix();
		float format = 10;
		float dis = (topViewLength - 2 - 7 * format) / 2;
		scoreboard.textSize(format);
		scoreboard.fill(0);
		scoreboard.text("Total Score: ", 7, 2 * format);
		scoreboard.text(score, 7, 3 * format);
		scoreboard.text("Velocity: ", 7, 3 * format + dis + 5);
		scoreboard.text(ball.velocity.mag(), 7, 4 * format + dis + 5);
		scoreboard.text("Last Score: ", 7, 5 * format + 2 * dis);
		scoreboard.text(lastScore, 7, 6 * format + 2 * dis);
		popMatrix();
		scoreboard.endDraw();
	}

	public void drawBarChart() {
		barChart.beginDraw();
		pushMatrix();
		barChart.background(108, 89, 11);
		popMatrix();
		// draw data graph
		if (frame % 200 == 0) {
			frame = 0;
			int graphData = score - lastScore;
			lastScore = score;
			data.add(graphData);
		}
		stroke(255);
		pushMatrix();

		// HScrollbar
		hs.update();
		hs.display();
		for (int i = 0; i < data.size(); i++) {
			int l = data.get(i) / 2; // number of point per rect
			for (int j = 0; j < Math.abs(l); j++) {
				if (l < 0) {
					pushMatrix();
					barChart.stroke(255);
					barChart.fill(206, 53, 16);
					barChart.rect(i * 10 * (hs.getPos() + 0.1f), barChartHeight
							- (j + 1) * 10 * (hs.getPos() + 0.1f),
							10 * (hs.getPos() + 0.1f),
							10 * (hs.getPos() + 0.1f));
					popMatrix();
				} else {
					pushMatrix();
					barChart.stroke(255);
					barChart.fill(36, 255, 20);
					barChart.rect(i * 10 * (hs.getPos() + 0.1f), barChartHeight
							- (j + 1) * 10 * (hs.getPos() + 0.1f),
							10 * (hs.getPos() + 0.1f),
							10 * (hs.getPos() + 0.1f));
					popMatrix();
				}
			}
		}
		popMatrix();
		barChart.endDraw();
	}

	float pressedY;
	float currentZ;
	float pressedX;
	float currentX;

	public void mousePressed(MouseEvent e) {
		pressedY = e.getY();
		pressedX = e.getX();
	}

	// public void mouseDragged(MouseEvent e) {
	// if ( !(pressedX>hsX && pressedY>hsY) ) {
	// currentX += map(mouseY, 0, height, PI/3 * speed, -PI/3 * speed) -
	// map(pressedY, 0, height, PI/3 * speed, -PI/3 * speed);
	// currentZ += map(mouseX, 0, width, -PI/3 * speed, PI/3 * speed)-
	// map(pressedX, 0, width, -PI/3 * speed, PI/3 * speed);
	// currentX = testAngle(currentX);
	// currentZ = testAngle(currentZ);
	// rotX = currentX;
	// rotZ = currentZ;
	// pressedY = e.getY();
	// pressedX= e.getX();
	// }
	// }

	public float testAngle(float angle) {
		if (angle < -PI / 3)
			return -PI / 3;
		if (angle > PI / 3)
			return PI / 3;
		return angle;
	}

	public void mouseWheel(MouseEvent event) {
		speed += 0.05f * event.getCount();
		if (speed < MIN_SPEED)
			speed = MIN_SPEED;
		else if (speed > MAX_SPEED)
			speed = MAX_SPEED;
	}

	public void keyPressed() {
		if (key == CODED) {
			if (keyCode == SHIFT)
				SHIFT_PRESSED = true;
		}
	}

	public void keyReleased() {
		SHIFT_PRESSED = false;
	}

	public void mouseClicked(MouseEvent e) {
		if (SHIFT_PRESSED) {
			if ((e.getX() <= boxLength / 2 + width / 2 - cylinderBaseSize)
					&& (e.getX() >= -boxLength / 2 + width / 2
							+ cylinderBaseSize)
					&& (e.getY() <= boxLength / 2 + height / 2
							- cylinderBaseSize)
					&& (e.getY() >= -boxLength / 2 + height / 2
							+ cylinderBaseSize)) {
				PVector mouse = new PVector(e.getX() - width / 2,
						ball.location.y, e.getY() - height / 2);
				if (mouse.dist(ball.location) >= (radius + cylinderBaseSize)) {
					PVector position = new PVector(e.getX(), e.getY(), 0);
					if (checkCylinder(position))
						array.add(new PVector(e.getX(), e.getY(), 0));
				}
			}
		}
	}

	// check for other cylinders position (Cylinder)
	public boolean checkCylinder(PVector p) {
		if (array.isEmpty())
			return true;
		PVector copy = p.get();
		for (int i = 0; i < array.size(); i++) {
			copy.z = array.get(i).z;
			if (array.get(i).dist(copy) < 2 * cylinderBaseSize)
				return false;
		}
		return true;
	}

	public void drawShape(PVector p) {
		pushMatrix();
		rotateX(PI / 2);
		translate(p.x - width / 2, p.y - height / 2, p.z);
		shape(Cylinder);
		shape(base);
		popMatrix();
	}

	// draw Tree
	public void drawTree(PVector p) {
		pushMatrix();
		translate(p.x - width / 2, p.z, p.y - height / 2);
		rotateX(PI);
		shape(tree);
		popMatrix();
	}

	public boolean clockWise(List<PVector> corners) {
		float val = 0;
		for (int i = 0; i < corners.size() - 1; i++) {
			val += (corners.get(i + 1).x - corners.get(i).x)
					* (corners.get(i + 1).y + corners.get(i).y);
		}
		val += (corners.get(0).x - corners.get(corners.size() - 1).x)
				* (corners.get(0).y + corners.get(corners.size() - 1).y);
		if (val >= 0)
			println("clockWise");
		else
			println("not clockWise");
		return val >= 0;
	}

	//
	public PImage intesityFilter(PImage img, int Max, int Min) {
		result = createImage(img.width, img.height, RGB);
		for (int i = 0; i < img.width * img.height; i++) {
			// do something with the pixel img.pixels[i]
			float pixelBrightness = brightness(img.pixels[i]);
			if (pixelBrightness >= Min && pixelBrightness <= Max) {
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}
		return result;
	}

	// modify hue, if green (between 115 and 135) -> set to white, otherwise
	// black
	public PImage filterImage(PImage image) {
		result = createImage(image.width, image.height, ALPHA);
		for (int i = 0; i < image.width * image.height; i++) {
			float pixelHue = hue(image.pixels[i]);
			float pixelBrightness = brightness(image.pixels[i]);
			float pixelSaturation = saturation(image.pixels[i]);
			if (pixelHue >= hueMin && pixelHue <= hueMax
					&& pixelBrightness >= brightMin
					&& pixelBrightness <= brightMax
					&& pixelSaturation >= satuMin && pixelSaturation <= satuMax) {
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}
		return result;
	}

	// convolute image with kernel(parameters)
	public PImage convolute(PImage img, float[][] kernel) {
		float weight = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				weight += kernel[i][j];
			}
		}
		PImage result = createImage(img.width, img.height, ALPHA);
		int newValue = 0;
		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {
				newValue = 0;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						int clampedX = (x + i - 1 < 0) ? 0
								: ((x + i - 1 > img.width - 1) ? img.width - 1
										: x + i - 1);
						int clampedY = (y + j - 1 < 0) ? 0
								: ((y + j - 1 > img.height - 1) ? img.height - 1
										: y + j - 1);
						newValue += kernel[i][j]
								* (img.pixels[clampedY * img.width + clampedX]);
					}
				}
				result.pixels[y * img.width + x] = (int) (newValue / weight);
			}
		}
		return result;
	}

	// GaussinanBlur
	public PImage gaussianBlur(PImage img) {
		return convolute(img, gaussianKernel);
	}

	// Sobel image
	public PImage sobel(PImage img, float max) {
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		PImage result = createImage(img.width, img.height, ALPHA);

		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}

		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {
				float sum_v = 0.f;
				float sum_h = 0.f;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {

						int clampedX = (x + i - 1 < 0) ? 0
								: ((x + i - 1 > img.width - 1) ? img.width - 1
										: x + i - 1);
						int clampedY = (y + j - 1 < 0) ? 0
								: ((y + j - 1 > img.height - 1) ? img.height - 1
										: y + j - 1);
						sum_h += hKernel[i][j]
								* brightness(img.pixels[clampedY * img.width
										+ clampedX]);
						sum_v += vKernel[i][j]
								* brightness(img.pixels[clampedY * img.width
										+ clampedX]);

					}
				}
				int total = (int) Math.sqrt(Math.pow(sum_h, 2)
						+ Math.pow(sum_v, 2));
				if (total > max * 0.3f) { // 30% of
					// the
					// max
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}

			}

		}
		return result;
	}

	public ArrayList<PVector> hough(PImage edgeImg, int nLines) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;

		int rMax = (edgeImg.width + edgeImg.height) * 2 + 1;
		// dimensions of the accumulator
		int phiDim = (int) Math.round(Math.PI / discretizationStepsPhi);
		int rDim = (int) Math.round(rMax / discretizationStepsR);

		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < edgeImg.height; y++) {
			for (int x = 0; x < edgeImg.width; x++) {
				// Are we on an edge?
				if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
					for (int i = 0; i < phiDim; i++) {
						float phi = i * discretizationStepsPhi;
						float r = (float) (x * Math.cos(phi) + y
								* Math.sin(phi));
						float accR = (r / discretizationStepsR);
						accR += (rDim - 1) * 0.5f;
						float accPhi = (phi / discretizationStepsPhi);
						accumulator[(int) ((accPhi + 1) * (rDim + 2) + accR + 1)] += 1;
					}

				}
			}
		}

		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		int minVotes = 100;
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > minVotes) {
					boolean bestCandidate = true;
					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;
							if (accumulator[idx] < accumulator[neighbourIdx]) {
								// the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}

		}

		Collections.sort(bestCandidates, new HoughComparator(accumulator));

		// check if nLines > the size of bestCandidates
		if (nLines >= bestCandidates.size())
			nLines = bestCandidates.size();

		ArrayList<PVector> PLines = new ArrayList<PVector>();

		for (int i = 0; i < nLines; i++) {
			int idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (int) (idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;
			// store (r, phi) in PLines
			PLines.add(new PVector(r, phi));
		}
		return PLines;
	}

	public ArrayList<PVector> getIntersections(List<PVector> linesList2) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < linesList2.size() - 1; i++) {
			for (int j = i + 1; j < linesList2.size(); j++) {
				intersections.add(intersection(linesList2.get(i),
						linesList2.get(j)));
			}
		}
		return intersections;
	}

	public static PVector intersection(PVector line1, PVector line2) {
		float r1 = line1.x;
		float sin1 = (float) Math.sin(line1.y);
		float cos1 = (float) Math.cos(line1.y);

		// compute the intersection and add it to 'intersections'

		float r2 = line2.x;
		float sin2 = (float) Math.sin(line2.y);
		float cos2 = (float) Math.cos(line2.y);

		float d = cos2 * sin1 - cos1 * sin2;
		float x = (int) ((r2 * sin1 - r1 * sin2) / d);
		float y = (int) ((-r2 * cos1 + r1 * cos2) / d);
		return new PVector(x, y);
	}

	public void drawIntersections(ArrayList<PVector> pointList) {
		for (int i = 0; i < pointList.size(); i++) {
			PVector point = pointList.get(i);
			fill(255, 128, 0, 0);
			ellipse(point.x, point.y, 10, 10);
		}
	}

	public static List<PVector> sortCorners(List<PVector> quad) {
		// Sort corners so that they are ordered clockwise
		PVector a = quad.get(0);
		PVector b = quad.get(2);
		PVector center = new PVector((a.x + b.x) / 2, (a.y + b.y) / 2);
		Collections.sort(quad, new CWComparator(center));
		int index = 0;
		PVector origin = new PVector(0, 0, 0);
		float smallestDist = origin.dist(quad.get(0));
		for (int i = 1; i < quad.size(); i++) {
			if (origin.dist(quad.get(i)) <= smallestDist) {
				index = i;
			}
		}
		Collections.rotate(quad, index);
		return quad;
	}

	class HScrollbar {
		float barWidth; // Bar's width in pixels
		float barHeight; // Bar's height in pixels
		float xPosition; // Bar's x position in pixels
		float yPosition; // Bar's y position in pixels
		float sliderPosition, newSliderPosition; // Position of slider
		float sliderPositionMin, sliderPositionMax; // Max and min values of
													// slider
		boolean mouseOver; // Is the mouse over the slider?
		boolean locked; // Is the mouse clicking and dragging the slider now?

		/**
		 * @brief Creates a new horizontal scrollbar
		 *
		 * @param x
		 *            The x position of the top left corner of the bar in pixels
		 * @param y
		 *            The y position of the top left corner of the bar in pixels
		 * @param w
		 *            The width of the bar in pixels
		 * @param h
		 *            The height of the bar in pixels
		 */
		HScrollbar(float x, float y, float w, float h) {
			barWidth = w;
			barHeight = h;
			xPosition = x;
			yPosition = y;
			sliderPosition = xPosition + barWidth / 2 - barHeight / 2;
			newSliderPosition = sliderPosition;
			sliderPositionMin = xPosition;
			sliderPositionMax = xPosition + barWidth - barHeight;
		}

		/**
		 * @brief Updates the state of the scrollbar according to the mouse
		 *        movement
		 */
		public void update() {
			if (isMouseOver()) {
				mouseOver = true;
			} else {
				mouseOver = false;
			}
			if (mousePressed && mouseOver) {
				locked = true;
			}
			if (!mousePressed) {
				locked = false;
			}
			if (locked) {
				newSliderPosition = constrain(mouseX - barHeight / 2,
						sliderPositionMin, sliderPositionMax);
			}
			if (abs(newSliderPosition - sliderPosition) > 1) {
				sliderPosition = sliderPosition
						+ (newSliderPosition - sliderPosition);
			}
		}

		/**
		 * @brief Clamps the value into the interval
		 *
		 * @param val
		 *            The value to be clamped
		 * @param minVal
		 *            Smallest value possible
		 * @param maxVal
		 *            Largest value possible
		 *
		 * @return val clamped into the interval [minVal, maxVal]
		 */
		public float constrain(float val, float minVal, float maxVal) {
			return min(max(val, minVal), maxVal);
		}

		/**
		 * @brief Gets whether the mouse is hovering the scrollbar
		 *
		 * @return Whether the mouse is hovering the scrollbar
		 */
		public boolean isMouseOver() {
			if (mouseX > xPosition && mouseX < xPosition + barWidth
					&& mouseY > yPosition && mouseY < yPosition + barHeight) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * @brief Draws the scrollbar in its current state
		 */
		public void display() {
			noStroke();
			fill(255);
			rect(xPosition, yPosition, barWidth, barHeight);
			if (mouseOver || locked) {
				fill(0, 0, 0);
			} else {
				fill(102, 102, 102);
			}
			ellipse(sliderPosition, yPosition + hsH / 2, barHeight, barHeight);
		}

		/**
		 * @brief Gets the slider position
		 *
		 * @return The slider position in the interval [0,1] corresponding to
		 *         [leftmost position, rightmost position]
		 */
		public float getPos() {
			return (sliderPosition - xPosition) / (barWidth - barHeight);
		}
	}

	PVector normalF = new PVector(0, 0, 0);

	class Mover {
		PVector location;
		PVector velocity;

		Mover() {
			location = new PVector(width / 2,
					(height - boxHeight) / 2 - radius, 0);
			velocity = new PVector(0, 0, 0);
		}

		public void rolling() {
			PVector gravityForce = new PVector(sin(rotZ) * gravityConstant
					/ 200, 0, -sin(rotX) * gravityConstant / 80);
			float normalForce = 1;
			float mu = 0.01f;
			float frictionMagnitude = normalForce * mu;
			PVector friction = velocity.get();
			friction.mult(-1);
			friction.normalize();
			friction.mult(frictionMagnitude);
			velocity.add(gravityForce);
			velocity.add(friction);
			location.add(velocity);
		}

		public void display() {
			pushMatrix();
			noStroke();
			fill(95, 83, 41);
			translate(location.x, location.y, location.z);
			sphere(radius);
			popMatrix();
		}

		public void checkedges() {
			if (location.x >= boxLength / 2) {
				velocity.x = velocity.x * -1;
				location.x = boxLength / 2;
				score--;
			}
			if (location.x <= -boxLength / 2) {
				velocity.x = velocity.x * -1;
				location.x = -boxLength / 2;
				score--;
			}
			if ((location.z >= boxLength / 2)) {
				velocity.z = velocity.z * -1;
				location.z = boxLength / 2;
				score--;
			}
			if (location.z <= -boxLength / 2) {
				velocity.z = velocity.z * -1;
				location.z = -boxLength / 2;
				score--;
			}
		}

		public void checkCylinderCollision() {

			for (int i = 0; i < array.size(); i++) {
				PVector cylinderPosition = new PVector(array.get(i).x - width
						/ 2, location.y, array.get(i).y - height / 2);

				if (cylinderPosition.dist(location) < (radius + cylinderBaseSize)) {
					PVector normal = cylinderPosition.get();
					normal.y = location.y;
					normal.sub(location);
					normal.normalize();
					float k = -2 * normal.dot(velocity);
					velocity.add(PVector.mult(normal, k));
					score++;
					array.remove(i);
				}
			}
		}

	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "Game" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}
