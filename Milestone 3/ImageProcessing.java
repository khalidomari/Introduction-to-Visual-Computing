package imageprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

public class ImageProcessing extends PApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// if true => use camera, otherwise use image
	Boolean camera = false;
	Boolean scrollBar = false;

	// image, camera, result
	static PImage img; // image
	Capture cam; // camera
	PImage result;
	PImage img2;

	// ScrollBar 1 and 2
	HScrollbar thresholdBar1;
	HScrollbar thresholdBar2;

	// parametres
	int minVotes = 200;
	int nLines = 4;
	double treshold = 200;
	int hueMax = 138;
	int hueMin = 50;
	int brightMax = 135;
	int brightMin = 10;
	int satuMin = 150;
	int satuMax = 255;

	// Kernel
	float[][] kernel1 = { { 0, 0, 0 }, { 0, 2, 0 }, { 0, 0, 0 } };
	float[][] kernel2 = { { 0, 1, 0 }, { 1, 0, 1 }, { 0, 1, 0 } };
	float[][] gaussianKernel = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

	static List<PVector> linesList = new ArrayList<>();
	QuadGraph qGraph = new QuadGraph();

	public void setup() {
		size(800, 600);

		// camera
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

		// crate a new, initially transpparent result
		result = createImage(width, height, ALPHA);
		img = createImage(width, height, ALPHA);

		// initialize scrolbars 1 & 2
		thresholdBar1 = new HScrollbar(result.parent, 0, height - 40, width, 20);
		thresholdBar2 = new HScrollbar(result.parent, 0, height - 20, width, 20);
		
		
		
		// noLoop(); // no interactive behaviour: draw() will be called only
		// once.
	}

	public void draw() {

		if (scrollBar) {
			thresholdBar1.display();
			thresholdBar1.update();
			thresholdBar2.display();
			thresholdBar2.update();
			treshold = 100 * thresholdBar1.getPos();
		}

		if (camera) {
			if (cam.available() == true) {
				cam.read();
			}
			img = cam.get();
		} else {
			img = loadImage("board1.jpg");
		}
		
		
		// to test
		
		result = sobel(convolute(modifyHueBrightnessSaturation(img),
				gaussianKernel));

		// result = sobel(modifyHueAndBrightness(img));
		// result = sobel(modifyHueAndBrightness(img));

		image(img, 0, 0, width, height);
		PVector[] lines = new PVector[nLines];
		lines = hough(result, nLines);
		// getIntersections(lines);
		linesList = Arrays.asList(lines);
		qGraph.build(linesList, width, height);
		List<int[]> quads = new ArrayList<int[]>();
		quads = qGraph.findCycles();	
		
		for (int[] quad : quads) {
			PVector l1 = linesList.get(quad[0]);
			
			PVector l2 = linesList.get(quad[1]);
			PVector l3 = linesList.get(quad[2]);
			PVector l4 = linesList.get(quad[3]);
			// (intersection() is a simplified version of the
			// intersections() method you wrote last week, that simply
			// return the coordinates of the intersection between 2 lines)
			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);
			// Choose a random, semi-transparent colour
			Random random = new Random();
			fill(Math.min(255, random.nextInt(300)),
					Math.min(255, random.nextInt(300)),
					Math.min(255, random.nextInt(300)));
			quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);
		}
		

	}

	// treshold binary
	public PImage threshBinary(PImage img) {
		result = createImage(width, height, RGB);
		for (int i = 0; i < img.width * img.height; i++) {
			// do something with the pixel img.pixels[i]
			if (brightness(img.pixels[i]) <= treshold) {
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}

		}
		return result;
	}

	// treshold binary inverted
	public void threshBinaryInverted() {
		result = createImage(width, height, RGB);
		for (int i = 0; i < img.width * img.height; i++) {
			// do something with the pixel img.pixels[i]
			if (brightness(img.pixels[i]) <= treshold) {
				result.pixels[i] = color(255, 255, 255);
			} else {
				result.pixels[i] = 0;
			}

		}
	}

	// modify hue, if green (between 115 and 135) -> set to white, otherwise
	// black
	public PImage modifyHueBrightnessSaturation(PImage image) {
		result = createImage(img.width, img.height, ALPHA);
		result = image.get();
		for (int i = 0; i < result.width * result.height; i++) {
			float pixelHue = hue(result.pixels[i]);
			float pixelBrightness = brightness(result.pixels[i]);
			float pixelSaturation = saturation(result.pixels[i]);
			if (pixelHue > hueMin && pixelHue < hueMax
					&& pixelBrightness > brightMin
					&& pixelBrightness < brightMax && pixelSaturation > satuMin
					&& pixelSaturation < satuMax) {
				result.pixels[i] = color(255);
			} else {
				result.pixels[i] = color(0);
			}
		}
		return result;
	}

	// convolute image with kernel(parameters)
	public PImage convolute(PImage img, float[][] kernel) {
		float weight = 0.f;
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

	// Sobel image
	public PImage sobel(PImage img) {
		float[][] hKernel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vKernel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		PImage result = createImage(img.width, img.height, ALPHA);

		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		float max = 300;

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

	public PVector[] hough(PImage edgeImg, int nLines) {
		float discretizationStepsPhi = 0.06f;
		float discretizationStepsR = 2.5f;
		int rMax = (edgeImg.width + edgeImg.height) * 2 + 1;
		// dimensions of the accumulator
		int phiDim = (int) Math.round(Math.PI / discretizationStepsPhi);
		nLines = 10; //
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
					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for (int i = 0; i < phiDim; i++) {
						double phi = i * discretizationStepsPhi;
						double r = (x * Math.cos(phi) + y * Math.sin(phi));
						int rad = (int) Math.round(r / discretizationStepsR);
						rad += (rDim - 1) * 0.5f;
						accumulator[(i + 1) * (rDim + 2) + rad + 1] += 1;
					}

				}
			}
		}

		// week10
		// if accumulator[idx] > minVote, add it to bestCandidates
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
		// for (int idx = 0; idx < accumulator.length; idx++) {
		// if (accumulator[idx] > minVotes && !bestCandidates.contains(idx)) {
		// bestCandidates.add(idx);
		// }
		// }

		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		int minVotes = 200;
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
		// if(nLines>=bestCandidates.size()){
		// nLines=bestCandidates.size();
		// }

		// check if nLines > the size of bestCandidates
		if (nLines >= bestCandidates.size())
			nLines = bestCandidates.size();

		PVector[] PLines = new PVector[nLines];

		for (int i = 0; i < nLines; i++) {
			int idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (int) (idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * discretizationStepsR;
			float phi = accPhi * discretizationStepsPhi;
			// store (r, phi) in PLines
			PLines[i] = new PVector(r, phi);

			
			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of
			// the image
		}
		return PLines;
	}

	public ArrayList<PVector> getIntersections(PVector[] lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < lines.length - 1; i++) {
			PVector line1 = lines[i];
			float r1 = line1.x;
			float sin1 = (float) Math.sin(line1.y);
			float cos1 = (float) Math.cos(line1.y);
			for (int j = i + 1; j < lines.length; j++) {
				// compute the intersection and add it to 'intersections'
				PVector line2 = lines[j];
				float r2 = line2.x;
				float sin2 = (float) Math.sin(line2.y);
				float cos2 = (float) Math.cos(line2.y);

				float d = cos2 * sin1 - cos1 * sin2;
				float x = (int) ((r2 * sin1 - r1 * sin2) / d);
				float y = (int) ((-r2 * cos1 + r1 * cos2) / d);
				intersections.add(new PVector(x, y));

				// draw the intersection
				fill(255, 128, 0);
				ellipse(x, y, 10, 10);
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

}

//
class HoughComparator implements Comparator<Integer> {
	int[] accumulator;

	public HoughComparator(int[] accumulator) {
		this.accumulator = accumulator;
	}

	@Override
	public int compare(Integer l1, Integer l2) {
		if (accumulator[l1] > accumulator[l2]
				|| (accumulator[l1] == accumulator[l2] && l1 < l2))
			return -1;
		return 1;
	}
}
