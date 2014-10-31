package net;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.Main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
   private DatagramSocket recvSocket = null;
   private DatagramSocket sendSocket = null;
   private Thread receiverThread = null;
   private Listener listener = null;
   private List<String> clientList;

   public ChatManager(final Listener listener)
   {
      this.listener = listener;

      try {
         sendSocket = new DatagramSocket(0);
      } catch (SocketException /*| UnknownHostException*/ e) {
         e.printStackTrace();
      }
   }

   public void startReceiverThread()
   {
      if (receiverThread == null || !receiverThread.isAlive()) {

         if (recvSocket == null) {
            try {
               recvSocket = new DatagramSocket(null);
               recvSocket.setSoTimeout(500);
               //recvSocket.setReuseAddress(true);
               InetSocketAddress bindAddress = (Main.localBindAddress != null) ?
                     new InetSocketAddress(Main.localBindAddress, PORT) : new InetSocketAddress(PORT);
               recvSocket.bind(bindAddress);
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

               //byte[] paketData = new byte[1024];
               String msg, id;

               while (!receiverThread.isInterrupted()) {
                  byte[] packetData = new byte[1024];
                  DatagramPacket packet = new DatagramPacket(packetData, packetData.length);
                  msg = id = "";
                  try {
                     // blocking call
                     recvSocket.receive(packet);
                     id = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                     msg = new String(packet.getData(), "UTF-8");
                     packetData = null;
                     parsePacket(msg, id);
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
      System.out.println("Msg: " + gson.toJson(message) + " to: " + peerAddress);

      new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            try {
               byte[] pktData = gson.toJson(message).getBytes("UTF-8");
               DatagramPacket packet = new DatagramPacket(pktData, pktData.length,
                     InetAddress.getByName(peerAddress), PORT);
               //System.out.println("Sending msg to: "+remoteAddress.getHostAddress());
               sendSocket.send(packet);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }, "SendThread").start();
   }

   private void parsePacket(String jsonMsg, final String peerId)
   {
      Gson gson = new Gson();
      try {
         jsonMsg = jsonMsg.trim();
         Message newMsg = gson.fromJson(jsonMsg, Message.class);
         listener.onNewMessage(newMsg, peerId);
      } catch (JsonSyntaxException e) {
         System.err.println("JsonSyntaxException: " + e.getMessage());
         System.err.println("JSON:'" + jsonMsg + "'");
         byte[] data = jsonMsg.getBytes();
         for (byte b : data) {
            System.err.print(Integer.toHexString(b) + " ");
         }
         System.err.println();
      }
   }


   public interface Listener
   {
      public void onNewMessage(final Message newMsg, final String peerId);

      public void onError(final String errMsg);
   }

}
