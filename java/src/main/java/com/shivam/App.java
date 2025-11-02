package com.shivam;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 * OCR application that extracts text from screen when 's' key is pressed
 */
public class App implements NativeKeyListener 
{
    private static Robot robot;
    private static ITesseract tesseract;
    private static final String TESSDATA_DIR = "tessdata";
    private static final String GEMINI_API_KEY = "AIzaSyDFEXPTZmfvMuLqnTuUHulKTW2H4v-WOlk";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static File extractedTessdataDir;
    private static File extractedLibraryDir;
    private static JFrame answerWindow;
    private static JLabel answerLabel;
    private static Timer hideTimer;
    
    public static void main( String[] args )
    {
        try {
            // Set library path for Tesseract native library
            setupLibraryPath();
            
            // Initialize Robot for screenshot
            robot = new Robot();
            
            // Setup Tesseract OCR with bundled tessdata
            setupTesseract();
            
            // Create answer display window
            createAnswerWindow();
            
            // Register global keyboard hook
            GlobalScreen.registerNativeHook();
            
            // Add key listener
            GlobalScreen.addNativeKeyListener(new App());
            
            System.out.println("OCR application started. Press 'S' key to capture screen and extract text.");
            System.out.println("Press Ctrl+C to exit.");
            
            // Keep the program running
            Thread.currentThread().join();
            
            
        } catch (AWTException e) {
            System.err.println("Error initializing Robot: " + e.getMessage());
            e.printStackTrace();
        } catch (NativeHookException e) {
            System.err.println("Error registering native hook: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Application interrupted.");
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Setup library path for native Tesseract library
     * Extracts native library from resources if needed
     * Optimized for Windows (primary) and macOS
     */
    private static void setupLibraryPath() throws IOException {
        String pathSeparator = System.getProperty("path.separator");
        
        // Try to extract native library from resources first
        try {
            extractedLibraryDir = extractNativeLibraryFromResources();
            if (extractedLibraryDir != null && extractedLibraryDir.exists()) {
                String libPath = extractedLibraryDir.getAbsolutePath();
                String existingLibraryPath = System.getProperty("jna.library.path");
                if (existingLibraryPath != null && !existingLibraryPath.isEmpty()) {
                    System.setProperty("jna.library.path", existingLibraryPath + pathSeparator + libPath);
                } else {
                    System.setProperty("jna.library.path", libPath);
                }
                System.out.println("Using native library from resources: " + libPath);
                return;
            }
        } catch (Exception e) {
            System.out.println("Could not extract native library from resources: " + e.getMessage());
        }
        
        // Fallback: Try to find library in system locations (for development)
        String osName = System.getProperty("os.name").toLowerCase();
        String[] systemPaths;
        
        if (osName.contains("win")) {
            // Windows paths
            systemPaths = new String[]{
                "C:\\Program Files\\Tesseract-OCR\\lib",           // Windows default installation
                "C:\\Program Files (x86)\\Tesseract-OCR\\lib",     // Windows 32-bit
                System.getenv("ProgramFiles") + "\\Tesseract-OCR\\lib", // Environment variable
                System.getProperty("user.home") + "\\AppData\\Local\\Tesseract-OCR\\lib" // User installation
            };
        } else if (osName.contains("mac")) {
            // macOS paths
            systemPaths = new String[]{
                "/opt/homebrew/lib",    // macOS Homebrew (Apple Silicon)
                "/usr/local/lib",       // macOS Homebrew (Intel)
                "/usr/lib"              // System library location
            };
        } else {
            // Linux paths
            systemPaths = new String[]{
                "/usr/lib",
                "/usr/local/lib",
                "/usr/lib64"
            };
        }
        
        String libExtension = osName.contains("win") ? ".dll" : (osName.contains("mac") ? ".dylib" : ".so");
        String libName = osName.contains("win") ? "tesseract" : "libtesseract";
        
        for (String path : systemPaths) {
            if (path != null && !path.isEmpty()) {
                File libFile = new File(path, libName + libExtension);
                if (libFile.exists()) {
                    String existingLibraryPath = System.getProperty("jna.library.path");
                    if (existingLibraryPath != null && !existingLibraryPath.isEmpty()) {
                        System.setProperty("jna.library.path", existingLibraryPath + pathSeparator + path);
                    } else {
                        System.setProperty("jna.library.path", path);
                    }
                    System.out.println("Using system native library: " + path);
                    return;
                }
            }
        }
        
        System.out.println("Warning: Native Tesseract library not found. OCR may not work.");
    }
    
    /**
     * Extract native Tesseract library from resources to a temporary directory
     */
    private static File extractNativeLibraryFromResources() throws IOException {
        // Detect OS and architecture
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        String libName;
        if (osName.contains("mac")) {
            if (osArch.contains("aarch64") || osArch.contains("arm64")) {
                libName = "libtesseract.dylib";  // macOS ARM64
            } else {
                libName = "libtesseract.dylib";   // macOS Intel
            }
        } else if (osName.contains("linux")) {
            libName = "libtesseract.so";
        } else if (osName.contains("win")) {
            libName = "tesseract.dll";
        } else {
            libName = "libtesseract.dylib";  // Default fallback
        }
        
        // Try to load from resources
        String resourcePath = "/" + libName;
        InputStream inputStream = App.class.getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            // Try without leading slash
            resourcePath = libName;
            inputStream = App.class.getClassLoader().getResourceAsStream(resourcePath);
        }
        
        if (inputStream != null) {
            // Create temporary directory for native library
            Path tempDir = Files.createTempDirectory("tesseract-lib");
            File libDir = tempDir.toFile();
            libDir.deleteOnExit();
            
            File outputFile = new File(libDir, libName);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            inputStream.close();
            
            // On macOS/Linux, make sure the library is executable (Windows doesn't need this)
            if (!osName.contains("win")) {
                outputFile.setExecutable(true);
            }
            
            System.out.println("Extracted native library to: " + outputFile.getAbsolutePath());
            return libDir;
        } else {
            System.out.println("Native library not found in resources: " + libName);
            return null;
        }
    }
    
    /**
     * Setup Tesseract OCR with bundled tessdata from resources
     * This method extracts tessdata from resources if needed and configures Tesseract
     */
    private static void setupTesseract() throws IOException {
        // Initialize Tesseract
        tesseract = new Tesseract();
        
        // Try to find tessdata in different locations
        File tessdataDir = null;
        
        // 1. Check for tessdata in project root (for development)
        File rootTessdata = new File(TESSDATA_DIR);
        if (rootTessdata.exists() && rootTessdata.isDirectory()) {
            tessdataDir = rootTessdata;
            System.out.println("Using tessdata from project root: " + TESSDATA_DIR);
        } else {
            // 2. Extract tessdata from resources to a temporary location
            try {
                extractedTessdataDir = extractTessdataFromResources();
                if (extractedTessdataDir != null && extractedTessdataDir.exists()) {
                    tessdataDir = extractedTessdataDir;
                    System.out.println("Using extracted tessdata from resources: " + extractedTessdataDir.getAbsolutePath());
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not extract tessdata from resources: " + e.getMessage());
            }
        }
        
        // 3. Fallback to system tessdata (only for development/testing, not for standalone)
        if (tessdataDir == null || !tessdataDir.exists()) {
            String osName = System.getProperty("os.name").toLowerCase();
            String[] systemPaths;
            
            if (osName.contains("win")) {
                // Windows paths
                systemPaths = new String[]{
                    "C:\\Program Files\\Tesseract-OCR\\tessdata",
                    "C:\\Program Files (x86)\\Tesseract-OCR\\tessdata",
                    System.getenv("ProgramFiles") + "\\Tesseract-OCR\\tessdata",
                    System.getProperty("user.home") + "\\AppData\\Local\\Tesseract-OCR\\tessdata"
                };
            } else if (osName.contains("mac")) {
                // macOS paths
                systemPaths = new String[]{
                    "/opt/homebrew/share/tessdata",
                    "/usr/local/share/tessdata",
                    "/usr/share/tessdata"
                };
            } else {
                // Linux paths
                systemPaths = new String[]{
                    "/usr/share/tesseract-ocr/5/tessdata",
                    "/usr/share/tesseract-ocr/4.00/tessdata",
                    "/usr/share/tessdata"
                };
            }
            
            for (String path : systemPaths) {
                if (path != null && !path.isEmpty()) {
                    File systemTessdata = new File(path);
                    if (systemTessdata.exists() && systemTessdata.isDirectory()) {
                        tessdataDir = systemTessdata;
                        System.out.println("Using system tessdata: " + path);
                        break;
                    }
                }
            }
        }
        
        if (tessdataDir != null && tessdataDir.exists()) {
            tesseract.setDatapath(tessdataDir.getAbsolutePath());
        } else {
            System.out.println("Warning: Using default tessdata location. OCR may not work properly.");
        }
        
        // Set language
        tesseract.setLanguage("eng");
        
        // Configure OCR settings for better accuracy
        tesseract.setPageSegMode(1); // Automatic page segmentation
        tesseract.setOcrEngineMode(1); // LSTM OCR Engine
    }
    
    /**
     * Extract tessdata files from resources to a temporary directory
     */
    private static File extractTessdataFromResources() throws IOException {
        // Create a temporary directory for tessdata
        Path tempDir = Files.createTempDirectory("tessdata");
        File tessdataDir = tempDir.toFile();
        tessdataDir.deleteOnExit(); // Clean up on JVM exit
        
        // Extract eng.traineddata from resources
        String resourcePath = "/tessdata/eng.traineddata";
        InputStream inputStream = App.class.getResourceAsStream(resourcePath);
        
        if (inputStream == null) {
            System.out.println("tessdata not found in resources. Checking classpath...");
            // Try alternative path
            resourcePath = "tessdata/eng.traineddata";
            inputStream = App.class.getClassLoader().getResourceAsStream(resourcePath);
        }
        
        if (inputStream != null) {
            File outputFile = new File(tessdataDir, "eng.traineddata");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            inputStream.close();
            System.out.println("Extracted tessdata to: " + tessdataDir.getAbsolutePath());
            return tessdataDir;
        } else {
            System.out.println("tessdata resource not found in classpath");
            return null;
        }
    }
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Check if 's' key is pressed (case insensitive)
        if (e.getKeyCode() == NativeKeyEvent.VC_S) {
            takeScreenshot();
        }
    }
    
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Not needed for this implementation
    }
    
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Not needed for this implementation
    }
    
    private static void takeScreenshot() {
        try {
            // Get screen bounds (handles multi-monitor setups on Windows and macOS)
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screenRect = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
            
            // For Windows, capture the entire virtual screen if multi-monitor
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                // Windows: Get all screen devices bounds
                GraphicsDevice[] screens = ge.getScreenDevices();
                if (screens.length > 1) {
                    // Multi-monitor: calculate union of all screens
                    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                    int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
                    for (GraphicsDevice screen : screens) {
                        Rectangle bounds = screen.getDefaultConfiguration().getBounds();
                        minX = Math.min(minX, bounds.x);
                        minY = Math.min(minY, bounds.y);
                        maxX = Math.max(maxX, bounds.x + bounds.width);
                        maxY = Math.max(maxY, bounds.y + bounds.height);
                    }
                    screenRect = new Rectangle(minX, minY, maxX - minX, maxY - minY);
                }
            }
            
            // Capture screenshot
            BufferedImage screenshot = robot.createScreenCapture(screenRect);
            
            // Extract text and send to Gemini API
            extractAndGetAnswer(screenshot);
            
        } catch (Exception e) {
            System.err.println("Error capturing screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void extractAndGetAnswer(BufferedImage image) {
        try {
            // Perform OCR on the image
            String extractedText = tesseract.doOCR(image);
            
            // Send to Gemini API and get answer
            String answer = callGeminiAPI(extractedText);
            
            // Display answer in the window
            displayAnswer(answer);
            
        } catch (TesseractException e) {
            System.err.println("Error performing OCR: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error getting answer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a small window at bottom left corner to display the answer
     */
    private static void createAnswerWindow() {
        SwingUtilities.invokeLater(() -> {
            answerWindow = new JFrame();
            answerWindow.setAlwaysOnTop(true);
            answerWindow.setUndecorated(true);
            answerWindow.setBackground(new Color(0, 0, 0, 200)); // Semi-transparent black
            // Use POPUP type for maximum z-index (always on top)
            answerWindow.setType(Window.Type.POPUP);
            
            // Enable transparency (macOS and Windows)
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac")) {
                answerWindow.getRootPane().putClientProperty("Window.alpha", 0.85f);
            } else if (osName.contains("win")) {
                // Windows 10+ supports per-pixel alpha
                answerWindow.setBackground(new Color(0, 0, 0, 0));
                answerWindow.setOpacity(0.85f);
            }
            
            // Create panel with semi-transparent background
            JPanel panel = new JPanel();
            panel.setBackground(new Color(0, 0, 0, 200));
            panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            
            // Create label for answer - super tiny size
            answerLabel = new JLabel("?", SwingConstants.CENTER);
            answerLabel.setFont(new Font("Arial", Font.BOLD, 10));
            answerLabel.setForeground(Color.WHITE);
            answerLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            panel.add(answerLabel);
            answerWindow.add(panel);
            answerWindow.setSize(22, 22); // Super tiny window (5x5 would be invisible, 50x50 is minimal visible)
            answerWindow.setResizable(false);
            
            // Position at bottom left corner (Windows taskbar aware)
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
            
            int x = 20; // Left margin
            int y;
            
            // Position higher up from bottom (about 150px up for better visibility)
            int offsetFromBottom = 40; // Move window up by this amount
            
            // Windows taskbar is typically at bottom, adjust if needed
            if (osName.contains("win")) {
                // Account for Windows taskbar (usually 40-48px height) + move up
                y = screenBounds.height - answerWindow.getHeight() - 50 - offsetFromBottom;
            } else {
                // Standard bottom margin + move up
                y = screenBounds.height - answerWindow.getHeight() - 20 - offsetFromBottom;
            }
            
            answerWindow.setLocation(x, y);
            
            // Hide window initially - will show when answer comes
            answerWindow.setVisible(false);
        });
    }
    
    /**
     * Update the answer window with the new answer
     * Shows window for 3 seconds then hides it
     */
    private static void displayAnswer(String answer) {
        SwingUtilities.invokeLater(() -> {
            if (answerWindow != null && answerLabel != null) {
                // Cancel any existing hide timer
                if (hideTimer != null && hideTimer.isRunning()) {
                    hideTimer.stop();
                }
                
                // Update answer text
                answerLabel.setText(answer.toUpperCase());
                answerLabel.setForeground(Color.GREEN);
                
                // Show the window with maximum z-index
                answerWindow.setVisible(true);
                answerWindow.toFront();
                answerWindow.requestFocus();
                answerWindow.setAlwaysOnTop(true); // Ensure always on top
                
                // Force to front on Windows
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    answerWindow.setExtendedState(JFrame.NORMAL);
                    answerWindow.setState(JFrame.NORMAL);
                }
                
                // Change to white after a brief moment
                Timer colorTimer = new Timer(500, e -> {
                    answerLabel.setForeground(Color.WHITE);
                });
                colorTimer.setRepeats(false);
                colorTimer.start();
                
                // Hide window after 3 seconds
                hideTimer = new Timer(4000, e -> {
                    if (answerWindow != null) {
                        answerWindow.setVisible(false);
                    }
                });
                hideTimer.setRepeats(false);
                hideTimer.start();
            }
        });
    }
    
    /**
     * Call Gemini API 2.5 Flash to get MCQ answer
     */
    private static String callGeminiAPI(String questionText) throws IOException {
        // Create clear, specific prompt
        String prompt = "You are solving a multiple choice question (MCQ). Analyze the question and select the CORRECT answer.\n\n" +
                        "Instructions:\n" +
                        "1. Read the question carefully\n" +
                        "2. Solve the problem\n" +
                        "3. Find which option (a, b, c, d, or e) is the CORRECT answer\n" +
                        "4. Reply with ONLY a single lowercase letter: a, b, c, d, or e\n" +
                        "5. Do NOT write any explanation, reasoning, or other text\n" +
                        "6. Reply with ONLY the letter corresponding to the correct answer\n\n" +
                        "Question:\n" + questionText + "\n\n" +
                        "Answer (single letter only):";
        
        // Create JSON request body
        String jsonBody = String.format(
            "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
            escapeJson(prompt)
        );
        
        // Create URL (without API key in query string)
        URL url;
        try {
            url = new URI(GEMINI_API_URL).toURL();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + GEMINI_API_URL, e);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // Set request method and headers (API key as header, not query param)
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-goog-api-key", GEMINI_API_KEY);
            conn.setDoOutput(true);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    
                    // Parse response to extract answer
                    String rawResponse = response.toString();
                    System.out.println("Raw Gemini response: " + rawResponse); // Debug
                    return parseGeminiResponse(rawResponse);
                }
            } else {
                // Read error response
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    throw new IOException("API request failed with code " + responseCode + ": " + errorResponse.toString());
                }
            }
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Escape JSON special characters
     */
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Parse Gemini API response to extract the answer (single character a-e)
     */
    private static String parseGeminiResponse(String jsonResponse) {
        try {
            System.out.println("Full JSON response: " + jsonResponse); // Debug
            
            String textContent = null;
            
            // Method 1: Try to find "text" field more robustly
            // Look for "text":" pattern and extract until closing quote (handling escaped quotes)
            int textStart = jsonResponse.indexOf("\"text\":\"");
            if (textStart != -1) {
                textStart += 8; // Length of "\"text\":\""
                
                // Find the end of the string, accounting for escaped quotes
                StringBuilder extracted = new StringBuilder();
                for (int i = textStart; i < jsonResponse.length(); i++) {
                    char c = jsonResponse.charAt(i);
                    if (c == '\\' && i + 1 < jsonResponse.length()) {
                        // Handle escape sequences
                        char next = jsonResponse.charAt(i + 1);
                        if (next == '"') {
                            extracted.append('"');
                            i++; // Skip next character
                        } else if (next == '\\') {
                            extracted.append('\\');
                            i++; // Skip next character
                        } else if (next == 'n') {
                            extracted.append('\n');
                            i++; // Skip next character
                        } else {
                            extracted.append(c);
                        }
                    } else if (c == '"') {
                        // End of string found
                        break;
                    } else {
                        extracted.append(c);
                    }
                }
                textContent = extracted.toString();
            }
            
            // Method 2: Try alternative JSON structure
            if (textContent == null || textContent.isEmpty()) {
                // Try finding text after "text"
                int idx = jsonResponse.indexOf("\"text\"");
                if (idx != -1) {
                    int colonIdx = jsonResponse.indexOf(":", idx);
                    if (colonIdx != -1) {
                        int quoteStart = jsonResponse.indexOf("\"", colonIdx);
                        if (quoteStart != -1) {
                            // Extract text until closing quote (simple version)
                            int quoteEnd = jsonResponse.indexOf("\"", quoteStart + 1);
                            if (quoteEnd != -1) {
                                textContent = jsonResponse.substring(quoteStart + 1, quoteEnd);
                            }
                        }
                    }
                }
            }
            
            if (textContent == null || textContent.isEmpty()) {
                System.err.println("No text content found in response");
                return "?";
            }
            
            System.out.println("Extracted text content: '" + textContent + "'"); // Debug
            
            // Clean and extract answer
            textContent = textContent.trim().toLowerCase();
            
            // Remove common prefixes/suffixes that Gemini might add
            textContent = textContent.replaceAll("^answer[:\\.\\s]*", "")
                                     .replaceAll("^the answer is[:\\.\\s]*", "")
                                     .replaceAll("^option[:\\.\\s]*", "")
                                     .replaceAll("^correct answer[:\\.\\s]*", "")
                                     .replaceAll("[^a-e]", "") // Remove everything except a-e
                                     .trim();
            
            System.out.println("Cleaned text: '" + textContent + "'"); // Debug
            
            // Find the first valid answer option
            if (!textContent.isEmpty()) {
                char firstChar = textContent.charAt(0);
                if (firstChar >= 'a' && firstChar <= 'e') {
                    System.out.println("Answer found: " + firstChar); // Debug
                    return String.valueOf(firstChar);
                }
            }
            
            // Fallback: search for any a-e in original text (before removing non-letters)
            String originalText = textContent;
            textContent = jsonResponse.toLowerCase();
            for (int i = 0; i < textContent.length(); i++) {
                char ch = textContent.charAt(i);
                if (ch >= 'a' && ch <= 'e') {
                    // Check if it's likely a standalone answer
                    boolean isStandalone = false;
                    // Check context around the letter
                    if ((i == 0 || !Character.isLetterOrDigit(textContent.charAt(i - 1))) &&
                        (i == textContent.length() - 1 || !Character.isLetterOrDigit(textContent.charAt(i + 1)))) {
                        isStandalone = true;
                    }
                    // Also check if it appears right after common patterns
                    String before = i > 10 ? textContent.substring(Math.max(0, i - 10), i) : textContent.substring(0, i);
                    if (before.contains("answer") || before.contains("option") || before.contains("\"text\":\"")) {
                        isStandalone = true;
                    }
                    
                    if (isStandalone) {
                        System.out.println("Found standalone answer: " + ch); // Debug
                        return String.valueOf(ch);
                    }
                }
            }
            
            System.err.println("Could not find valid answer (a-e) in response");
            System.err.println("Original cleaned text: '" + originalText + "'");
            return "?";
        } catch (Exception e) {
            System.err.println("Error parsing response: " + e.getMessage());
            e.printStackTrace();
            return "?";
        }
    }
}
