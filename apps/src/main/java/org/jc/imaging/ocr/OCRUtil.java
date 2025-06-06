package org.jc.imaging.ocr;


import io.xlogistx.gui.GUIUtil;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.zoxweb.shared.util.SUS;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class OCRUtil {
    private static final Lock lock = new ReentrantLock();
    private final Tesseract tesseract = new Tesseract();
    public static final OCRUtil SINGLETON = new OCRUtil();
    private String lastResult;


    private OCRUtil() {

    }

//    public static boolean compareImages(BufferedImage imgA, BufferedImage imgB) {
//        // Check if dimensions are the same
//        if (imgA == null || imgB == null ||
//                imgA.getWidth() != imgB.getWidth() ||
//                imgA.getHeight() != imgB.getHeight()) {
//            return false;
//        }
//
//        int width = imgA.getWidth();
//        int height = imgA.getHeight();
//
//        // Compare pixel by pixel
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                // Get the RGB values of the pixels
//                int pixelA = imgA.getRGB(x, y);
//                int pixelB = imgB.getRGB(x, y);
//
//                if (pixelA != pixelB) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

//
//    public static BufferedImage captureSelectedArea(Rectangle area) throws AWTException
//    {
//        Robot robot = new Robot();
//        return robot.createScreenCapture(area);
//    }
//
//    public  static Rectangle captureSelectedArea()
//            throws AWTException, InterruptedException
//    {
//
//
//
//
//        Condition cond = lock.newCondition();
//        SelectionWindow selectionWindow = new SelectionWindow(lock, cond);
//        selectionWindow.setVisible(true);
//        selectionWindow.toFront();
//
//
//        try
//        {
//            lock.lock();
//            cond.await();
//        }
//        finally
//        {
//            lock.unlock();
//        }
//
//
//
//

    /// /        int counter = 0;
    /// /        // Wait until the user has made a selection
    /// /        while (!selectionWindow.isSelectionMade() && counter < 50) {
    /// /            //counter++;
    /// /            //System.out.println("Sleeping: " + counter);
    /// /            Thread.sleep(100);
    /// /        }
//
//        selectionWindow.dispose();
//
//        // Get the selected area
//        return selectionWindow.getSelectedArea();
//    }
    public String tesseractOCRImage(String tesserActPath, String lang, BufferedImage image)
            throws TesseractException {
        return tesseractOCRImage(tesserActPath, lang, image, null);
    }

    public String tesseractOCRImage(String tesserActPath, String lang, BufferedImage image, BufferedImage oldImage)
            throws TesseractException {
        SUS.checkIfNulls("Null parameters", tesserActPath, lang, image);

        if (oldImage != null) {
            // compare the 2 images

            boolean result = GUIUtil.compareImages(image, oldImage);
            if (result)
                return lastResult;
        }
        lock.lock();
        String ret;
        try {
            tesseract.setDatapath(tesserActPath);
            tesseract.setLanguage(lang);
            ret = tesseract.doOCR(image);
            lastResult = lastResult;
        } finally {
            lock.unlock();

        }
        return ret;
    }


    public static void main(String[] args) {
        // Path to the image file
        int index = 0;
        // Update with your image path

        // Create a Tesseract instance
        //Tesseract tesseract = new Tesseract();

        // Set the tessdata directory (the folder containing training data files)
        // This should point to the 'tessdata' directory inside your Tesseract installation
        String tesserActPath = args[index++];

        String imagePath = args[index++];

        // Optionally, set the language
        //tesseract.setLanguage("eng"); // For English

        try {
            // Perform OCR on the image
            String result1 = OCRUtil.SINGLETON.tesseractOCRImage(tesserActPath, "eng", ImageIO.read(new File(imagePath)));

            // Print the result
            System.out.println("OCR Result:\n" + result1);

            String result2 = OCRUtil.SINGLETON.tesseractOCRImage(tesserActPath, "eng", ImageIO.read(new File(imagePath)));

            // Print the result
            System.out.println("OCR Result:\n" + result2);

            System.out.println(result2.equals(result1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
