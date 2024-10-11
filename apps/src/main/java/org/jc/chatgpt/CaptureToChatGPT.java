package org.jc.chatgpt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jc.imaging.ocr.OCRSelection;
import org.jc.imaging.ocr.OCRUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.RateCounter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class CaptureToChatGPT extends JFrame {

    public static final LogWrapper log = new LogWrapper(CaptureToChatGPT.class).setEnabled(true);
    private JButton startButton;
    private JButton stopButton;
    private JButton clearPromptButton;
    private JButton selectButton;
    private JTextField refreshRateField;
    private JTextArea promptTextArea;
    private JTextArea resultTextArea;

    private OCRSelection ocrSelection;
    private BufferedImage lastCapture;


    static private Rectangle selectedArea;
    private Timer timer;
    private RateCounter rc = new RateCounter("app");
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    // API keys (replace with your actual keys)
    private static String ocrApiKey = null; // Replace with your OCR.space API key
    private static String openAiApiKey = null; // Replace with your OpenAI API key

    public CaptureToChatGPT() {
        setTitle("Screen OCR ChatGPT Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initComponents();
    }


    private JPanel createWorkPanel()
    {
        // Create the panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());

        // Create GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();

        // Labels
        JLabel resultLabel = new JLabel("GPT Result");
        JLabel promptLabel = new JLabel("GPT Prompt");

        // TextAreas
        resultTextArea = new JTextArea();
        promptTextArea = new JTextArea();

        // Make TextAreas wrap lines and scrollable
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);

        promptTextArea.setLineWrap(true);
        promptTextArea.setWrapStyleWord(true);

        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        JScrollPane promptScrollPane = new JScrollPane(promptTextArea);

        // Set preferred sizes for initial appearance
        resultScrollPane.setPreferredSize(new Dimension(250, 150));
        promptScrollPane.setPreferredSize(new Dimension(250, 150));

        // Insets for padding
        Insets padding = new Insets(5, 5, 5, 5);

        // Add labels to the first row
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = padding;
        gbc.weightx = 0.5; // Equal horizontal space for labels
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(resultLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(promptLabel, gbc);

        // Add TextAreas to the second row
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = padding;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0; // Allows vertical expansion
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(resultScrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(promptScrollPane, gbc);

        return panel;
    }
    private void initComponents() {
        // Panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        clearPromptButton = new JButton("Clear Prompt");
        selectButton = new JButton("Select");
        ocrSelection = new OCRSelection(this);
        ocrSelection.selectionBox.setName("OCR");

        refreshRateField = new JTextField("5s", 5); // Default refresh rate is 5 seconds

        //controlPanel.add(selectButton);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(clearPromptButton);
        controlPanel.add(new JLabel("Refresh Rate (s):"));
        controlPanel.add(refreshRateField);

        controlPanel.add(new JLabel("OCR"));
        controlPanel.add(ocrSelection.selectionBox);

        // Text areas for OCR text and result
//        promptTextArea = new JTextArea(10, 40);
//        resultTextArea = new JTextArea(10, 40);
//
//        JScrollPane ocrScrollPane = new JScrollPane(promptTextArea);
//        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
//
//        promptTextArea.setLineWrap(true);
//        promptTextArea.setWrapStyleWord(true);
//        resultTextArea.setLineWrap(true);
//        resultTextArea.setWrapStyleWord(true);

        // Panel for text areas
//        JPanel textPanel = new JPanel(new GridLayout(2, 1));
//        textPanel.add(new JLabel("OCR Text:"));
//        textPanel.add(ocrScrollPane);
//        textPanel.add(new JLabel("ChatGPT Response:"));
//        textPanel.add(resultScrollPane);


//        JPanel textPanel = new JPanel();
//        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
//        textPanel.add(new JLabel("Prompt Text:"));
//        textPanel.add(ocrScrollPane);
//        textPanel.add(new JLabel("ChatGPT Response:"));
//        textPanel.add(resultScrollPane);


        // Add panels to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(controlPanel, BorderLayout.NORTH);
        getContentPane().add(createWorkPanel(), BorderLayout.CENTER);

        // Button actions
        selectButton.addActionListener(e -> selectProcessing());
        startButton.addActionListener(e -> startProcessing());
        clearPromptButton.addActionListener(e -> promptTextArea.setText(""));


//        startButton.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseReleased(MouseEvent e)
//            {
//                startProcessing();
//
//            }
//        });

        stopButton.addActionListener(e -> stopProcessing());

        // Initially, stop button is disabled
        stopButton.setEnabled(false);
    }

    private void startProcessing() {
        if (isRunning.get()) {
            return;
        }
        isRunning.set(true);

        // Disable start button and enable stop button
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // Parse refresh rate
        int refreshRate;
        try {
            refreshRate = (int)Const.TimeInMillis.toMillis(refreshRateField.getText()); //// Convert to milliseconds
            if (log.isEnabled()) log.getLogger().info("refreshRate: " + refreshRate);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid refresh rate. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            stopProcessing();
            return;
        }
//        this.setEnabled(false);
//        // Activate screen selection
//        try {
//            selectedArea = captureScreenSelection();
//            if (selectedArea == null) {
//                JOptionPane.showMessageDialog(this, "No area selected.", "Info", JOptionPane.INFORMATION_MESSAGE);
//                stopProcessing();
//                return;
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Error during screen selection.", "Error", JOptionPane.ERROR_MESSAGE);
//            stopProcessing();
//            return;
//        }
//
//        this.setEnabled(true);
        // Set up timer task
        timer = new Timer(refreshRate, e -> {
            new Thread(() -> {
                try {
                    // Capture the selected screen area
                    BufferedImage image = OCRUtil.SINGLETON.captureSelectedArea(selectedArea);

                    rc.start();
                    if (OCRUtil.SINGLETON.compareImages(image, lastCapture)) {
                        lastCapture = image;
                        rc.stop();
                        if (log.isEnabled()) log.getLogger().info("Image compare equal, it took: " + Const.TimeInMillis.toString(rc.lastDeltaInMillis()));
                        return;
                    } else {
                        lastCapture = image;
                    }

                    if (log.isEnabled()) log.getLogger().info("New image to process, it took: " + Const.TimeInMillis.toString(rc.lastDeltaInMillis()));
                    String text = null;

                    NVGenericMap selectionInfo = ocrSelection.getSelectionInfo();
                    if (selectionInfo != null)
                    {
                        switch (selectionInfo.getName())
                        {
                            case "local-ocr":
                                text = OCRUtil.SINGLETON.tesseractOCRImage(selectionInfo.getValue("path"), selectionInfo.getValue("language"), image);
                                break;

                            case "remote-ocr":
                                // Perform OCR
                                text = performOCRWithOCRSpace(selectionInfo.getValue("image-format"), image, selectionInfo.getValue("api-key"));
                                break;
                        }
                        String ocrText = text;
                        SwingUtilities.invokeLater(() -> promptTextArea.setText(ocrText));
                    }


//                    // Save image (optional)
//                    ImageIO.write(image, "png", new File("screenshot.png"));
//
//                    // Perform OCR
//                    ocrText = performOCRWithOCRSpace("screenshot.png", ocrApiKey);

                    // Update OCR text area

                    return;
                    // Send to ChatGPT
//                    String response = sendToChatGPT(ocrText, openAiApiKey);
//
//                    // Update result text area
//                    SwingUtilities.invokeLater(() -> resultTextArea.setText(response));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
        timer.setInitialDelay(0); // Start immediately
        timer.start();
    }

    private void stopProcessing() {
        // Enable start button and disable stop button
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        this.setEnabled(true);
        if (!isRunning.get()) {
            return;
        }
        isRunning.set(false);

        // Stop the timer
        if (timer != null) {
            timer.stop();
        }
    }

    private void selectProcessing()
    {
        this.setVisible(false);
        try {
            selectedArea = OCRUtil.SINGLETON.captureSelectedArea();
            if (log.isEnabled()) log.getLogger().info("SelectedArea: " + selectedArea);
        } catch (Exception e) {
           e.printStackTrace();
        }
        finally {
            this.setVisible(true);
        }

    }



    // Function to capture screen selection
//    private static Rectangle captureScreenSelection() throws AWTException, InterruptedException {
//        // Create a full-screen window for selection
//        SelectionWindow selectionWindow = new SelectionWindow();
//        selectionWindow.setVisible(true);
//        selectionWindow.toFront();
//
//        int counter = 0;
//        // Wait until the user has made a selection
//        while (!selectionWindow.isSelectionMade() && counter < 50) {
//            //counter++;
//            //if (log.isEnabled()) log.getLogger().info("Sleeping: " + counter);
//            Thread.sleep(100);
//        }
//
//        selectionWindow.dispose();
//
//        // Get the selected area
//        return selectionWindow.getSelectedArea();
//    }

    // Custom window for rectangle selection (same as before)
//    static class RectangleSelectionWindow extends JWindow {
//        private Point startPoint;
//        private Point endPoint;
//        private Rectangle selectionBounds;
//        private boolean selectionMade = false;

//        public RectangleSelectionWindow() {
//            setAlwaysOnTop(true);
//            if (log.isEnabled()) log.getLogger().info(Toolkit.getDefaultToolkit().getScreenSize());
//            setSize(Toolkit.getDefaultToolkit().getScreenSize());
//            setBackground(new Color(0, 0, 0, 50)); // Semi-transparent background
//
//            // Mouse listeners
//            addMouseListener(new MouseAdapter() {
//                @Override
//                public void mousePressed(MouseEvent e) {
//
//                    startPoint = e.getPoint();
//                    endPoint = startPoint;
//                    if (log.isEnabled()) log.getLogger().info("mousePreset: " + startPoint);
//                    repaint();
//                }
//
//                @Override
//                public void mouseReleased(MouseEvent e) {
//                    endPoint = e.getPoint();
//                    selectionBounds = getSelectionRectangle();
//                    if (log.isEnabled()) log.getLogger().info("mouseReleased: " + endPoint);
//                    selectionMade = true;
//                }
//            });
//
//            addMouseMotionListener(new MouseMotionAdapter() {
//                @Override
//                public void mouseDragged(MouseEvent e) {
//                    endPoint = e.getPoint();
//                    repaint();
//                }
//            });
//        }
//
//        @Override
//        public void paint(Graphics g) {
//            super.paint(g);
//            if (startPoint != null && endPoint != null) {
//                Graphics2D g2d = (Graphics2D) g;
//                g2d.setColor(Color.RED);
//                Rectangle rect = getSelectionRectangle();
//                g2d.draw(rect);
//            }
//        }
//
//        private Rectangle getSelectionRectangle() {
//            int x = Math.min(startPoint.x, endPoint.x);
//            int y = Math.min(startPoint.y, endPoint.y);
//            int width = Math.abs(startPoint.x - endPoint.x);
//            int height = Math.abs(startPoint.y - endPoint.y);
//            return new Rectangle(x, y, width, height);
//        }
//
//        public Rectangle getSelectionBounds() {
//            return selectionBounds;
//        }
//
//        public boolean isSelectionMade() {
//            return selectionMade;
//        }
//    }

    // Function to perform OCR using OCR.space API (same as before)
    public static String performOCRWithOCRSpace(String imageFormat, BufferedImage image, String apiKey) {
        try {
            String urlStr = "https://api.ocr.space/parse/image";
            URL url = new URL(urlStr);

            // Prepare the image file
            UByteArrayOutputStream ubaos = new UByteArrayOutputStream();
            ImageIO.write(image, "png", ubaos);
            String imageName = "Capture."+imageFormat;

            if (log.isEnabled()) log.getLogger().info("image size: " + ubaos.size());


            // Setup the connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", apiKey);
            conn.setDoOutput(true);

            // Prepare the multipart request
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            // Add image file
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"" + imageName+ "\"\r\n");
            writer.write("Content-Type: image/"+imageFormat + "\r\n\r\n");
            writer.flush();
            outputStream.write(ubaos.getInternalBuffer(),0, ubaos.size());
            outputStream.flush();
            writer.write("\r\n");

            // Add language parameter (optional)
            writer.write("--" + boundary + "\r\n");
            writer.write("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
            writer.write("eng\r\n");

            // Close the multipart request
            writer.write("--" + boundary + "--\r\n");
            writer.flush();
            writer.close();

            // Get the response
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder response = new StringBuilder();
            String output;

            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            conn.disconnect();

            // Parse the OCR result
            return parseOCRSpaceResult(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Helper function to read file to byte array (same as before)
    private static byte[] readFileToByteArray(File file) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        int readNum;

        while ((readNum = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, readNum);
        }
        fis.close();
        return bos.toByteArray();
    }

    // Function to parse OCR.space API response (same as before)
    private static String parseOCRSpaceResult(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray parsedResults = jsonObject.getAsJsonArray("ParsedResults");
        if (parsedResults != null && parsedResults.size() > 0) {
            JsonObject parsedResult = parsedResults.get(0).getAsJsonObject();
            String extractedText = parsedResult.get("ParsedText").getAsString();
            return extractedText.trim();
        }
        return "";
    }

    // Function to send text to ChatGPT and receive a response (same as before)
    public static String sendToChatGPT(String prompt, String apiKey) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set up the connection properties
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            // Create the JSON request body
            String input = "{\n" +
                    "  \"model\": \"gpt-3.5-turbo\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": \"" + prompt.replace("\"", "\\\"") + "\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Read the response
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream()), "UTF-8"));

                StringBuilder response = new StringBuilder();
                String output;

                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
                br.close();

                // Parse the assistant's reply from the JSON response
                String assistantReply = parseAssistantReply(response.toString());
                return assistantReply;

            } else {
                // Read the error response
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    br.close();
                    if (log.isEnabled()) log.getLogger().info("Error Response: " + errorResponse.toString());
                }
                if (log.isEnabled()) log.getLogger().info("HTTP Error Code: " + responseCode);
                return "Error: Unable to get response from ChatGPT.";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Exception occurred while communicating with ChatGPT.";
        }
    }

    // Function to parse the assistant's reply from the JSON response (same as before)
    private static String parseAssistantReply(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonArray choices = jsonObject.getAsJsonArray("choices");
        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.getAsJsonObject("message");
        String content = message.get("content").getAsString();
        return content.trim();
    }

    // Main method
    public static void main(String ...args) {

        int index = 0;
        ParamUtil.ParamMap params = ParamUtil.parse("=", args);

        boolean selectArea = params.booleanValue("cap", true);
        ocrApiKey = params.stringValue("ocr-key", true);
        openAiApiKey = params.stringValue("gpt-key", true);

        if (selectArea) {
            try {
                selectedArea = OCRUtil.SINGLETON.captureSelectedArea();
                if (log.isEnabled()) log.getLogger().info("SelectedArea: " + selectedArea);
            } catch (AWTException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        SwingUtilities.invokeLater(() -> {
            CaptureToChatGPT app = new CaptureToChatGPT();
            app.setVisible(true);
        });
    }
}
