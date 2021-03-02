import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyAdapter;
import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

/*
* 02/03/2019
* Chat application on the Client side the ClientGUI class. The client runs this on a different host computer, port 12001 hard coded. GUI experience
* Authored by kagiso, Mathew & Paul
*/

public class ClientGUI extends Thread{
  final JTextPane jtextPaneChat = new JTextPane();
  final JTextPane jtextPaneUsers = new JTextPane();
  final public JTextField jtextFieldInput = new JTextField();
  private String previousMessage = "";
  private Thread userInput;
  private String serverName;
  private int port;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;
  TextListener textListener;

  /**
  * Constructor to generate a GUI for the Client-server application, pre-assigned text for the server name (IP address), port number and user name
  */
  public ClientGUI(){
      this.serverName = "Server IP address";
      this.port = 12001;
      this.name = "Guest user";
      String fontfamily = "Arial, sans-serif";
      Font font = new Font(fontfamily, Font.PLAIN, 15);
      final JFrame jFrame = new JFrame("Client-Server Chat application");
      jFrame.getContentPane().setLayout(null);
      jFrame.setSize(1000,800);
      jFrame.setResizable(false);            // Allow the JFrame to be resized by the user
      jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jtextPaneChat.setBounds(25,25,600,200);
      jtextPaneChat.setFont(font);
      jtextPaneChat.setMargin(new Insets(6, 6, 6, 6));
      jtextPaneChat.setContentType("text/html");
      jtextPaneChat.setEditable(false);
      JScrollPane jtextPaneChatScrollPane = new JScrollPane(jtextPaneChat);
      jtextPaneChatScrollPane.setBounds(25,25,600,200);
      jtextPaneUsers.setContentType("text/html");
      jtextPaneChat.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
      jtextPaneUsers.setBounds(25,250,600,100);
      jtextPaneUsers.setFont(font);
      jtextPaneUsers.setEditable(true);
      JScrollPane jtextPaneUsersScrollPane = new JScrollPane(jtextPaneUsers);
      jtextPaneUsersScrollPane.setBounds(25, 250, 600, 100);
      jtextFieldInput.setBounds(25,375,600,50);
      jtextFieldInput.setFont(font);
      final JScrollPane jtextFieldInputScrollPane = new JScrollPane(jtextFieldInput);
      jtextFieldInputScrollPane.setBounds(25,375,600,50);
      // New GUI panes, fields and buttons
      final JButton buttonSend = new JButton("Send");
      buttonSend.setBounds(25,450,100,35);
      buttonSend.setFont(font);

      final JButton buttonExit = new JButton("Leave chat room?");
      buttonExit.setBounds(25,500,100,35);
      buttonExit.setFont(font);
      // Add a listener to the input text field, whenever the "enter/return" key is pressed call the sendMessage method to send the message otherwise continue reading the text
      jtextFieldInput.addKeyListener(new KeyAdapter(){
        public void keyPressed(KeyEvent e){
          if(e.getKeyCode() == KeyEvent.VK_ENTER){
            sendMessage();
          }
          if(e.getKeyCode() == KeyEvent.VK_UP){
            String currentMessage = jtextFieldInput.getText().trim();
            jtextFieldInput.setText(previousMessage);
            previousMessage=currentMessage;
          }
          if(e.getKeyCode() == KeyEvent.VK_DOWN){
            String currentMessage = jtextFieldInput.getText().trim();
            jtextFieldInput.setText(previousMessage);
            previousMessage=currentMessage;
          }
        }
      });

      buttonSend.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent actionEvent){
            sendMessage();
          }
        });

        final JTextField textName = new JTextField(this.name);
        final JTextField textAddress = new JTextField(this.serverName);
        final JButton buttonConnect = new JButton("Connect");
        textName.getDocument().addDocumentListener(new TextListener(textName, textAddress, buttonConnect));
        textAddress.getDocument().addDocumentListener(new TextListener(textName, textAddress, buttonConnect));
        textName.setBounds(25,400,200,75);
        textAddress.setBounds(25,500,200,75);
        buttonConnect.setBounds(25,600,125,125);

        jFrame.add(buttonConnect);
        jFrame.add(jtextPaneChatScrollPane);
        jFrame.add(jtextPaneUsersScrollPane);
        jFrame.add(textName);
        jFrame.add(textAddress);
        jFrame.setVisible(true);

        // Information for the User
        appendToPane(jtextPaneChat, "Welcome to the Chat room hosted by Kagiso, Mathew and Paul");
        buttonConnect.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent actionEvent){
            try{
              name = textName.getText();
              int port = 12001;
              serverName = textAddress.getText();
              appendToPane(jtextPaneChat, "Connecting");
              server = new Socket(serverName, port);
              appendToPane(jtextPaneChat,"Connected to chat room at "+ server.getRemoteSocketAddress()+" from "+serverName+" from port "+ port);

              // Handle input from the user
              input = new BufferedReader(new InputStreamReader(server.getInputStream()));
              output = new PrintWriter(server.getOutputStream(),true);
              output.println(name);     //Send the server the users name
              
	      userInput = new HandleMessage();
              userInput.start();
              jFrame.remove(textName);
              jFrame.remove(textAddress);
              jFrame.remove(buttonConnect);
              jFrame.add(buttonSend);
              jFrame.add(jtextFieldInputScrollPane);
              jFrame.add(buttonExit);
              jFrame.revalidate();
              jFrame.repaint();
              jtextPaneChat.setBackground(Color.WHITE);
              jtextPaneUsers.setBackground(Color.WHITE);
            } catch(Exception e){
              e.printStackTrace();
              appendToPane(jtextPaneChat,"Can't reach server");
              JOptionPane.showMessageDialog(jFrame, e.getMessage());
            }// Give feedback to the user as to the nature of the exception/error
          }
        });

        // When the user disconnects
        buttonExit.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent actionEvent){
            jFrame.add(textName);
            jFrame.add(textAddress);
            jFrame.add(buttonConnect);
            jFrame.remove(buttonSend);
            jFrame.remove(jtextFieldInputScrollPane);
            jFrame.remove(buttonExit);
            jFrame.revalidate();
            jFrame.repaint();
            userInput.interrupt();      //Stop the thread from reading the wrong Messages
            jtextPaneUsers.setText(null);   // Do not show users until Connected
            appendToPane(jtextPaneChat,"Chat ended, start a new chat?");
            output.close();
          }
        });

  }
// New class of TextListener for the textboxes
	public class TextListener implements DocumentListener{
  JTextField jTextField1;
  JTextField jTextField2;
  JButton jButton;

	public TextListener(JTextField text1, JTextField text2, JButton button){
    this.jTextField1 = text1;
    this.jTextField2 = text2;
    this.jButton=button;
  }

  public void changedUpdate(DocumentEvent e){}

    /**
    * When the user disconnects the texts are removed to clear from the users server activity
    */
  public void removeUpdate(DocumentEvent e){
    if(jTextField1.getText().trim().equals("")||jTextField2.getText().trim().equals("")){
      jButton.setEnabled(false);
    }else{
      jButton.setEnabled(true);
    }
  }

  /**
  * insertUpdate: Determines if the textfields for the IP Address and username have been read or not
  */
  public void insertUpdate(DocumentEvent e){
    if(jTextField1.getText().trim().equals("") || jTextField2.getText().trim().equals("")){
      jButton.setEnabled(false);
    }else{
      jButton.setEnabled(true);
    }
  }

  }// Close TextListener

/**
    * sendMessage users the object in the ClientGUI to read from the jtextFieldInput so that the message can be sent ot the server
    */
  public void sendMessage() {
    try {
      String message = jtextFieldInput.getText().trim();
      if (message.equals("")) {
        return;
      }
      previousMessage = message;
      output.println(message);
      jtextFieldInput.requestFocus();
      jtextFieldInput.setText(null);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, e.getMessage());
      System.exit(0);
    }
  
}

 public static void main(String[] args) throws Exception{
    ClientGUI client = new ClientGUI();
  }



class HandleMessage extends Thread {

    /**
    * This thread extracts the input information of client side messages and send them to the server whether they are direct or broadcast
    */
   public void run() {
     String message;
     while(!Thread.currentThread().isInterrupted()){
       try {
         message = input.readLine();
         if(message != null){
           if (message.charAt(0) == '[') {
             message = message.substring(1, message.length()-1);
             ArrayList<String> ListUser = new ArrayList<String>(Arrays.asList(message.split(", ")));
             jtextPaneUsers.setText(null);
             for (String user : ListUser) {
               appendToPane(jtextPaneUsers, "@" + user);
             }//close for
		
           }else{
             appendToPane(jtextPaneChat, message);
           }//close else
         }//close while 
       }catch (IOException e) {
         e.printStackTrace();
         System.err.println("Unable to read/write message");
       }// close catch
     }//close run
}// close UserInput
}// close if

	
private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}
