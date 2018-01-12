// IP Ue1 WS2017/18 Vorgabe
//
// Copyright (C) 2017 by Klaus Jung
// All rights reserved.
// Date: 2017-08-18

import java.util.*;

public class Filter {

	private static int[] kernel4 = new int[]{0, 1, 0,
			1, 1, 1,
			0, 1, 0};

	private static int[] kernel8 = new int[]{1, 1, 1,
			1, 1, 1,
			1, 1, 1};

	private static int[] seqKernel = new int[]{1, 1, 1,
			1, 1, 0,
			0, 0, 0};

	// left						= white 0 - new black 2
	private static int[][] potraceKernelsOut = new int[][]{{0, 1,
			1, 1},
			// right			= white 3 - new black 1
 			{0, 1,
					0, 0},
			// straight 		= white 2 - new black 3
			{0, 1,
					0, 1},
			// diag - links 	=  white 0 - new Black2
			{0, 1,
					1, 0}};

	private static int[][] potraceKernelsIn = new int[][]{{ 1, 0,
															0, 0},
															// right			= white 3 - new black 1
															{1, 0,
															 1, 1},
															// straight 		= white 2 - new black 3
															{1, 0,
															 1, 0},
															// diag - links 	=  white 0 - new Black2
															{1, 0,
															 0, 1}};

	private static Set<String> pathsIDs = null;
	private static int[] rotCompValues = new int[4];
	private static int kernelStartPos = 1;
	private static int horizontalStep = 0; // gets set in potrace()
	private static int verticalStep = 1;


	public static void outline(RasterImage src, RasterImage dst) {

		erode(src, dst, kernel4);
		invert(dst);

		for (int pixel = 0; pixel < src.argb.length; ++pixel) {
			// if the source img and eroded & inverted img differ in current pixel, the pixel of dst gets white
			if (!(src.argb[pixel] == dst.argb[pixel]))
				dst.argb[pixel] = 0xFFFFFFFF;
			else
				dst.argb[pixel] = 0xFF000000;
		}
	}

	private static void erode(RasterImage src, RasterImage dst, int[] kernel) {
		int kernelSideLenght = (int) Math.sqrt(kernel.length);

		for (int height = 0; height < src.height; ++height) {
			for (int width = 0; width < src.width; ++width) {
				int kernelCenterPos = (height * src.width) + width;

				// only do follwing 'erode-computation' if the current pixel is black
				if (src.argb[kernelCenterPos] == 0xFFFFFFFF) {
					boolean match = true;

					// iterate over kernel and get underlying pixels from image, with center of kernel as '0', to make computation for 'imagePxelPos' easier
					for (int kernelHeight = -(kernelSideLenght / 2); kernelHeight <= (kernelSideLenght / 2); ++kernelHeight) {
						for (int kernelWidth = -(kernelSideLenght / 2); kernelWidth <= (kernelSideLenght / 2); ++kernelWidth) {
							int kernelPos = ((kernelHeight + 1) * kernelSideLenght) + (kernelWidth + 1);

							if (kernel[kernelPos] == 1) {
								// get underlying pixel and check if it is black
								int imagePixelPos = kernelCenterPos + (src.width * kernelHeight) + kernelWidth;

								if (!checkBorders(kernelCenterPos, imagePixelPos, src.height, src.width, width)) {
									match = false;
									break;
								}

								// if pixel is white then the underlying pixel do not conform the kernel -> the pixel at kernel center will be turned white
								// imagePixelPos < 0 are pixels with are out of bounds and also 'white'
								if (src.argb[imagePixelPos] == 0xFF000000 /*|| imagePixelPos < 0*/) {
									match = false;
									break;
								}
							}
						}
						if (!match)
							break;
					}

					if (match)
						dst.argb[kernelCenterPos] = 0xFFFFFFFF;
					else
						dst.argb[kernelCenterPos] = 0xFF000000;
				}
			}
		}
	}

	private static void invert(RasterImage dst) {
		for (int pixel = 0; pixel < dst.argb.length; ++pixel) {
			// if black turn white and vice versa
			if (dst.argb[pixel] == 0xFFFFFFFF)
				dst.argb[pixel] = 0xFF000000;
			else
				dst.argb[pixel] = 0xFFFFFFFF;
		}
	}

	private static boolean checkBorders(int kernelCenter, int kernelPos, int imgHeight, int imgWidth, int currImgWidth) {

		if (kernelPos < 0 || kernelPos > (imgWidth * imgHeight) - 1)
			return false;

			// left side
		else if ((kernelCenter % imgHeight) == 0 && currImgWidth == 0) {
			// normalize kernelPos -> if kernelpos < center
			int normKernelPos = kernelPos + imgWidth;

			if (kernelCenter > normKernelPos)
				return false;
		}
		// right side
		else if ((kernelCenter % (imgHeight - 1)) == 0 && currImgWidth == (imgWidth - 1)) {
			int normKernelPos = kernelPos + imgWidth;

			if (kernelCenter < normKernelPos)
				return false;
		}

		return true;
	}


	// ------------------------------------------------------------
	// 					IP2
	// ------------------------------------------------------------

	public static void methodSelection(BinarizeViewController.FillingType method, RasterImage img) {
		switch (method) {
			case DEPTH:
				// warmup
				for (int i = 0; i < 1000; ++i) {
					depthFirst(img);
				}

				long depthStart = System.currentTimeMillis();
				depthFirst(img);
				long depthStop = System.currentTimeMillis();

				System.out.println("Time for depth first in ms: " + (depthStop - depthStart));
				break;
			case BREADTH:
				// warmup
				for (int i = 0; i < 1000; ++i) {
					breadthFirst(img);
				}

				long breathStart = System.currentTimeMillis();
				breadthFirst(img);
				long breathStop = System.currentTimeMillis();

				System.out.println("Time for breath first in ms: " + (breathStop - breathStart));
				break;
			case SEQUENTIAL:
				// warmup
				for (int i = 0; i < 1000; ++i) {
					sequential(img);
				}

				long seqStart = System.currentTimeMillis();
				breadthFirst(img);
				long seqStop = System.currentTimeMillis();

				System.out.println("Time for sequential in ms: " + (seqStop - seqStart));
				break;
		}

	}

	public static void depthFirst(RasterImage img) {
		// LIFO
		Stack<Integer> stack = new Stack();
		//List<Integer> stackLength = new LinkedList<>();

		int color = getRandomColor();

		for (int height = 0; height < img.height; ++height) {
			for (int width = 0; width < img.width; ++width) {
				// store coordinates width and height as absolute position
				int position = (height * img.width) + width;
				if (img.argb[position] == 0xFF000000)
					stack.push(position);

				while (!stack.isEmpty()) {
					int pixel = stack.pop();

					//if(img.argb[pixel] == 0xFF000000) {
					img.argb[pixel] = color;

					for (int pos : positions(kernel8, pixel, img.width)) {
						// private static boolean checkBorders(int kernelCenter, int kernelPos, int imgHeight, int imgWidth, int currImgWidth)

						if (checkBorders(pixel, pos, img.height, img.width, width)) {
							if (img.argb[pos] == 0xFF000000)
								stack.push(pos);
						}
					}
					//stackLength.add(stack.size());
					//}
				}
				color = getRandomColor();
			}
		}

		//System.out.println("Size of stack: "+Collections.max(stackLength));
	}

	public static void breadthFirst(RasterImage img) {
		//FIFO
		List<Integer> queue = new LinkedList<>();
		//List<Integer> queueLength = new LinkedList<>();

		int color = getRandomColor();

		for (int height = 0; height < img.height; ++height) {
			for (int width = 0; width < img.width; ++width) {
				// store coordinates width and height as absolute position
				queue.add((height * img.width) + width);

				while (!queue.isEmpty()) {
					int pixel = queue.remove(0);

					if (img.argb[pixel] == 0xFF000000) {
						img.argb[pixel] = color;

						for (int pos : positions(kernel8, pixel, img.width)) {
							// private static boolean checkBorders(int kernelCenter, int kernelPos, int imgHeight, int imgWidth, int currImgWidth)

							if (checkBorders(pixel, pos, img.height, img.width, width)) {
								if (img.argb[pos] == 0xFF000000)
									queue.add(pos);
							}
						}
						//queueLength.add(queue.size());
					}
				}
				color = getRandomColor();
			}
		}

		//System.out.println("Size of queue: "+Collections.max(queueLength));
	}

	private static List<Integer> positions(int[] kernel, int kernelCenter, int imgWidth) {
		List<Integer> positions = new LinkedList<>();
		int kernelSideLenght = (int) Math.sqrt(kernel.length);

		// iterate over kernel and get underlying pixel positions, with center of kernel as '0', to make computation for 'imagePxelPos' easier
		for (int kernelHeight = -(kernelSideLenght / 2); kernelHeight <= (kernelSideLenght / 2); ++kernelHeight) {
			for (int kernelWidth = -(kernelSideLenght / 2); kernelWidth <= (kernelSideLenght / 2); ++kernelWidth) {
				int kernelPos = ((kernelHeight + 1) * kernelSideLenght) + (kernelWidth + 1);

				if (kernel[kernelPos] == 1) {
					int imagePixelPos = kernelCenter + (imgWidth * kernelHeight) + kernelWidth;

					positions.add(imagePixelPos);
				}
			}
		}
		return positions;
	}

	public static void sequential(RasterImage img) {
		//FIFO
		Set<int[]> collisions = new HashSet<>();
		Set<Integer> usedLabels = new HashSet<>();

		int color = getRandomColor();
		usedLabels.add(color);

		for (int height = 0; height < img.height; ++height) {
			for (int width = 0; width < img.width; ++width) {
				int kernelCenterPos = (height * img.width) + width;

				if (img.argb[kernelCenterPos] == 0xFF000000) {
					List<Integer> labels = getBackgroundPixels(img, seqKernel, kernelCenterPos, width);
					Set<Integer> labelOccurences = new HashSet<>(labels);

					if (labelOccurences.isEmpty()) {
						img.argb[kernelCenterPos] = color;
						color = getRandomColor();
						usedLabels.add(color);
					} else if (labelOccurences.size() == 1) {
						img.argb[kernelCenterPos] = labels.get(0);
					} else if (labelOccurences.size() > 1) {
						// get label with highest occurency
						int assignLabel = 0;
						int highestOcc = 0;

						for (Integer label : labelOccurences) {
							int occ = Collections.frequency(labels, label);

							if (highestOcc < occ) {
								assignLabel = label;
								highestOcc = occ;
							}
						}

						img.argb[kernelCenterPos] = assignLabel;
						labelOccurences.remove(assignLabel);

						// register collisions
						for (Integer label : labelOccurences) {
							collisions.add(new int[]{assignLabel, label});
						}
					}

				}
			}
		}

		// resolve label collisions

		Vector<Set<Integer>> partitioning = new Vector<>();

		for (int usedLabel : usedLabels) {
			Set<Integer> set = new HashSet<>();
			set.add(usedLabel);

			partitioning.add(set);
		}

		for (int[] collision : collisions) {
			int ra = collision[0];
			int rb = collision[1];

			Set<Integer> aSet = null;
			Set<Integer> bSet = null;

			for (Set<Integer> currentSet : partitioning) {
				if (currentSet.contains(ra))
					aSet = currentSet;

				if (currentSet.contains(rb))
					bSet = currentSet;
			}

			// Ra =/= Rb
			if (!aSet.contains(rb) && !bSet.contains(ra)) {
				partitioning.remove(aSet);
				partitioning.remove(bSet);

				aSet.addAll(bSet);

				partitioning.add(aSet);
			}
		}

		// assign new labels
		for (int height = 0; height < img.height; ++height) {
			for (int width = 0; width < img.width; ++width) {
				int kernelCenterPos = (height * img.width) + width;
				int currLabel = img.argb[kernelCenterPos];

				if (!(currLabel == 0xFF000000) && !(currLabel == 0xFFFFFFFF)) {
					// search set with this color
					for (Set<Integer> currentSet : partitioning) {
						if (currentSet.contains(currLabel))
							img.argb[kernelCenterPos] = Collections.min(currentSet);
					}
				}
			}
		}
	}

	public static List<Integer> getBackgroundPixels(RasterImage img, int[] kernel, int kernelCenterPos, int currentWidth) {
		int kernelSideLenght = (int) Math.sqrt(kernel.length);
		List<Integer> labels = new ArrayList<>();

		// iterate over kernel and get underlying pixels from image, with center of kernel as '0', to make computation for 'imagePxelPos' easier
		for (int kernelHeight = -(kernelSideLenght / 2); kernelHeight <= (kernelSideLenght / 2); ++kernelHeight) {
			for (int kernelWidth = -(kernelSideLenght / 2); kernelWidth <= (kernelSideLenght / 2); ++kernelWidth) {
				int kernelPos = ((kernelHeight + 1) * kernelSideLenght) + (kernelWidth + 1);

				if (kernel[kernelPos] == 1) {
					// get underlying pixel and check if it has a label (not black or white)
					int imagePixelPos = kernelCenterPos + (img.width * kernelHeight) + kernelWidth;

					if (checkBorders(kernelCenterPos, imagePixelPos, img.height, img.width, currentWidth)) {
						int pixel = img.argb[imagePixelPos];

						if (!(pixel == 0xFF000000) && !(pixel == 0xFFFFFFFF)) {
							labels.add(pixel);
						}
					}
				}
			}
		}
		return labels;
	}

	private static int getRandomColor() {
		Random rnd = new Random();

		int r = rnd.nextInt(256);
		int g = rnd.nextInt(256);
		int b = rnd.nextInt(256);

		return ((255 << 24) | (r << 18) | (g << 8) | b);
	}


	// ------------------------------------------------------------
	// 					IP3
	// ------------------------------------------------------------


	public static List<Path> potrace(RasterImage img) {

		boolean outline = true;

		// set the verticalStep initially with the width of the image (one scanline)
		horizontalStep = img.width;

		// data structure to store vertexes / outlines of objects
		List<Path> paths = new LinkedList<>();
		pathsIDs = new HashSet<>();

		for (int height = 0; height < img.height; ++height) {

			// reset outline for each line of img
			outline = true;

			for (int width = 0; width < img.width; ++width) {

				int pos = (height * img.width) + width;

				// if black pixel with white pixel to the left is found AND border check of current position with borders of image is correct
				if (checkBorders(pos, pos - 1, img.height, img.width, width) &&
						img.argb[pos] == 0xFF000000 &&
						img.argb[pos - 1] == 0xFFFFFFFF)
				{
					List<Path> path = new LinkedList<>();
					Set<String> pathIDs = new HashSet<>();

					Path tmpPath = new Path(0,0,0);
					tmpPath.setBlackPixel(pos);
					tmpPath.setWhitePixel(pos-1);
					tmpPath.setBorder(outline);

					System.out.println("tmp: "+tmpPath.getID());

					// this border is not in another path
					if(!pathsIDs.contains(tmpPath.getID())) {

						// see getDirection()
						int turns = 0;

						// terminates when the next pixel has the same orientation and pixelPos as the first element
						while (pathIDs.add(tmpPath.getID())) {
							path.add(tmpPath);

							// the next Pixel is the Black Pixel of the returned Path Object
							tmpPath = getDirection(img,  tmpPath.getBlackPixel(), turns, width, outline);

							System.out.println("border between white: " + tmpPath.getWhitePixel() + " and black: " + tmpPath.getBlackPixel() + " matchingkernel: " + tmpPath.getDirection() + " outline: " + tmpPath.isOuterBorder());

							turns = 0;

							// a right turn adds one, a left turn subtracts 1 from turns -> see 'getDirection()' -> 'rotateKernels()'
							switch (tmpPath.getDirection()) {
								//case 3:
								case 0:
									turns = 1;
									break;
								case 1:
									turns = -1;
									break;
								case 3:
									if(outline)
										turns = 1;
									else
									{
										// if the last border has the same white pixel as the new border, the kernels only need to be turned once
										if(tmpPath.getWhitePixel() == path.get(path.size()-1).getWhitePixel())
											turns = 1;
										else
											turns = -1;
									}
									break;

								default:
									// straight
									break;
							}
						}

						paths.addAll(path);
						pathsIDs.addAll(pathIDs);

						// add empty path element to signal the end of a path --> see vectorisation
						paths.add(new Path(0,0,0));

						resetKernels();

						//System.out.println("ids " + pathIDs.toString());
						//System.out.println("path completed! added: " + path.toString() + "\n\n");
					}
				}
				// if this occurs, toggle the value of outline if this position isn't already a border
				if(checkBorders(pos, pos + 1, img.height, img.width, width) &&
						img.argb[pos] == 0xFF000000 &&
						img.argb[pos + 1] == 0xFFFFFFFF)
				{

					Path tmpPath = new Path(0,0,0);
					tmpPath.setBlackPixel(pos);
					tmpPath.setWhitePixel(pos+1);


					System.out.println("reverse id: "+tmpPath.getID());

					if(!pathsIDs.contains(tmpPath.getID()))
						outline = false;
					else
						outline = true;
				}
			}
		}

		System.out.println("size of paths for: "+img.toString()+" is: "+paths.size());
		return paths;
	}

	private static Path getDirection(RasterImage img, int startPos, int turns, int currImgWidth, boolean outline)
	{

		int rotations = Math.abs(turns);

		// only rotates kernels if turns != 0 BUT Return values to get correct pixel values with every call
		for(; rotations > 0; --rotations)
		{
			for (int kernel = 0; kernel < potraceKernelsOut.length; ++kernel)
			{
				potraceKernelsOut[kernel] = rotateKernels(turns, potraceKernelsOut[kernel]);
			}

			rotateKernelStartPos(turns);
		}


		int[] compValues = getCompValues();

		boolean match = false;

		for(int kernel = 0; kernel < potraceKernelsOut.length; ++kernel)
		{
			match = true;

			// potrace kernel and compValues have the same length
			for(int compValue = 0; compValue < compValues.length; ++compValue)
			{
				int pixelPos = startPos + compValues[compValue];

				int kernelBW = potraceKernelsOut[kernel][compValue];
				int imgBW = 0;

				// if the kernel pos is outside the bounds of the img, the pixel is white
				// TODO failure, currImgWidth is wrong here
				if (!checkBorders(startPos, pixelPos, img.height, img.width, currImgWidth))
					imgBW = 0xFFFFFFFF;
				else
					imgBW = img.argb[pixelPos];


				// if kernelValue is 1 -> underlying pixel should be black
				if (kernelBW == 1 && imgBW == 0xFF000000) {
					continue;
				}
				// if kernelValue is 0 -> underlying pixel should be white
				else if(kernelBW == 0 && imgBW == 0xFFFFFFFF){
					continue;
				}

				match = false;
				break;

			}

			// return the new Border
			if(match)
			{
				if(kernelStartPos != 1)
				{
					int iterations = (kernelStartPos == 0) ? 1 : kernelStartPos;
					rotCompValues = compValues;


					for(int i = 0; i < iterations; ++i)
						rotCompValues = rotateKernels(2, rotCompValues);

					//rotCompValues = rotateKernels();
					return getNextPath(rotCompValues, kernel, startPos, outline);
				}
				else
					return getNextPath(compValues, kernel, startPos, outline);
			}
		}

		// TODO return as failure
		return new Path(0,0,0);
	}

	// sets the white pixel and black pixel (position) in which betweeen the border lies
	private static Path getNextPath(int[] compValues, int kernelNum, int kernelStartPos, boolean outline)
	{
		Path newPath = new Path(0,0,0);
		newPath.setDirection(kernelNum);
		newPath.setBorder(outline);

		switch (kernelNum)
		{
			//case 3:
			case 0:
				newPath.setBlackPixel(kernelStartPos + compValues[2]);
				newPath.setWhitePixel(kernelStartPos + compValues[0]);
				break;
			case 1:
				newPath.setBlackPixel(kernelStartPos + compValues[1]);
				newPath.setWhitePixel(kernelStartPos + compValues[3]);
				break;
			case 2:
				newPath.setBlackPixel(kernelStartPos + compValues[3]);
				newPath.setWhitePixel(kernelStartPos + compValues[2]);
				break;

			case 3:
				if(outline)
				{
					newPath.setBlackPixel(kernelStartPos + compValues[2]);
					newPath.setWhitePixel(kernelStartPos + compValues[0]);
				}
				else
				{
					// if current path is an inline, check if 'opposite' border is already registered wit its ID
					int bP = kernelStartPos + compValues[2];
					int wP = kernelStartPos + compValues[3];

					Path testPath = new Path(bP, wP, 0);

					if(pathsIDs.contains(testPath.getID()))
					{
						newPath.setBlackPixel(kernelStartPos + compValues[2]);
						newPath.setWhitePixel(kernelStartPos + compValues[0]);
					}
					else
					{
						newPath.setBlackPixel(kernelStartPos + compValues[1]);
						newPath.setWhitePixel(wP);
					}
				}
				break;
		}

		return newPath;
	}

	private static void rotateKernelStartPos(int turns)
	{
		// left
		if(turns < 0)
		{
			switch (kernelStartPos)
			{
				case 0:
					kernelStartPos = 2;
					break;
				case 1:
					kernelStartPos = 0;
					break;
				case 2:
					kernelStartPos = 3;
					break;
				case 3:
					kernelStartPos = 1;
					break;
			}
		}
		else if(turns > 0)
		{
			switch (kernelStartPos)
			{
				case 0:
					kernelStartPos = 1;
					break;
				case 1:
					kernelStartPos = 3;
					break;
				case 2:
					kernelStartPos = 0;
					break;
				case 3:
					kernelStartPos = 2;
					break;
			}
		}
	}
	private static int[] rotateKernels(int turns, int[] kernel)
	{


			int[] copyKernel = Arrays.copyOf(kernel, kernel.length);

			// turn left
			if(turns < 0)
			{
				//System.out.println("rotate left");
				kernel[0] = copyKernel[1];
				kernel[1] = copyKernel[3];
				kernel[2] = copyKernel[0];
				kernel[3] = copyKernel[2];
			}
			// turn right
			else if(turns > 0)
			{
				//System.out.println("rotate right");
				kernel[0] = copyKernel[2];
				kernel[1] = copyKernel[0];
				kernel[2] = copyKernel[3];
				kernel[3] = copyKernel[1];

			}

			return kernel;
	}

	private static int[] getCompValues()
	{
		int[] compValues = new int[4];

		switch (kernelStartPos)
		{
			case 0:
				//kernelStartPos = 2;
				compValues = new int[]{0, verticalStep, horizontalStep, horizontalStep + verticalStep};
				break;
			case 1:
				//kernelStartPos = 0;
				compValues = new int[]{-verticalStep, 0, horizontalStep - verticalStep, horizontalStep};
				break;
			case 2:
				//kernelStartPos = 3;
				compValues = new int[]{-horizontalStep, -horizontalStep + verticalStep, 0, verticalStep};
				break;
			case 3:
				//kernelStartPos = 1;
				compValues = new int[]{-horizontalStep - verticalStep, -horizontalStep, -verticalStep, 0};
				break;
		}

		return compValues;
	}

	private static void resetKernels()
	{

		kernelStartPos = 1;

		potraceKernelsOut = new int[][]{{0, 1,
				1, 1},
				// right
				{0, 1,
						0, 0},
				// straight
				{0, 1,
						0, 1},
				// diag
				{0, 1,
						1, 0}};

		potraceKernelsIn = new int[][]{{ 1, 0,
				0, 0},
				// right			= white 3 - new black 1
				{1, 0,
						1, 1},
				// straight 		= white 2 - new black 3
				{1, 0,
						1, 0},
				// diag - links 	=  white 0 - new Black2
				{1, 0,
						0, 1}};
	}



	// ------------------------------------------------------------
	// 					IP4
	// ------------------------------------------------------------


	public static List<List<int[]>> vectorisation(List<Path> paths, int imgWidth)
	{
		// organizes paths in ring storages
		List<RingStorage<Path>> ringPaths = new LinkedList<>();
		RingStorage<Path> newPath = new RingStorage<>();

		for(Path path : paths)
		{
			// the beginning of a new path;
			if(path.getID().matches("00"))
			{
				ringPaths.add(newPath);
				newPath = new RingStorage<>();
			}
			else
			{
				newPath.addNode(path);
			}
		}

		List<List<int[]>> vectorPaths = new LinkedList<>();

		System.out.println("size of paths: "+ringPaths.size());

		// path vectors
		int[] v_i = null;
		int[] v_k = null;

		// contraints
		int[] c0 = new int[]{0,0};
		int[] c1 = new int[]{0,0};

		for(RingStorage<Path> ringPath : ringPaths)
		{
			List<List<int[]>> tmpVectorPaths = new ArrayList<>();
			Path initialHead = ringPath.getHead();

			do {
				Set<Integer> directions = new HashSet<>();
				RingStorage<Path> currRP = new RingStorage<>(ringPath); // TODO geht auch ohne das?
				List<int[]> tmpVectorPath = new ArrayList<>();

				Path pivot = currRP.getHead();
				Path path = null;

				// init the start point and add to the temporary vector path
				v_i = new int[]{pivot.getBlackPixel() / imgWidth,
								pivot.getBlackPixel() % imgWidth};


				tmpVectorPath.add(getCorrectPointOrientation(pivot, imgWidth));

				do {
					// on the first iteration path and currentHead are the same
					path = currRP.getNext();
					directions.add(path.getDirection());

					if(directions.size() > 3)
					{
						directions.clear();

						path = currRP.getPrevious();

						int[] nextPoint = new int[]{path.getBlackPixel() / imgWidth,
													path.getBlackPixel() % imgWidth};

						tmpVectorPath.add(getCorrectPointOrientation(path, imgWidth));

						// the new startPoint (v_i) is the current added point
						v_i = nextPoint;
					}
					else
					{
						v_k = new int[]{path.getBlackPixel() / imgWidth,
										path.getBlackPixel() % imgWidth};

						int[] v_ik = new int[]{v_k[0] - v_i[0],
								v_k[1] - v_i[1]};

						// is the vector v_ik inside of bounds
						if (checkConstraints(c0, c1, v_ik))
						{
							// update contraints
							if (!(Math.abs(v_ik[0]) <= 1) && !(Math.abs(v_ik[1]) <= 1))
							{
								c0 = updateC1(c0, v_ik);
								c1 = updateC1(c1, v_ik);
							}
						}
						else
						{
							//System.out.println("hurt constraint");
							//System.out.println("x: "+point[0]+" y: "+point[1]);

							// get the previous element, because constraints for current are false, and add to tmp path
							path = currRP.getPrevious();

							int[] prevPoint = new int[]{path.getBlackPixel() / imgWidth, path.getBlackPixel() % imgWidth};



							tmpVectorPath.add(getCorrectPointOrientation(path, imgWidth));

							// the new startPoint (v_i) is the current added point
							//v_i = nextPoint;

							v_i = prevPoint;

							c0 = new int[]{0,0};
							c1 = new int[]{0,0};

							directions.clear();
						}

					}

				}
				// stops when the ring iterated to the initial head
				while(!pivot.getID().matches(path.getID()));

				v_i = null;
				v_k = null;

				tmpVectorPaths.add(tmpVectorPath);

				// increment the index for a new start pos
				ringPath.incrementIndex();

				/*
				System.out.println("currentPaths: ");
				for(int[] point : tmpVectorPath)
					System.out.println("x: "+point[0]+" y: "+point[1]);


				System.out.println("\n\n: ");
				*/

			}
			// stops when the ring iterated to the initial head
			while(!initialHead.getID().matches(ringPath.getHead().getID()));



			/*
			System.out.println("length possible vector pahts: "+tmpVectorPaths.size());

				for(List<int[]> path : tmpVectorPaths)
				{
					for(int[] point : path)
						System.out.println("x: " + point[0] + " y: " + point[1]);

					System.out.println("\n\n");
				}



			System.out.println("\n\n: ");
			*/

			int size = 0;
			List<int[]> lowPoly = new LinkedList<>();

			// find shortes polygon -> polygon with the least subpaths
			for(List<int[]> path : tmpVectorPaths)
			{
				if(path.size() < size || size == 0)
				{
					size = path.size();
					lowPoly = path;
				}

			}

			vectorPaths.add(lowPoly);
		}

		/*
		System.out.println("paths: ");
		for(List<int[]> path : vectorPaths)
		{

			System.out.println("new route: ");
			for(int[] point : path)
				System.out.println("x: " + point[0] + " y: " + point[1]);
		}


		System.out.println("\n\n: ");
		*/
		return vectorPaths;
	}
	private static int[] getCorrectPointOrientation(Path path, int origImgWidth)
	{
		int[] nextPoint = new int[2];

		int black = path.getBlackPixel();
		int white = path.getWhitePixel();

		// every  point in vector path has third element to indicate an inline=0 or outline=1
		int outline = (path.isOuterBorder()) ? 1 : 0;

		// left to right
		if(black == (white + 1))
		{
			nextPoint =  new int[]{path.getBlackPixel() / origImgWidth,
									path.getBlackPixel() % origImgWidth,
									outline};
		}
		// up to down
		else if(black == (white + origImgWidth))
		{
			nextPoint =  new int[]{path.getBlackPixel() / origImgWidth,
									path.getBlackPixel() % origImgWidth,
									outline};
		}
		// right to left
		else if(black == (white - 1))
		{
			nextPoint =  new int[]{path.getBlackPixel() / origImgWidth,
									(path.getBlackPixel() % origImgWidth) + 1,
									outline};
		}
		// down to up
		else if(black == (white - origImgWidth))
		{
			nextPoint =  new int[]{(path.getBlackPixel() / origImgWidth) + 1,
									path.getBlackPixel() % origImgWidth,
									outline};
		}

		return nextPoint;
	}

	private static boolean checkConstraints(int[] c0, int[] c1, int[] v_ik)
	{
		// cross product
		if((c0[0] * v_ik[1]) - (c0[1] * v_ik[0]) < 0)
			return false;
		else if((c1[0] * v_ik[1]) - (c1[1] * v_ik[0]) > 0)
			return false;
		else
			return true;
	}

	private static int[] updateC0(int[] c0, int[] v_ik)
	{
		int[] d = new int[]{0,0};

		if(v_ik[1] >= 0 && (v_ik[1] > 0 || v_ik[0] < 0))
		{
			d[0] = v_ik[0] + 1;
		}
		else
		{
			d[0] = v_ik[0] - 1;
		}


		if(v_ik[0] <= 0 && (v_ik[0] < 0 || v_ik[1] < 0))
		{
			d[1] = v_ik[1] + 1;
		}
		else
		{
			d[1] = v_ik[1] - 1;
		}


		if((c0[0] * d[1]) - (c0[1] * d[0]) >= 0)
		{
			return d;
		}
		else
		{
			return c0;
		}
	}

	private static int[] updateC1(int[] c1, int[] v_ik)
	{
		int[] d = new int[]{0,0};

		if(v_ik[1] <= 0 && (v_ik[1] < 0 || v_ik[0] < 0))
		{
			d[0] = v_ik[0] + 1;
		}
		else
		{
			d[0] = v_ik[0] - 1;
		}

		if(v_ik[0] >= 0 && (v_ik[0] > 0 || v_ik[1] < 0))
		{
			d[1] = v_ik[1] + 1;
		}
		else
		{
			d[1] = v_ik[1] - 1;
		}

		if((c1[0] * d[1]) - (c1[1] * d[0]) <= 0)
		{
			return d;
		}
		else
		{
			return c1;
		}
	}
}

