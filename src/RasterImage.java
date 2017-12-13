// IP Ue1 WS2017/18 Vorgabe
//
// Copyright (C) 2017 by Klaus Jung
// All rights reserved.
// Date: 2017-08-18

import java.io.File;
import java.util.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class RasterImage {
	
	private static final int gray  = 0xffa0a0a0;

	public int[] argb;	// pixels represented as ARGB values in scanline order
	public int width;	// image width in pixels
	public int height;	// image height in pixels
	
	public RasterImage(int width, int height) {
		// creates an empty RasterImage of given size
		this.width = width;
		this.height = height;
		argb = new int[width * height];
		Arrays.fill(argb, gray);
	}
	
	public RasterImage(RasterImage image) {
		// copy constructor
		width = image.width;
		height = image.height;
		argb = image.argb.clone();
	}
	
	public RasterImage(File file) {
		// creates an RasterImage by reading the given file
		Image image = null;
		if(file != null && file.exists()) {
			image = new Image(file.toURI().toString());
		}
		if(image != null && image.getPixelReader() != null) {
			width = (int)image.getWidth();
			height = (int)image.getHeight();
			argb = new int[width * height];
			image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
		} else {
			// file reading failed: create an empty RasterImage
			this.width = 256;
			this.height = 256;
			argb = new int[width * height];
			Arrays.fill(argb, gray);
		}
	}
	
	public RasterImage(ImageView imageView) {
		// creates a RasterImage from that what is shown in the given ImageView
		Image image = imageView.getImage();
		width = (int)image.getWidth();
		height = (int)image.getHeight();
		argb = new int[width * height];
		image.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
	}
	
	public void setToView(ImageView imageView) {
		// sets the current argb pixels to be shown in the given ImageView
		if(argb != null) {
			WritableImage wr = new WritableImage(width, height);
			PixelWriter pw = wr.getPixelWriter();
			pw.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argb, 0, width);
			imageView.setImage(wr);
		}
	}
	

	public void binarizeWithThreshold(int threshold) {

		for(int pixel = 0; pixel < argb.length; ++pixel)
		{
			if(getGrayValue(argb[pixel]) > threshold)
				argb[pixel] = 0xFFFFFFFF;
			else
				argb[pixel] = 0xFF000000;
		}
	}
	
	/**
	 * 
	 * @return the threshold computed by iso-data
	 */
	public int binarizeWithIsoData() {

		// Treemap is sorted by key (in this case brightness)
		Map<Integer, Integer> pixels = new TreeMap<>();

		// initialize Treemap keys with values from 0 - 255
		for(int x = 0; x < 256; ++x)
		{
			pixels.put(x, 0);
		}

		// count occurences of grey pixels
		for(int pixel : argb)
		{
			int grey = getGrayValue(pixel);

			pixels.replace(grey, pixels.get(grey) + 1);
		}

		List<Integer> keys = new ArrayList<>(pixels.keySet());

		int oldThreshold = 0;
		int newThreshold = pixels.size() / 2;

		while(Math.abs((oldThreshold-newThreshold)) > 1)
		{
			List<Integer> lowerPixels = keys.subList(0, newThreshold);
			List<Integer> upperPixels = keys.subList(newThreshold, keys.size());

			int pa = 0;
			int ua = 0;
			for(int pixel : lowerPixels)
			{
				pa += pixels.get(pixel);
				ua += pixel * pixels.get(pixel);
			}

			int pb = 0;
			int ub = 0;
			for(int pixel : upperPixels)
			{
				pb += pixels.get(pixel);
				ub += pixel * pixels.get(pixel);
			}

			ua = ua / pa;
			ub = ub / pb;

			oldThreshold = newThreshold;
			newThreshold = (ua + ub) / 2;

			System.out.println("new threshold: "+newThreshold+"\n\n\n");
		}
		return newThreshold;
	}

	public int getGrayValue(int pixel)
	{
		int r = (pixel >> 16) 	& 0xff;
		int g = (pixel >> 8) 	& 0xff;
		int b =  pixel 			& 0xff;

		int grey = (r + g + b) / 3;

		if(grey > 255)
			grey = 255;

		return grey;
	}

}
