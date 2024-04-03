import Mobile.*;
import java.io.*;
import java.rmi.*;
import java.util.*;

import java.lang.reflect.*;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * TestAgent2 is a test mobile agent that can be injected 
 * to host system and can read a message from a destiation
 * using the key provided by usera s input.
 * 
 * @author  Greeshma Sree Parimi
 * @version %I% %G%
 * @since   1.0
 */


public class TestAgent2 extends Agent{
    public String[] arguments = null;
    public String destination = null;
    public String messageKey = null;
    
    /**
     * The consturctor receives a String array as an argument from 
     * Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     * @param arg[0] indicates the hostname of system from where
     * message has to be read.
     * @param arg[1] indicates the key, for which message has be read.
     */
    public TestAgent2( String[] args ) {
        System.out.println("Inside My Test Agent 2");
	    arguments = args;
        destination = args[0];
        messageKey = args[1];
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println("INIT called");
        /**
         * recieveMessage is called to read
         * the message
         */
        recieveMessage(destination);
    }

    /**
     * recieveMessage is called from init(), to read 
     * the user message from user provided hostname and key.
     * @param hostname it is hostname of Place from where 
     * message needs to be read.
     */
    public void recieveMessage(String hostName){
        try{
            /**
             * it retrieves the place object of respective hostname
             * system. Place object provides a readMessage method
             * to read message using a key.
             */
            PlaceInterface placeObject =  ( PlaceInterface ) 
                Naming.lookup( "rmi://" + hostName + ":" + _port + "/place" );

            String message = placeObject.readMessage(messageKey);
            /** if there is no message for particular it prints 
             * null. Otherwise, prints the message.
             */
            if(message == null){
                System.out.println("There does not exist message with the key :" 
                + messageKey);
            }else {
                System.out.println("Agent 2 at " + 
                InetAddress.getLocalHost().getHostName()
                 + " recieved :: " + message);
            }
        }catch(Exception e){

        }
    }
}
