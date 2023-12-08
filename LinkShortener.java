package p;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LinkShortener {
    private static Map<String, String> shortToLongMap = new HashMap<>();
    private static Map<String, String> longToShortMap = new HashMap<>();
    private static int counter = 1;

    public static void main(String[] args) {
        loadLinkData();  // Load existing data from file

        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Link Shortener");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);
        placeComponents(panel);

        frame.setSize(400, 200);
        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel longURLLabel = new JLabel("Long URL:");
        longURLLabel.setBounds(10, 20, 80, 25);
        panel.add(longURLLabel);

        JTextField longURLText = new JTextField(20);
        longURLText.setBounds(100, 20, 250, 25);
        panel.add(longURLText);

        JButton shortenButton = new JButton("Shorten");
        shortenButton.setBounds(10, 50, 150, 25);
        panel.add(shortenButton);

        JButton expandButton = new JButton("Expand");
        expandButton.setBounds(200, 50, 150, 25);
        panel.add(expandButton);

        JTextArea resultArea = new JTextArea();
        resultArea.setBounds(10, 80, 350, 70);
        resultArea.setEditable(false);
        panel.add(resultArea);

        shortenButton.addActionListener(e -> {
            String longURL = longURLText.getText();
            if (!longURL.isEmpty()) {
                String shortURL = generateShortLink(longURL);
                shortToLongMap.put(shortURL, longURL);
                longToShortMap.put(longURL, shortURL);
                resultArea.setText("Shortened URL: " + shortURL);
                saveLinkData();  // Save updated data to file
            } else {
                resultArea.setText("Please enter a valid long URL.");
            }
        });

        expandButton.addActionListener(e -> {
            String shortURL = longURLText.getText();
            if (!shortURL.isEmpty() && shortToLongMap.containsKey(shortURL)) {
                String longURL = shortToLongMap.get(shortURL);
                resultArea.setText("Expanded URL: " + longURL);
            } else {
                resultArea.setText("Invalid short URL. Please enter a valid one.");
            }
        });
    }

    private static String generateShortLink(String longURL) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(longURL.getBytes());

            // Convert byte array to a hex string
            StringBuilder shortURL = new StringBuilder();
            for (byte b : messageDigest) {
                shortURL.append(String.format("%02x", b));
            }

            // Take the first 8 characters as the short URL
            return "short_" + shortURL.substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void loadLinkData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("link_data.ser"))) {
            shortToLongMap = (HashMap<String, String>) ois.readObject();
            for (Map.Entry<String, String> entry : shortToLongMap.entrySet()) {
                longToShortMap.put(entry.getValue(), entry.getKey());
            }
            counter = shortToLongMap.size() + 1;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
    }

    private static void saveLinkData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("link_data.ser"))) {
            oos.writeObject(shortToLongMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
