//
//  Lecture.java
//  Gypsum
//
//  Created by DLP on 7/28/09.
//  Copyright (c) 2009, Dan Lidral Porter
//  All rights reserved.

//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:

//  * Redistributions of source code must retain the above copyright notice, this list
//    of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright notice, this list
//    of conditions and the following disclaimer in the documentation and/or other materials
//    provided with the distribution.
//  * Neither the name of "Gypsum" nor the names of its contributors may be used to endorse
//    or promote products derived from this software without specific prior written permission.

//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
//  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
//  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
//  SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
//  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
//  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//

import java.util.zip.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.Image;

public class Lecture {
	private String[] paths;
	public Image[] images;
	public String dir, name;
	
	public Lecture (String[] theImages, Gypsum app) {
		paths = new String[theImages.length];
		images = new Image[theImages.length];
		
		for (int i = 0; i < theImages.length; i++) {
			paths[i] = theImages[i];
			
			File imageFile = new File(paths[i]);
			try {
				images[i] = (Image) ImageIO.read(imageFile);
			} catch (java.io.IOException e) {
				app.showWarning("there was an IO error while trying to load the image \"" + imageFile.getName() + "\".\nPlease make sure the file exists and is readable, and try again", (Exception) e);
				return;
			}
			
		}
		
		dir = null;
		name = null;
	}
	
	private Lecture() {
		dir = null;
		name = null;
	}
	
	
	public void save(String theDir, String theName, Gypsum app) {
		if (theName.endsWith(".lec")) {
			theName = theName.substring(0, theName.length()-4);
		}
		
		dir = theDir;
		name = theName;
		
		_save(app);
	}
	
	public void save(Gypsum app) {
		_save(app);
	}
	
	private void _save(Gypsum app) {
		try {
			File lec = new File(dir + name + ".lec");
			if (lec.exists()) {
				lec.delete();
			}
			lec.createNewFile();
			
			File saveDst = new File(dir + name);
			if (saveDst.exists()) {
				saveDst.delete();
			}
			
			saveDst.mkdirs();
			
			String path = dir + name;
			String[] savedImages = new String[paths.length];
			
			// copy all of the paths to the destination folder
			for (int i = 0; i < paths.length; i++) {
				
				String suffix = "";
				String[] imageFormats = ImageIO.getReaderFormatNames();
				
				for (int j = 0; j < imageFormats.length; j++) {
					if (paths[i].toLowerCase().endsWith("." + imageFormats[j].toLowerCase())) {
						suffix = imageFormats[j].toLowerCase();
					}
				}
				
				InputStream src = new FileInputStream(paths[i]);
				OutputStream dst = new FileOutputStream(path + "/" + (i+1) + "." + suffix);
				
				byte[] buf = new byte[1024];
				int len;
				
				while ((len = src.read(buf)) > 0) {
					dst.write(buf, 0, len);
				}
				
				src.close();
				dst.close();
				
				savedImages[i] = (i+1) + "." + suffix;
			}
			
			// zip up the destination folder into the .lec file
			lec.delete();
			ZipOutputStream zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(lec)));
			BufferedInputStream in = null;
			
			byte[] buf = new byte[1024];
			
			for (int i = 0; i < savedImages.length; i++) {
				
				in = new BufferedInputStream(new FileInputStream(path + "/" + savedImages[i]), 1024);
				zipout.putNextEntry(new ZipEntry(savedImages[i]));
				int len;
				while ((len = in.read(buf, 0, 1024)) > 0) {
					zipout.write(buf, 0, len);
				}
				zipout.closeEntry();
			}
			
			zipout.flush();
			zipout.close();
			
			// if mac os, set the creator code
			com.apple.eio.FileManager.setFileCreator(dir + name + ".lec", 0x4770736D);
			
			// delete the temporary folder we created to copy the paths into
			for (int i = 0; i < savedImages.length; i++) {
				File img = new File(path + "/" + savedImages[i]);
				img.delete();
			}
			saveDst.delete();
			
		} catch (Exception e) {
			app.showWarning("An error occurred while trying to save \"" + name + "\".\nPlease make sure you have permission to write to\nthe directory, and try again.", e);
		}
	}
	
	public static Lecture open(String theDir, String theName, Gypsum app) {
		try {
			String path = theDir + theName;
			ZipFile zipped = new ZipFile(path);
			int imageIndex = 1;
			
			Lecture lecture = new Lecture();
			lecture.dir = theDir;
			lecture.name = theName;
			lecture.images = new Image[0];
			
			while(true) {
				String suffix = "";
				String[] imageFormats = ImageIO.getReaderFormatNames();
				ZipEntry image = null;
				boolean foundImage = false;
				
				// try every available imageformat as a suffix
				for (int j = 0; j < imageFormats.length; j++) {
					if ((image = zipped.getEntry(imageIndex + "." + imageFormats[j])) != null) {
						foundImage = true;
						imageIndex++;
						break;
					}
				}
				if (!foundImage) {
					break;
				}
				
				InputStream imagein = zipped.getInputStream(image);
				
				// copy over the previously found images
				Image[] previousImages = lecture.images;
				lecture.images = new Image[lecture.images.length + 1];
				for (int i = 0; i < previousImages.length; i++) {
					lecture.images[i] = previousImages[i];
				}
				
				// add this image
				lecture.images[previousImages.length] = (Image) ImageIO.read(imagein);
				
			}
			
			return lecture;
		} catch (IOException e) {
			app.showWarning("An error occurred while trying to open \"" + theName + "\".\nPlease make sure the file is readable, and try again.", e);
			return null;
		}
		
	}
}
