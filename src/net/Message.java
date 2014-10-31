package net;

/**
 * Created by Student on 30.10.2014.
 */
public class Message
{
   public Boolean ACK;
   public String FROM;
   public String TEXT;

   public Message()
   {
      this(false, "null", "null");
   }

   public Message(final Boolean ack, final String from, final String text)
   {
      ACK = ack;
      FROM = from;
      TEXT = text;
   }

}
