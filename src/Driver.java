
import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import java.awt.Toolkit;
import java.net.URL;
import java.util.Date;

public class Driver extends JFrame {

    private static final long serialVersionUID = -8051939985045615968L;
    TrayIcon trayIcon;
    SystemTray tray;
    static JTextArea textArea;

    public static void addComment(String msg){
        if (null != textArea){
            String oldText = textArea.getText();
            textArea.setText(oldText+msg+"\n");
        }
    }

    private JScrollPane textarea(String content, int rows, int columns)
    {
        textArea = new JTextArea(rows, columns);
        Font font = new Font("Verdana UTF-8", Font.BOLD, 12);
        textArea.setFont(font);
        JScrollPane scrollPane = new JScrollPane(textArea);
        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        return scrollPane;
    }

    public Driver() {
        super("Happy Birthday Wisher");
        System.out.println("creating instance");
        try {
            System.out.println("setting look and feel");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Unable to set LookAndFeel");
        }
        textArea = new JTextArea("");
        textArea.setFont(new Font("Verdana", Font.PLAIN, 13));
        if (SystemTray.isSupported()) {
            System.out.println("system tray supported");
            tray = SystemTray.getSystemTray();

            // Image image=Toolkit.getDefaultToolkit().getImage("/gift.png");
            ActionListener exitListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Exiting....");
                    System.exit(0);
                }
            };
            PopupMenu popup = new PopupMenu();
            MenuItem defaultItem = new MenuItem("Exit");
            defaultItem.addActionListener(exitListener);
            popup.add(defaultItem);
            defaultItem = new MenuItem("Open");
            defaultItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                }
            });
            popup.add(defaultItem);
            trayIcon = new TrayIcon(createImage("/gift.png", "tray icon"), "Happy Birthday Wisher", popup);
            // trayIcon=new TrayIcon(image, "SystemTray Demo", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    System.out.println("pressing");
                    setVisible(true);
                    setExtendedState(JFrame.NORMAL);
                }
            });

        } else {
            System.out.println("system tray not supported");
        }
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == ICONIFIED) {
                    try {
                        tray.add(trayIcon);
                        setVisible(false);
                        System.out.println("added to SystemTray");
                    } catch (AWTException ex) {
                        System.out.println("unable to add to tray");
                    }
                }

                if (e.getNewState() == MAXIMIZED_BOTH) {
                    tray.remove(trayIcon);
                    setVisible(true);
                    System.out.println("Tray icon removed");
                }
                if (e.getNewState() == NORMAL) {
                    tray.remove(trayIcon);
                    setVisible(true);
                    System.out.println("Tray icon removed");
                }
            }
        });
        getContentPane().add(textarea("", 6,5));

        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/gift.png")));

        setVisible(true);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(ICONIFIED);

        HappyBirthdayWisher wisher = new HappyBirthdayWisher();
        wisher.process();

    }

    // Obtain the image URL
    protected static Image createImage(String path, String description) {
        URL imageURL = Driver.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

    public static void go(int hour) {
        boolean shouldRun = true;
        do {
            DateHandler time = new DateHandler();
            int currHour = time.getHour();
            if (currHour != hour) {
                try {
                    System.out.println("The hour is still not " + hour + ". written at: " + new Date());
                    Thread.sleep(60000 * 20);
                } catch (InterruptedException ex) {

                }
            } else {
                shouldRun = false;
            }
        } while (shouldRun);
        new Driver();
    }

    public static void main(String[] args) {

        PropertiesHandler propertiesHandler = PropertiesHandler.getInstance("common.properties");
        System.setProperty("webdriver.gecko.driver", propertiesHandler.readValue("webdriver.gecko.driver"));
        int hour = 7;
        if (args.length > 0) {
            try {
                hour = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.out.println("Did not pass a number as arg");
            }
        }
        go(hour);
    }
}