package Mobile;
import java.io.*;
import java.rmi.*;
import java.util.Queue;
import java.lang.reflect.*;
import java.net.Inet4Address;
import java.net.InetAddress;

import Mobile.*;

/**
 * Mobile.Agent is the base class of all user-define 
 * mobile agents. It carries an agent identifier, 
 * the next host IP and port, the name of the function to
 * invoke at the next host, arguments passed to 
 * this function, its class name, and its byte code. 
 * It runs as an independent thread that invokes a given
 * function upon migrating the next host.
 *
 * @author  Greeshma Sree Parimi
 * @version %I% %G%
 * @since   1.0
 */
public class Agent implements Serializable, Runnable {
    // live data to carry with the agent upon a migration
    protected int agentId        = -1;    // this agent's identifier
    private String _hostname     = null;  // the next host name to migrate
    // the function to invoke upon a move
    private String _function     = null;  
    protected int _port          = 0;     // the next host port to migrate
    private String[] _arguments  = null;  // arguments pass to _function
    private String _classname    = null;  // this agent's class name
    private byte[] _bytecode     = null;  // this agent's byte code

    /**
     * setPort( ) sets a port that is 
     * used to contact a remote Mobile.Place.
     * 
     * @param port a port to be set.
     */
    public void setPort( int port ) {
	    this._port = port;
    }

    /**
     * setId( ) sets this agent identifier: agentId.
     *
     * @param id an idnetifier to set to this agent.
     */
    public void setId( int id ) {
	    this.agentId = id;
    }

    /**
     * getId( ) returns this agent identifier: agentId.
     *
     * @param this agent's identifier.
     */
    public int getId( ) {
	    return agentId;
    }

    /**
     * getByteCode( ) reads a byte code from
     *  the file whosename is given in
     * "classname.class".
     *
     * @param classname the name of a class to read from local disk.
     * @return a byte code of a given class.
     */
    public static byte[] getByteCode( String classname ) {
        // create the file name
        String filename = classname + ".class";

        // allocate the buffer to read this agent's bytecode in
        File file = new File( filename );
        byte[] bytecode = new byte[( int )file.length( )];

        // read this agent's bytecode from the file.
        try {
            BufferedInputStream bis =
            new BufferedInputStream( new FileInputStream( filename ) );
            bis.read( bytecode, 0, bytecode.length );
            bis.close( );
        } catch ( Exception e ) {
            e.printStackTrace( );
            return null;
        }

        // now you got a byte code 
        // from the file. just return it.
        return bytecode;	
    }

    /**
     * getByteCode( ) reads this agent's 
     * byte code from the corresponding file.
     *
     * @return a byte code of this agent.
     */
    public byte[] getByteCode( ) {
        // bytecode has been already read from a file
	    if ( _bytecode != null ) 
            return _bytecode; 
        
        // obtain this agent's class name and file name
        _classname = this.getClass( ).getName( );
        _bytecode = getByteCode( _classname );

        return _bytecode;
    }

    /**
     * run( ) is the body of Mobile.Agent that is executed 
     * upon an injection or a migration as an independent 
     * thread. run( ) identifies the method with a given 
     * function name and arguments and invokes it. The invoked
     * method may include hop( ) that transfers this agent 
     * to a remote host or simply returns back to 
     * run( ) that termiantes the agent.
     */
    public void run( ) {

        // if arguments exists 
        if(_arguments != null){
            Class[] args = new Class[]{_arguments.getClass()};
            try{
                // get method to invoke based on function and arguments.
                Method method = this.getClass().getMethod(_function,args);
                // invoke the method using arguments
                method.invoke(this, (Object)_arguments);
            }catch(Exception e){
            }
        }else{
            try{
                // get method to invoke based on function
                Method method = this.getClass().getMethod(_function);
                // invoke the method
                method.invoke(this);
            }catch(Exception e){

            }
        }

    }

    /**
     * hop( ) transfers this agent to a given host, and invokes a given
     * function of this agent.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     */    
    public void hop( String hostname, String function ) {
	    hop( hostname, function, null );
    }

    /**
     * hop( ) transfers this agent to a given host, and invokes a given
     * function of this agent as passing given arguments to it.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     * @param args     the arguments passed to a function called upon a 
     *                 migration.
     */
    @SuppressWarnings( "deprecation" )
    public void hop( String hostname, String function, String[] args ) {
        // assign the data to variables 
        _hostname = hostname;
        _function = function;
        _arguments = args;

        // getting the agent byte code
        byte[] byteCode = this.getByteCode();

        // serializing the agent to tranfer it
        byte[] entity = this.serialize();

        try{
            // looking for rmi place object for transfer
            PlaceInterface placeObject =  ( PlaceInterface )
		     Naming.lookup( "rmi://" + hostname + ":" 
                + _port + "/place" );
            
            // transfer the byteCode and Entity to next destination.
            boolean isTransferSuccess = 
                placeObject.transfer(_classname, byteCode, entity);

            System.out.println("isTransferSuccess  :: " 
                + isTransferSuccess); 
            Thread.currentThread( ).stop( );

        }catch(Exception e){

        }    

    }

    public void spawn(String hostname, String agentClassName, String[] args){
        //check if it is local host
        if(hostname == null || hostname.equals("localhost")){
            hostname = "localhost";
        }
        
        //check if agentClassName is null
        if(agentClassName == null){
            agentClassName = this.getClass().getName();
        }
        
        //load the agent's bytecode
        byte[] bytecode = getByteCode(agentClassName);


        //instantiate the child agent
        Agent childAgent = null;

        try{
            Class<?> c = Class.forName(agentClassName);
            childAgent = (Agent) c.newInstance();
        }catch(Exception e){
        }

        // Set the function and arguments of the child agent. 
        // If available, can use getter and setter instead.
        childAgent._function = "init"; 
        childAgent._arguments = args;


        //serialize the child agent
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos );
        oos.writeObject(childAgent);
        oos.close();
        byte[] serializedChildAgent = bos .toByteArray();
        //find the remote place through Naming.lookup()
        try{
            PlaceInterface place = (PlaceInterface) Naming.lookup("rmi://" + hostname + ":" + _port + "/place");
            //invoke the RMI call to transfer the child agent to the specified host
            boolean isTransferSuccess = place.transfer(agentClassName, byteCode, serializedChildAgent );
 		System.out.println("isTransferSuccess  :: " 
                + isTransferSuccess); 


        }catch(Exception e){


        }
    }


    /**
     * serialize( ) serializes this agent into a byte array.
     *
     * @return a byte array to contain this serialized agent.
     */
    private byte[] serialize( ) {
        try {
            // instantiate an object output stream.
            ByteArrayOutputStream out = new ByteArrayOutputStream( );
            ObjectOutputStream os = new ObjectOutputStream( out );
            
            // write myself to this object output stream
            os.writeObject( this );

            return out.toByteArray( ); // conver the stream to a byte array
        } catch ( IOException e ) {
            e.printStackTrace( );
            return null;
        }
    }
}