package org.jc.debooleanizer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.xlogistx.api.ai.AIAPI;
import io.xlogistx.api.ai.AIAPIBuilder;
import io.xlogistx.audio.AudioRecorder;
import io.xlogistx.audio.AudioUtil;
import io.xlogistx.gui.DynamicComboBox;
import io.xlogistx.gui.GUIUtil;
import io.xlogistx.gui.LedWidget;
import io.xlogistx.gui.TreeTextWidget;
import io.xlogistx.http.NIOHTTPServer;
import io.xlogistx.http.NIOHTTPServerCreator;
import net.sourceforge.tess4j.TesseractException;
import okhttp3.OkHttpClient;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.DateUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.annotation.EndPointProp;
import org.zoxweb.shared.annotation.MappedProp;
import org.zoxweb.shared.filters.StringFilter;
import org.zoxweb.shared.http.HTTPAuthScheme;
import org.zoxweb.shared.http.HTTPAuthorization;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPServerConfig;
import org.zoxweb.shared.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


@MappedProp(name = "chat-gpt", id = "chat-gpt-capture")
public class ZeDebooleanizer extends JFrame {

    public static final LogWrapper log = new LogWrapper(ZeDebooleanizer.class).setEnabled(true);
    private JButton startButton;
    private JButton stopButton;
    private JButton clearPromptButton;
    private JButton selectButton;
    private JButton captureButton;

    private JTextField refreshRateField;
    //private FilterPromptPanel filterPromptPanel;
    private JTextArea responseFilterTA;
    private JTextArea captureTextArea;
    private JTextArea audioTextArea;
    private JFileChooser fileChooser;
    //private JButton fileChooserButton;
    private JButton stopRecording;
    private DynamicComboBox aiModelDCB;
    private JTextField recordingModelName;
    private JCheckBox autoCopyToClipboardCB;
    private JCheckBox uniqueCaptureCB = null;
    private ConfigSelection configSelection;
    private BufferedImage lastCapture;
    private TreeTextWidget promptsDCB;
    private final NVGenericMap deBooleanizerConfig = new NVGenericMap("APIConfig");


    private final JCheckBoxMenuItem enableLoggingItem = new JCheckBoxMenuItem("Enable Logs");

    private LedWidget captureLed;
    private final LedWidget audioLed = new LedWidget(30, 30, Color.BLACK)
            .mapStatus(AudioRecorder.Status.RECORDING, GUIUtil.BOOTSTARP_RED)
            .mapStatus(AudioRecorder.Status.STOP_RECORDING, GUIUtil.BOOTSTARP_GREEN)
            .mapStatus(AudioRecorder.Status.PROCESSING, GUIUtil.BOOTSTRAP_BLUE);
    private JButton audioButton;

    private final AIAPI aiApi = AIAPIBuilder.SINGLETON.createAPI("capture", "gpt capture api", null);
    private AudioRecorder audioRecorder;


    private final ReentrantLock lock = new ReentrantLock();

    private final OkHttpClient httpClient = OkHTTPCall.createOkHttpBuilder(null, null, 300, true, 10, 60).build();


    static private Rectangle selectedArea;
    private final RateCounter rc = new RateCounter("app");

    //private AtomicBoolean isRunning = new AtomicBoolean(false);

    // API keys (replace with your actual keys)
    private static String ocrApiKey = null; // Replace with your OCR.space API key
    //private static String openAIApiKey = null; // Replace with your OpenAI API key
    //private static String openAIApiURL = null;
    private static String aiModel = null;
    private Future<?> future = null;

    public ZeDebooleanizer() {
        setTitle("Ze-DeBooleanizer App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        try {
            audioRecorder = new AudioRecorder(AudioUtil.defaultAudioFormat(), true);
            TaskUtil.defaultTaskProcessor().execute(audioRecorder);
        } catch (Exception e) {
            e.printStackTrace();
            audioRecorder = null;
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

//        JMenuItem newItem = new JMenuItem("New");
//        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
//        newItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "New clicked"));
//
//        JMenuItem openItem = new JMenuItem("Open…");
//        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
//        openItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Open clicked"));
//
//        JMenuItem saveItem = new JMenuItem("Save");
//        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
//        saveItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Save clicked"));
//
//        fileMenu.add(newItem);
//        fileMenu.add(openItem);
//        fileMenu.add(saveItem);
        JMenuItem fileChooserItem = new JMenuItem("Logs Dir");
        fileChooserItem.addActionListener(e -> fileChooser.showOpenDialog(this));
        fileMenu.add(fileChooserItem);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.addActionListener(e -> {
            this.dispose();
            System.exit(0);
        });
        fileMenu.add(exitItem);


        JMenu configMenu = new JMenu("Config");
        JMenuItem apiKeyItem = new JMenuItem("AI API Config");
        apiKeyItem.addActionListener(e -> configSelection.showAIAPIConfig());
        configMenu.add(apiKeyItem);
//        JMenuItem apiURLItem = new JMenuItem("AI API URL");
//        apiURLItem.addActionListener(e->configSelection.showAIAPIURL());
//        configMenu.add(apiURLItem);
        configMenu.addSeparator();
//        ButtonGroup ocrGroup = new ButtonGroup();
//        JRadioButtonMenuItem noOCR = new JRadioButtonMenuItem("No OCR", true);
//        noOCR.addActionListener(e -> configSelection.setSelectionInfo(null));
//        JRadioButtonMenuItem localOCR = new JRadioButtonMenuItem("Local OCR");
//        localOCR.addActionListener(e -> configSelection.showLocalOCRDialog());
//        JRadioButtonMenuItem remoteOCR = new JRadioButtonMenuItem("Remote OCR");
//        remoteOCR.addActionListener(e -> configSelection.showRemoteOCRDialog());
//        ocrGroup.add(noOCR);
//        ocrGroup.add(localOCR);
//        ocrGroup.add(remoteOCR);
//        configMenu.add(noOCR);
//        configMenu.add(localOCR);
//        configMenu.add(remoteOCR);
//        configMenu.addSeparator();
//        JCheckBoxMenuItem enableLoggingItem = new JCheckBoxMenuItem("Enable Logs");
        configMenu.add(enableLoggingItem);

//        zoomGroup.add(zoom100);
//        zoomGroup.add(zoom125);
//        zoomGroup.add(zoom150);


//        // ===== Edit =====
//        JMenu editMenu = new JMenu("Edit");
//        editMenu.setMnemonic(KeyEvent.VK_E);
//
//        JMenuItem cutItem = new JMenuItem("Cut");
//        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask));
//        cutItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Cut"));
//
//        JMenuItem copyItem = new JMenuItem("Copy");
//        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
//        copyItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Copy"));
//
//        JMenuItem pasteItem = new JMenuItem("Paste");
//        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask));
//        pasteItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Paste"));
//
//        editMenu.add(cutItem);
//        editMenu.add(copyItem);
//        editMenu.add(pasteItem);

//        // ===== View =====
//        JMenu viewMenu = new JMenu("View");
//        viewMenu.setMnemonic(KeyEvent.VK_V);
//
//        JCheckBoxMenuItem statusBar = new JCheckBoxMenuItem("Show Status Bar", true);
//        statusBar.addActionListener(e ->
//                JOptionPane.showMessageDialog(owner, "Status Bar: " + (statusBar.isSelected() ? "Shown" : "Hidden")));
//        viewMenu.add(statusBar);
//
//        // Radio group for zoom preset
//        viewMenu.addSeparator();
//        ButtonGroup zoomGroup = new ButtonGroup();
//        JRadioButtonMenuItem zoom100 = new JRadioButtonMenuItem("Zoom 100%", true);
//        JRadioButtonMenuItem zoom125 = new JRadioButtonMenuItem("Zoom 125%");
//        JRadioButtonMenuItem zoom150 = new JRadioButtonMenuItem("Zoom 150%");
//        zoomGroup.add(zoom100);
//        zoomGroup.add(zoom125);
//        zoomGroup.add(zoom150);
//
//        zoom100.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Zoom set to 100%"));
//        zoom125.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Zoom set to 125%"));
//        zoom150.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Zoom set to 150%"));
//
//        viewMenu.add(zoom100);
//        viewMenu.add(zoom125);
//        viewMenu.add(zoom150);

        // ===== Help =====
        JMenu helpMenu = new JMenu("Help");
//        helpMenu.setMnemonic(KeyEvent.VK_H);

//        JMenuItem docsItem = new JMenuItem("Documentation");
//        docsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
//        docsItem.addActionListener(e -> JOptionPane.showMessageDialog(owner, "Open docs…"));

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this, "Ze-DeBooleanizer v 1.0.5"));

//        helpMenu.add(docsItem);
//        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        // Attach to bar
        menuBar.add(fileMenu);
        menuBar.add(configMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }


    private JPanel createWorkPanel() {
        // Create the panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());

        // Create GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();

        // Labels
//        JLabel resultLabel = new JLabel("Capture Result");
//        JLabel promptLabel = new JLabel("Audio Result");

        // TextAreas
        captureTextArea = GUIUtil.configureTextArea(new JTextArea(), null, null);
        audioTextArea = GUIUtil.configureTextArea(new JTextArea(), null, null);

//        filterPromptPanel = new FilterPromptPanel();
//        filterPromptPanel.setPromptInputText("Analyse the image and respond with solution only");

        // Make TextAreas wrap lines and scrollable
//        resultTextArea.setLineWrap(true);
//        resultTextArea.setWrapStyleWord(true);

//        promptTextArea.setLineWrap(true);
//        promptTextArea.setWrapStyleWord(true);

        JScrollPane captureScrollPane = GUIUtil.createScrollPane(captureTextArea, "Capture Result", null, new Dimension(250, 150));

        JScrollPane audioScrollPane = GUIUtil.createScrollPane(audioTextArea, "Audio Result", null, new Dimension(250, 150));

        // Insets for padding
        Insets padding = new Insets(5, 5, 5, 5);

        // Add labels to the first row
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = padding;
        gbc.weightx = 0.5; // Equal horizontal space for labels
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        //panel.add(resultLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        //panel.add(promptLabel, gbc);

        // Add TextAreas to the second row
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = padding;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0; // Allows vertical expansion
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(captureScrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(audioScrollPane, gbc);

        return panel;
    }

    private void initComponents() {
        // Panel for controls
        //JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));


        captureLed = new LedWidget(30, 30, Color.BLACK);
        captureLed.mapStatus(Const.Bool.ON, GUIUtil.BOOTSTARP_GREEN)
                .mapStatus(Const.Bool.OFF, GUIUtil.BOOTSTARP_RED);
        captureLed.setStatus(Const.Bool.ON);

        audioButton = new JButton("Audio");
        audioLed.setStatus(AudioRecorder.Status.STOP_RECORDING);
        uniqueCaptureCB = new JCheckBox("UniqueCapture", true);

        captureButton = new JButton("Capture");
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        clearPromptButton = new JButton("Clear Prompt");
        selectButton = new JButton("Select");
        stopRecording = new JButton("StopAudio");
        configSelection = new ConfigSelection(this, deBooleanizerConfig, gnvs -> {
            log.getLogger().info(""+gnvs);
            aiApi.setHTTPAuthorization(new HTTPAuthorization(HTTPAuthScheme.BEARER, gnvs.getValue("ai-api-key")));
            aiApi.updateURL(gnvs.getValue("ai-api-url"));
        });
        autoCopyToClipboardCB = new JCheckBox("AutoCopy");
        autoCopyToClipboardCB.setSelected(true);
//        configSelection.selectionBox.setName("CONF");

        refreshRateField = new JTextField("10s", 5); // Default refresh rate is 5 seconds
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //fileChooserButton = new JButton("Files");
//        captureModelName = new JTextField(10);
//        captureModelName.setText(openAIModel);
        aiModelDCB = new DynamicComboBox(true);
        if(SUS.isNotEmpty(aiModel))
            aiModelDCB.addItem(aiModel);
        recordingModelName = new JTextField(10);
        recordingModelName.setText(aiModel);
        promptsDCB = new TreeTextWidget("Prompts");
        responseFilterTA = GUIUtil.configureTextArea(new JTextArea(), null, null);


        JPanel capturePanel = new JPanel();
        capturePanel.setLayout(new BoxLayout(capturePanel, BoxLayout.Y_AXIS));


        capturePanel.add(GUIUtil.createPanel(null, new FlowLayout(FlowLayout.LEFT),
                captureButton,
                captureLed,
                autoCopyToClipboardCB,
                uniqueCaptureCB,
                new JLabel("Model"),
                aiModelDCB));
        capturePanel.add(promptsDCB);
        capturePanel.add(GUIUtil.createScrollPane(responseFilterTA, "Response-Filter", null, null));
        controlPanel.add(capturePanel);
        controlPanel.add(GUIUtil.createPanel("AudioCapture", new FlowLayout(FlowLayout.LEFT),
                audioButton,
                audioLed,
                new JLabel("Model"),
                recordingModelName,
                stopRecording));


        controlPanel.add(GUIUtil.createPanel("Control", new FlowLayout(FlowLayout.LEFT),
                clearPromptButton,
                startButton,
                stopButton,
                new JLabel("Refresh Rate (s):"),
                refreshRateField));


        // fileChooserButton.addActionListener(e -> fileChooser.showOpenDialog(this));
        stopRecording.addActionListener(e ->
        {
            audioRecorder.setStatus(AudioRecorder.Status.RESET, () ->
            {
                try {
                    processSpeechChatGPT();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });


        // Add panels to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(controlPanel, BorderLayout.NORTH);
        getContentPane().add(createWorkPanel(), BorderLayout.CENTER);

        // Button actions
        selectButton.addActionListener(e -> selectProcessing());
        startButton.addActionListener(e -> startProcessing());
        audioButton.addActionListener(e -> {
            try {
                if (audioRecorder.getStatus() == AudioRecorder.Status.RESET) {
                    audioRecorder.setStatus(AudioRecorder.Status.RECORDING);
                }
                speechToGPT();
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        });
        captureButton.addActionListener(e -> {
            try {
                TaskUtil.defaultTaskScheduler().queue(200, () -> {
                    try {
                        processCaptureChatGPT();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        stopButton.addActionListener(e -> stopProcessing());

        // Initially, stop button is disabled
        stopButton.setEnabled(false);

        aiApi.updateOkHttpClient(httpClient);

        // put it here
        setJMenuBar(createMenuBar());

    }

    @EndPointProp(methods = {HTTPMethod.GET}, name = "to-chat-gpt", uris = "/capture-to-gpt")
    public NVGenericMap captureToGPT() throws TesseractException, IOException, AWTException {

        if (!lock.isLocked()) {
            return processCaptureChatGPT();
        }

        return new NVGenericMap().build("system", "busy");
    }

    @EndPointProp(methods = {HTTPMethod.GET}, name = "speech-to-gpt", uris = "/speech-to-gpt")
    public NVGenericMap speechToGPT() throws IOException {
        if (log.isEnabled())
            log.getLogger().info("Speech api");

        if (lock.isLocked())
            return new NVGenericMap().build("Status", "processing");
        else
            TaskUtil.defaultTaskProcessor().execute(() -> {
                boolean locked = lock.tryLock();
                if (locked) {
                    try {
                        processSpeechChatGPT();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
            });

        return new NVGenericMap().build("Status", "processing");
    }


    private void processSpeechChatGPT() throws IOException {
        boolean locked = lock.tryLock();
        if (locked) {
            try {
//                TaskUtil.defaultTaskScheduler().queue(0,()->SwingUtilities.invokeLater(() -> recordingLed.setStatus(AudioRecorder.Status.PROCESSING)));
                SwingUtilities.invokeLater(() -> audioLed.setStatus(AudioRecorder.Status.PROCESSING));
                AudioRecorder.Status status = audioRecorder.getStatus();
                if (log.isEnabled()) log.getLogger().info("Audio Stat: " + status);
                switch (status) {
                    case RECORDING:
                        // we need to stop recording
                        //audioRecorder.setStatus(AudioRecorder.Status.STOP_RECORDING);
                        try {
                            InputStream recordedData = audioRecorder.getRecordedStream();
                            if (recordedData != null)
                                if (log.isEnabled())
                                    log.getLogger().info("is: " + Const.SizeInBytes.K.convertBytesDouble(recordedData.available()) + " " + Const.SizeInBytes.K.getName());
                            if (recordedData != null) {
                                // send to chatgpt transcribe


//                                NamedValue<InputStream> audioClip = new NamedValue<InputStream>();
//                                audioClip.setName("AudioClip.wav");
//
//                                if (log.isEnabled())
//                                    log.getLogger().info("length " + Const.SizeInBytes.toString(recordedData.available()));
//                                audioClip.setValue(recordedData);
//                                //gptAPI.setHTTPAuthorization(new HTTPAuthorization(HTTPAuthScheme.BEARER, gptSelection.getGPTAPIKey()));
//                                NVGenericMap response = gptAPI.syncCall(GTPAPIBuilder.Command.TRANSCRIBE, audioClip);
//                                String toDisplay = response.getValue("text");
                                String response = aiApi.transcribe(recordedData, "AudioClip.wav");
                                if (log.isEnabled()) log.getLogger().info("transcribe: " + response);
                                final String toDisplay = response;
                                SwingUtilities.invokeLater(() -> audioTextArea.setText(toDisplay));

                                String[] models = recordingModelName.getText().split(",");
                                for (int i = 0; i < models.length; i++) {
                                    if (SUS.isNotEmpty(models[i])) {
                                        response = aiApi.completion(models[i], response, 5000);
                                        final String text = response;
                                        SwingUtilities.invokeLater(() -> audioTextArea.setText(text));
                                    }

                                }
                            }
                        } finally {
                            SwingUtilities.invokeLater(() -> audioLed.setStatus(AudioRecorder.Status.RECORDING));
                        }

                        break;
                    case STOP_RECORDING:
                        audioRecorder.setStatus(AudioRecorder.Status.RECORDING);
                        SwingUtilities.invokeLater(() -> audioLed.setStatus(AudioRecorder.Status.RECORDING));
                        break;
                    case RESET:
                        audioRecorder.reset();
                        SwingUtilities.invokeLater(() -> audioLed.setStatus(AudioRecorder.Status.STOP_RECORDING));
                        audioRecorder.setStatus(AudioRecorder.Status.STOP_RECORDING);
                    case ERROR:
                        break;
                    case CLOSED:
                        break;
                    case PROCESSING:
                        break;
                }
            } finally {
                if (locked) {
                    lock.unlock();
                }
            }
        }

    }


    private NVGenericMap processCaptureChatGPT() throws AWTException, IOException, TesseractException {
        NVGenericMap request = null;
        NVGenericMap response = null;
        StringFilter sf = null;
        {
            try {
                if (SUS.isNotEmpty(responseFilterTA.getText())) {
                    NVGenericMap config = GSONUtil.fromJSONDefault(responseFilterTA.getText(), NVGenericMap.class);
                    sf = new StringFilter("toto", config);
                }


            } catch (Exception e) {
                e.printStackTrace();
                sf = null;
            }
        }
        try {
            lock.lock();
            SwingUtilities.invokeLater(() -> captureLed.setStatus(Const.Bool.OFF));
            captureButton.setEnabled(false);
            String prompt = null;
            // Capture the selected screen area
            BufferedImage image = GUIUtil.captureSelectedArea(selectedArea);


            rc.start();
            if (uniqueCaptureCB.isSelected() && GUIUtil.compareImages(image, lastCapture)) {
                lastCapture = image;
                rc.stop();
                if (log.isEnabled())
                    log.getLogger().info("Image compare equal, it took: " + Const.TimeInMillis.toString(rc.lastDeltaInMillis()));
                return null;
            } else {
                lastCapture = image;
            }
            File chosenDir = fileChooser.getSelectedFile();
            String baseFileName = null;

            if (chosenDir != null && enableLoggingItem.isSelected()) {
                if (chosenDir.isDirectory()) {
                    baseFileName = DateUtil.FILE_DATE_FORMAT.format(new Date());
                    File file = new File(chosenDir, baseFileName + ".png");
                    ImageIO.write(image, "png", file);
                    log.getLogger().info("ext: " + SharedStringUtil.valueAfterRightToken(file.getName(), ".") +
                            " filename: " + file);
                }
            }

            if (log.isEnabled())
                log.getLogger().info("New image to process, it took: " + Const.TimeInMillis.toString(rc.lastDeltaInMillis()));
//            String text = null;

            prompt  = promptsDCB.getContent();

//            NVGenericMap selectionInfo = configSelection.getSelectionInfo();
//            if (selectionInfo != null) {
//                switch (selectionInfo.getName()) {
//                    case "local-ocr":
//                        text = OCRUtil.SINGLETON.tesseractOCRImage(selectionInfo.getValue("path"), selectionInfo.getValue("language"), image);
//                        break;
//
//                    case "remote-ocr":
//                        // Perform OCR
//                        text = performOCRWithOCRSpace(selectionInfo.getValue("image-format"), image, selectionInfo.getValue("api-key"));
//                        break;
//                    case "ai-api-key":
//                        text = promptsDCB.getSelectedItem();//filterPromptPanel.getPromptInputText();
//                        break;
//                }
//                prompt = text;
////                String textToDisplay = prompt;
////                SwingUtilities.invokeLater(() -> filterPromptPanel.setPromptInputText(textToDisplay));
//            } else {
//                prompt = promptsDCB.getSelectedItem();
//            }


            if (SUS.isNotEmpty(prompt)) {
                UByteArrayOutputStream baos = new UByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                String[] models = aiModelDCB.getSelectedItem().split(",");

                Object content = null;
                for (int i = 0; i < models.length; i++) {
                    if (content == null)
                        request = AIAPIBuilder.SINGLETON.toVisionParams(models[i], prompt, 5000, baos, "png");
                    else
                        request = AIAPIBuilder.SINGLETON.toPromptParams(models[i], "" + content, 5000);


                    //response = GSONUtil.fromJSONDefault(rd.getDataAsString(), NVGenericMap.class);
                    aiApi.setHTTPAuthorization(new HTTPAuthorization(HTTPAuthScheme.BEARER, deBooleanizerConfig.getValue("ai-api-key")));
                    long ts = System.currentTimeMillis();
                    response = aiApi.syncCall(AIAPIBuilder.Command.COMPLETION, request);
                    ts = System.currentTimeMillis() - ts;
                    if (log.isEnabled()) log.getLogger().info("" + response);
                    NVGenericMapList choices = (NVGenericMapList) response.get("choices");


                    if (log.isEnabled()) log.getLogger().info("" + choices);
                    NVGenericMap firstChoice = choices.getValue().get(0);
                    if (log.isEnabled()) log.getLogger().info("" + firstChoice);
                    NVGenericMap message = (NVGenericMap) firstChoice.get("message");

                    content = message.getValue("content");
                    if (log.isEnabled()) log.getLogger().info("Content\n" + content);


                    SwingUtilities.invokeLater(() -> captureTextArea.setText("" + message.getValue("content")));
                    if(log.isEnabled()) log.getLogger().info("Last request took " + Const.TimeInMillis.toString(ts));
                }


                if (autoCopyToClipboardCB.isSelected()) {

                    String toClipboard = null;
                    if (sf != null && content instanceof String) {
                        toClipboard = sf.decode((String) content);
                    } else if (content instanceof String)
                        toClipboard = (String) content;

                    GUIUtil.copyToClipboard(SUS.isNotEmpty(toClipboard) ? toClipboard : "" + content);
                    if (baseFileName != null) {
                        File file = new File(chosenDir, baseFileName + "." + sf.getExtension());
                        try (OutputStream os = Files.newOutputStream(file.toPath())) {
                            os.write(toClipboard.getBytes());
                        }
                    }
                }


                //log.getLogger().info("api call duration " + Const.TimeInMillis.toString(rd.getDuration()));
            }
        } finally {
            SwingUtilities.invokeLater(() -> captureLed.setStatus(Const.Bool.ON));
            captureButton.setEnabled(true);
            lock.unlock();
        }


        return response;
    }


    private void startProcessing() {
        if (future != null)
            future.cancel(true);


        // Disable start button and enable stop button
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // Parse refresh rate
        int refreshRate;
        try {
            refreshRate = (int) Const.TimeInMillis.toMillis(refreshRateField.getText()); //// Convert to milliseconds
            if (log.isEnabled()) log.getLogger().info("refreshRate: " + refreshRate);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid refresh rate. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            stopProcessing();
            return;
        }

        future = TaskUtil.defaultTaskScheduler().scheduleAtFixedRate(() -> {
            try {
                processCaptureChatGPT();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, refreshRate, TimeUnit.MILLISECONDS);
    }

    private void stopProcessing() {
        // Enable start button and disable stop button
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        this.setEnabled(true);

        // Stop the timer
        if (future != null) {
            future.cancel(true);
            lastCapture = null;
        }
    }

    private void selectProcessing() {
        this.setVisible(false);
        try {
            selectedArea = GUIUtil.captureSelectedArea();
            if (log.isEnabled()) log.getLogger().info("SelectedArea: " + selectedArea);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.setVisible(true);
        }

    }


    // Function to perform OCR using OCR.space API (same as before)
    public static String performOCRWithOCRSpace(String imageFormat, BufferedImage image, String apiKey) {
        try {
            String urlStr = "https://api.ocr.space/parse/image";
            URL url = new URL(urlStr);

            // Prepare the image file
            UByteArrayOutputStream ubaos = new UByteArrayOutputStream();
            ImageIO.write(image, "png", ubaos);
            String imageName = "Capture." + imageFormat;

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
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"" + imageName + "\"\r\n");
            writer.write("Content-Type: image/" + imageFormat + "\r\n\r\n");
            writer.flush();
            outputStream.write(ubaos.getInternalBuffer(), 0, ubaos.size());
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


    // Main method
    public static void main(String... args) {

        try {
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);

            boolean selectArea = params.booleanValue("cap", true);
            ocrApiKey = params.stringValue("ocr-key", true);
            String aiAPIKey = params.stringValue("ai-api-key", true);
            String aiAPIURL = params.stringValue("ai-api-url", true);
            aiModel = params.stringValue("ai-model", true);
            String jsonFilterFile = params.stringValue("json-filter", true);
            String jsonAIConfigFile = params.stringValue("json-ai-config", true);
            String filterContent = null;
            if (jsonFilterFile != null) {
                try {
                    filterContent = IOUtil.inputStreamToString(IOUtil.locateFile(jsonFilterFile));
                    log.getLogger().info("Filter-Content\n" + filterContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            NVGenericMap prompts = null;
            NVStringList models = null;
            if (jsonAIConfigFile != null) {
                try {
                    String jsonPrompts = IOUtil.inputStreamToString(IOUtil.locateFile(jsonAIConfigFile));
                    NVGenericMap nvgmPrompts = GSONUtil.fromJSONDefault(jsonPrompts, NVGenericMap.class);
                    prompts = nvgmPrompts.getNV("prompts");
                    models = nvgmPrompts.getNV("models");

                    log.getLogger().info("Prompts-Content\n" + jsonPrompts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String webServerConfig = params.stringValue("web-config", true);


            if (selectArea) {
                try {
                    selectedArea = GUIUtil.captureSelectedArea();
                    if (log.isEnabled()) log.getLogger().info("SelectedArea: " + selectedArea);
                } catch (AWTException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


            ZeDebooleanizer app = null;
            if (webServerConfig != null) {
                try {

                    NIOHTTPServerCreator httpServerCreator = new NIOHTTPServerCreator();
                    File file = IOUtil.locateFile(webServerConfig);
                    HTTPServerConfig hsc = GSONUtil.fromJSON(IOUtil.inputStreamToString(file), HTTPServerConfig.class);
                    if (log.isEnabled()) log.getLogger().info("" + hsc);
                    if (log.isEnabled()) log.getLogger().info("" + hsc.getConnectionConfigs());
                    httpServerCreator.setAppConfig(hsc);
                    NIOHTTPServer ws = httpServerCreator.createApp();
                    app = ws.getEndPointsManager().lookupBean("chat-gpt");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            if (app == null)
                app = new ZeDebooleanizer();


            // for java lambda compliance
            ZeDebooleanizer appConst = app;
            String filterContentConst = filterContent;
            //-----------------------------------------
            NVGenericMap promptsConst = prompts;
            NVStringList modelsConst = models;
            SwingUtilities.invokeLater(() -> {
                appConst.initComponents();
                if (SUS.isNotEmpty(filterContentConst)) {
                    appConst.responseFilterTA.setText(filterContentConst);
                }
                appConst.setVisible(true);


                if (promptsConst != null) {
                    for (GetNameValue gnv: promptsConst.values()) {

                        appConst.promptsDCB.addEntry(null, gnv.getName(), (String)gnv.getValue());
                    }
                }

                if (modelsConst != null) {
                    for (String prompt : modelsConst.getValue()) {
                        appConst.aiModelDCB.addItem(prompt);
                    }
                }



                appConst.deBooleanizerConfig.build("ai-api-name", "openai");
                if(aiAPIURL != null)
                    appConst.deBooleanizerConfig.build("ai-api-url", aiAPIURL);
                else
                    appConst.deBooleanizerConfig.build("ai-api-url", appConst.aiApi.getEndPoints()[0].getConfig().getURL());
                appConst.deBooleanizerConfig.build("ai-api-key", aiAPIKey);


                //appConst.configSelection.setAIAPIKey(aiAPIKey);
                if (SUS.isEmpty(aiAPIKey))
                    appConst.configSelection.showAIAPIConfig();
            });


        } catch (Exception e) {
            e.printStackTrace();

            System.err.println("Usage: cap=[on/off] ai-api-key=[ai-api-key] api-url=[base-api-url] ai-model=[ai-model] json-ai-config=[data-filter-file]");
        }
    }
}
