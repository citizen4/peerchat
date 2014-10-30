package net;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Student on 30.10.2014.
 */
public class ChatManager
{
   private static final int PORT = 60000;
   private static final String LOCAL_HOST = "127.0.0.1";
   private static final String REMOTE_HOST = "127.0.0.2";
   private DatagramSocket recvSocket = null;
   private DatagramSocket sendSocket = null;
   private InetAddress localAddress;
   private InetAddress remoteAddress;
   private Thread receiverThread = null;
   private Listener listener = null;
   private List<String> clientList;

   public ChatManager(final Listener listener,final boolean isLocal)
   {
      this.listener = listener;


      try {
         localAddress = InetAddress.getByName(isLocal ? LOCAL_HOST : REMOTE_HOST);
         sendSocket = new DatagramSocket(0);

      } catch (SocketException | UnknownHostException e) {
         e.printStackTrace();
      }

      clientList = new ArrayList<>();

   }

   public void startReceiverThread()
   {
      if (receiverThread == null || !receiverThread.isAlive()) {

         if (recvSocket == null) {
            try {
               //recvSocket = new DatagramSocket(PORT, localAddress);
               recvSocket = new DatagramSocket(PORT);
               recvSocket.setSoTimeout(500);
            } catch (SocketException e) {
               e.printStackTrace();
               return;
            }
         }

         receiverThread = new Thread(new Runnable()
         {
            @Override
            public void run()
            {
               System.out.println("Receiver thread started...");

               byte[] paketData = new byte[1024];
               String msg, id;

               while (!receiverThread.isInterrupted()) {
                  DatagramPacket packet = new DatagramPacket(paketData, paketData.length);
                  msg = id = "";
                  try {
                     // blocking call
                     recvSocket.receive(packet);
                     id = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                     msg = new String(packet.getData(), "UTF-8");
                     parsePaket(msg, id);
                  } catch (IOException e) {
                     if (!(e instanceof SocketTimeoutException)) {
                        e.printStackTrace();
                     }
                  }
               }

               recvSocket.close();
               recvSocket = null;

               System.out.println("Out of while loop");
            }
         }, "ReceiverThread");

         receiverThread.start();
      }
   }

   public void stopReceiverThread()
   {
      System.out.println("stopReceiverThread() called");
      if (receiverThread != null && receiverThread.isAlive()) {
         receiverThread.interrupt();
      }
   }

   public void sendMessage(final Message message, final String peerAddress)
   {
      final Gson gson = new Gson();
      System.out.println("Msg: "+gson.toJson(message)+" to: "+peerAddress);

      new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            try {
               byte[] pktData = gson.toJson(message).getBytes("UTF-8");

               remoteAddress = InetAddress.getByName(peerAddress);
               DatagramPacket packet = new DatagramPacket(pktData, pktData.length, remoteAddress, PORT);
               //System.out.println("Sending msg to: "+remoteAddress.getHostAddress());
               sendSocket.send(packet);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }, "SendThread").start();
   }

   private void parsePaket(final String jsonMsg,final String peerId)
   {
      Gson gson = new Gson();
      Message newMsg = gson.fromJson(jsonMsg.trim(), Message.class);

      listener.onNewMessage(newMsg,peerId);
   }


   public interface Listener
   {
      public void onNewMessage(final Message newMsg,final String peerId);
   }

}
