package gui;

import main.Main;
import net.ChatManager;
import net.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * Created by Student on 30.10.2014.
 */
public class MainView extends JFrame implements ChatManager.Listener
{
   private static final Color[] PEER_COLORS = {Color.BLUE,
           Color.CYAN,
           Color.GREEN,
           Color.MAGENTA,
           Color.GRAY,
           Color.ORANGE,
           Color.PINK,
           Color.RED
   };

   private ChatManager chatManager = null;
   private Map<String,Color> peerColorMap;
   private List<String> peerList;
   private int peerCounter = 0;
   private boolean isLocal;
   private JTextArea msgBox;
   private JTextField msgField;
   private JTextField addrField;
   private JTextField fromField;

   public MainView(final boolean isLocal)
   {
      super("PeerChat v0.1" + (isLocal ? " (bind addr: " + Main.localBindAddress + ")" : ""));

      this.isLocal = isLocal;

      chatManager = new ChatManager(this);
      peerColorMap = new HashMap<>();
      peerList = new ArrayList<>();

      setSize(400,300);
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      setupCtrls();

      addrField.setText(isLocal ? "127.0.0." : "10.101.102.");
      setVisible(true);

      chatManager.startReceiverThread();
   }

   private void setupCtrls()
   {
      JPanel addrFromPanel = new JPanel();
      JPanel msgSendPanel = new JPanel();

      msgBox = new JTextArea();
      msgField = new JTextField(20);
      addrField = new JTextField(10);
      fromField = new JTextField(10);

      msgField.addKeyListener(new GuiActionListener());

      JButton sendButton = new JButton("Send");
      sendButton.setActionCommand("send_cmd");
      sendButton.addActionListener(new GuiActionListener());

      addrFromPanel.add(new JLabel("To(IP):"));
      addrFromPanel.add(addrField);
      addrFromPanel.add(new JLabel("From:"));
      addrFromPanel.add(fromField);
      //msgField.setSize(390,20);

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
         peerColorMap.put(peerId,PEER_COLORS[peerCounter++%8]);
      }

      String displayMsg = "<"+newMsg.FROM+">: "+newMsg.TEXT;
      //System.out.println(displayMsg);
      //msgBox.setForeground(peerColorMap.get(peerId));
      if (!isLocal && !newMsg.ACK) {
         newMsg.ACK = true;
         chatManager.sendMessage(newMsg, peerId.split(":")[0]);
      }

      msgBox.append(displayMsg + "\n");
   }

   @Override
   public void onError(final String errMsg)
   {
      //TODO
   }

   /*
   private void appendMsg(String msg, Color color)
   {
        StyleContext styleCtx = StyleContext.getDefaultStyleContext();
        AttributeSet attribSet = styleCtx.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }*/

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
            msgBox.append("<" + fromField.getText() + ">: " + msgField.getText() + "\n");
         }
         msgField.setText("");
         chatManager.sendMessage(msg, addrField.getText());
      }


   }

}
