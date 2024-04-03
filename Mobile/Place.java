package Mobile;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;
import java.lang.*;

/**
 * Mobile.Place is the our mobile-agent execution 
 * platform that accepts an agent transferred by 
 * Mobile.Agent.hop( ), deserializes it, and resumes it
 * as an independent thread.
 *
 * @author  Greeshma Sree Parimi
 * @version %I% %G$
 * @since   1.0
 */
public class Place extends UnicastRemoteObject implements PlaceInterface {
    // a loader to define a new agent class
    private AgentLoader loader = null;
    // a sequencer to give a unique agentId  
    private int agentSequencer = 0;     

    // a hashmap to store message from different agents w.r.t key
    private HashMap<String,String> messageMap; 

    /**
     * This constructor instantiates a Mobiel.AgentLoader
     *  object that is used to define a 
     * new agen class coming from remotely.
     */
    public Place( ) throws RemoteException {
        super( );
        loader = new AgentLoader( );
        messageMap = new HashMap<>();
    }

    /**
     * deserialize( ) deserializes a given byte 
     * array into a new agent.
     *
     * @param buf a byte array to be deserialized 
     * into a new Agent object.
     * @return a deserialized Agent object
     */
    private Agent deserialize( byte[] buf ) 
	throws IOException, ClassNotFoundException {
        // converts buf into an input stream
        ByteArrayInputStream in = new ByteArrayInputStream( buf );

        /** AgentInputStream identify a new agent class 
        * and deserialize a ByteArrayInputStream 
        * into a new object */ 
        AgentInputStream input = new AgentInputStream( in, loader );
        return ( Agent )input.readObject();
    }

    /**
     * transfer( ) accepts an incoming agent and
     * launches it as an independent thread.
     *
     * @param classname The class name of an agent to be transferred.
     * @param bytecode  The byte code of  an agent to be transferred.
     * @param entity    The serialized object of an agent to be transferred.
     * @return true if an agent was accepted in success, otherwise false.
     */
    public boolean transfer( String classname, byte[] bytecode, byte[] entity )
	throws RemoteException {
 
        Class agentClass = loader.loadClass(classname, bytecode);
        try{
            // deserialize the agent's entity
            Agent agentEntity = this.deserialize(entity);
            // if these is no Id for agent , set Id
            if(agentEntity.getId() == -1){
                int agentId = InetAddress.getLocalHost( ).hashCode( ) 
                    + agentSequencer++;
                agentEntity.setId(agentId);
            }
            // spawn a child thread by passing agent and start the thread 
            Thread thread = new Thread(agentEntity);
            thread.start();
        }catch(Exception e){
            return false;
        }
        return true;

    }

    /**
     * This method can be called by any agent which have access to 
     * place object.It stores a message sent by agent w.r.t key
     * @param message message sent by agent
     * @param key to store message w.r.t key.
     */
    public synchronized void depositMessage(String message, String key) 
        throws RemoteException{
        /**
         * it stores the message if a message & key exist
         * Otherwise returns.
         */
        if(message.isEmpty() || key.isEmpty()) return;
        messageMap.put(key,message);
        System.out.println("message from " + key + " deposited");
    }

    /**
     * This method can be called by any agent which have access to 
     * place object.Agent can read any message in the map using a 
     * correct key to that message.
     * @param key to read the message w.r.t key.
     */
    public synchronized String readMessage(String key) 
        throws RemoteException{
        /**
         * it returns the message if a message exist to key.
         * Otherwise return null.
         */
        if(key.isEmpty()) return null;
        if(messageMap.containsKey(key)){
            return messageMap.get(key);
        }
        return null;
    }

    /**
     * main( ) starts an RMI registry in local, 
     * instantiates a Mobile.Place agent execution platform, 
     * and registers it into the registry.
     *
     * @param args receives a port, (i.e., 5001-65535).
     */
    public static void main( String args[] ) {
        
        
        try{
            //read port from arguments 
            int port = Integer.parseInt(args[0]);
            if ( port < 5001 || port > 65535 ){
		        throw new Exception( );
	        }
            // start rmi registry at that port
            startRegistry(port);
            // create place Object and binds it the port
            Place placeObject = new Place();
            Naming.rebind( "rmi://localhost:" + port 
                + "/place",placeObject);

            System.out.println("Place Ready");
        }catch(Exception e){
            
        }
    }
    
    /**
     * startRegistry( ) starts an RMI registry 
     * process in local to this Place.
     * 
     * @param port the port to which this RMI should listen.
     */
    private static void startRegistry( int port ) throws RemoteException {
        try {
            Registry registry =
                LocateRegistry.getRegistry( port );
            registry.list( );
        }
        catch ( RemoteException e ) {
            Registry registry =
                LocateRegistry.createRegistry( port );
        }
    }
}