package Mobile;

import java.rmi.*;
import java.util.Queue;

/**
 * Mobile.PlaceInterface defines Place's RMI method that will be called from
 * an Mobile.Agent.hop( ) to transfer an agent.
 *
 * @author  Greeshma Sree Parimi
 * @version %I% %G%
 * @since   1.0
 */
public interface PlaceInterface extends Remote {
    /**
     * transfer( ) accepts an incoming agent and 
     * launches it as an independent thread.
     *
     * @param classname The class name of an agent 
     * to be transferred.
     * @param bytecode  The byte code of  an agent 
     * to be transferred.
     * @param entity    The serialized object of an 
     * agent to be transferred.
     * @return true if an agent was accepted 
     * in success, otherwise false.
     */
    public boolean transfer( String classname, byte[] bytecode,
			     byte[] entity ) throws RemoteException;

    /** depositMessage accepts a message and key from
     * agent and stores it the message in hashmap w.r.t key
     * 
     * @param message message sent by agent
     * @param key key to store message in map
     * @throws RemoteException
     */
    public void depositMessage(String message, String key) 
        throws RemoteException;

    /** readMessage accepts key from agent and return  
     * the message in hashmap w.r.t key.
     * 
     * @param key key to search message in map
     * @throws RemoteException
     */
    public String readMessage(String key) throws RemoteException;

}