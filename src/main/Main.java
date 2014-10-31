package main;

import gui.MainView;

/**
 * Created by Student on 30.10.2014.
 */
public class Main
{
   //XXX: for debugging only!!
   public static String localBindAddress = null;

   public static void main(final String[] args)
   {

      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            new MainView();
         }
      });
   }

}
