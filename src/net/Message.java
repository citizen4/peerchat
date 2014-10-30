package net;

/**
 * Created by Student on 30.10.2014.
 */
public class Message
{

   public String FROM;
   public String TEXT;

   public Message()
   {
      this("null","null");
   }

   public Message(final String from, final String text)
   {
      FROM = from;
      TEXT = text;
   }

}
