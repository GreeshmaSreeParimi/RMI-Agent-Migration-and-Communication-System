import Mobile.*;
import java.io.*;
import java.rmi.*;
import java.util.Queue;
import java.lang.reflect.*;
import java.net.Inet4Address;
import java.net.InetAddress;


/**
 * TestAgent1 is a test mobile agent that can be injected 
 * to host system and can deposit a message to specified
 * destination Place.
 * 
 * @author  Greeshma Sree Parimi
 * @version %I% %G%
 * @since   1.0
 */

public class TestAgent1 extends Agent{

    public String[] arguments = null;
    public String destination = null;
    public String message = null;

     /**
     * The consturctor receives a String array 
     * as an argument from Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject 
     * to this constructor
     * arg[0] => is the destination hostname of Place, where message
     * should be deposited.
     * arg[1] => is the message that needs to be sent.
     */
    public TestAgent1( String[] args ) {
        System.out.println("Inside My Test Agent 1");
	    arguments = args;
        destination = args[0];
        message = args[1];    
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println("INIT called");
        try{
            message = message + "  from Agent 1 at " + 
                InetAddress.getLocalHost( ).getHostName();
            /**
             * sendMessage() is called to deposit the message.
             */
            sendMessage(destination,message);
        }catch(Exception e){

        }
        
    }
    /**
     * sendMessage is called from init(), to deposit 
     * the user message to user provided hostname.
     * @param hostname it is hostname of Place where message
     * needs to be deposited.
     * @param message it is message from the user.
     */
    public void sendMessage(String hostName, String message){
        /**
         * it retrieves the place object of respective hostname
         * system. place object offers a method to deposit a
         * message by using a key and message. 
         * */ 
        
        try{
            PlaceInterface placeObject =  ( PlaceInterface ) 
                Naming.lookup( "rmi://" + hostName + ":" + _port + "/place" );
            placeObject.depositMessage(message , 
                InetAddress.getLocalHost( ).getHostName( ));
        }catch(Exception e){

        }
    }
}
