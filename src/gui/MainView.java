package gui;

import net.ChatManager;
import net.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
   private JTextArea msgBox;
   private JTextField msgField;
   private JTextField addrField;
   private JTextField fromField;

   public MainView(final boolean isLocal)
   {
      super("PeerChat v0.1");
      chatManager = new ChatManager(this,isLocal);
      peerColorMap = new HashMap<>();
      peerList = new ArrayList<>();

      setSize(400,300);
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

      setupCtrls();

      addrField.setText("10.101.102.");
      //addrField.setText(isLocal ? "127.0.0.2" : "127.0.0.1");

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


      addWindowStateListener(new WindowAdapter()
      {
         @Override
         public void windowClosed(WindowEvent e)
         {
            super.windowClosed(e);
            chatManager.stopReceiverThread();
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
      msgBox.append(displayMsg + "\n");
   }


   private class GuiActionListener implements ActionListener
   {

      @Override
      public void actionPerformed(ActionEvent e)
      {
         Message msg = new Message(fromField.getText(),msgField.getText());
         msgBox.append("<"+fromField.getText()+">: "+msgField.getText()+"\n");
         msgField.setText("");
         chatManager.sendMessage(msg,addrField.getText());
      }
   }

}
