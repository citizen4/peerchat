package gui;

import main.Main;
import net.ChatManager;
import net.Message;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Student on 30.10.2014.
 */
public class MainView extends JFrame implements ChatManager.Listener
{
   private static final Color[] PEER_COLORS = {Color.YELLOW,
           Color.CYAN,
           Color.GREEN,
           Color.MAGENTA,
           Color.LIGHT_GRAY,
           Color.ORANGE,
           Color.PINK,
           Color.RED
   };

   private ChatManager chatManager = null;
   private Map<String,Color> peerColorMap;
   private int peerCounter = 0;
   private boolean isLocal;
   private JTextPane msgBox;
   private JTextField msgField;
   private JTextField addrField;
   private JTextField fromField;

   public MainView()
   {
      super("PeerChat v0.31"/* + (isLocal ? " (bind addr: " + Main.localBindAddress + ")" : "")*/);

      chatManager = new ChatManager(this);
      peerColorMap = new HashMap<>();

      setSize(400,300);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      setupCtrls();

      setVisible(true);

      askIsLocal();
      addrField.setText(isLocal ? "127.0.0." : "10.101.102.");

      chatManager.startReceiverThread();
   }

   private void setupCtrls()
   {
      JPanel addrFromPanel = new JPanel();
      JPanel msgSendPanel = new JPanel();

      msgBox = new JTextPane();
      msgField = new JTextField(20);
      addrField = new JTextField(10);
      fromField = new JTextField(10);

      msgBox.setBackground(Color.BLACK);
      msgField.addKeyListener(new GuiActionListener());

      JButton sendButton = new JButton("Send");
      sendButton.setActionCommand("send_cmd");
      sendButton.addActionListener(new GuiActionListener());

      addrFromPanel.add(new JLabel("To(IP):"));
      addrFromPanel.add(addrField);
      addrFromPanel.add(new JLabel("From:"));
      addrFromPanel.add(fromField);

      msgSendPanel.add(new JLabel("Msg: "));
      msgSendPanel.add(msgField);
      msgSendPanel.add(sendButton);

      add(addrFromPanel,BorderLayout.NORTH);
      add(new JScrollPane(msgBox),BorderLayout.CENTER);
      add(msgSendPanel, BorderLayout.SOUTH);

      addWindowListener(new WindowAdapter()
      {
         @Override
         public void windowClosing(WindowEvent e)
         {
            chatManager.stopReceiverThread();
            dispose();
            System.exit(0);
         }
      });

   }

   @Override
   public void onNewMessage(final Message newMsg,final String peerId)
   {
      if(!peerColorMap.containsKey(peerId)){
         peerColorMap.put(peerId, PEER_COLORS[peerCounter++ % PEER_COLORS.length]);
      }

      String displayMsg = "<"+newMsg.FROM+">: "+newMsg.TEXT;
      Color displayColor = (newMsg.ACK) ? Color.WHITE : peerColorMap.get(peerId);

      if (!isLocal && !newMsg.ACK) {
         newMsg.ACK = true;
         chatManager.sendMessage(newMsg, peerId.split(":")[0]);
      }

      appendMsg(displayMsg + "\n", displayColor);
   }

   @Override
   public void onError(final String errMsg)
   {
      //TODO: show some useful information
   }


   private void appendMsg(String msg, Color color)
   {
        StyleContext styleCtx = StyleContext.getDefaultStyleContext();
        AttributeSet attribSet = styleCtx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

      attribSet = styleCtx.addAttribute(attribSet, StyleConstants.FontFamily, "Lucida Console");
      attribSet = styleCtx.addAttribute(attribSet, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

      int len = msgBox.getDocument().getLength();

      msgBox.setCaretPosition(len);
      msgBox.setCharacterAttributes(attribSet, false);
      msgBox.replaceSelection(msg);
   }

   private void askIsLocal()
   {
      String lastOctet = JOptionPane.showInputDialog(null, "Choose unique client number:",
              "Start as local client?", JOptionPane.QUESTION_MESSAGE);

      if (lastOctet != null) {
         Main.localBindAddress = "127.0.0." + lastOctet;
         System.out.println(Main.localBindAddress);
         setTitle(getTitle() + " [" + Main.localBindAddress + "]");
         isLocal = true;
      }
   }


   private class GuiActionListener extends KeyAdapter implements ActionListener
   {

      @Override
      public void actionPerformed(ActionEvent e)
      {
         sendChatMsg();
      }

      @Override
      public void keyTyped(KeyEvent e)
      {
         if (e.getKeyChar() == '\n') {
            sendChatMsg();
         }
      }

      private void sendChatMsg()
      {
         Message msg = new Message(false, fromField.getText(), msgField.getText());

         if (isLocal) {
            appendMsg("<" + fromField.getText() + ">: " + msgField.getText() + "\n", Color.WHITE);
         }

         msgField.setText("");
         chatManager.sendMessage(msg, addrField.getText());
      }
   }
}
