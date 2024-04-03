import Mobile.*;
/**
 * TestAgent is a test mobile agent that is injected to the 1st Mobile.Place
 * platform to print the breath message, migrates to the 2nd platform to
 * say "Hello!", and moves to the 3rd platform to say "Ola! and 
 * even moves to the 3rd platform to say "Namaste!".
 * This program's test's whether a agent can hop to 4 places.
 * 
 * @author  Greeshma Sree Parimi
 * @version %I% %G%
 * @since   1.0
 */
public class TestAgent extends Agent {
    public int hopCount = 0;
    public String[] destination = null;

     /**
     * The consturctor receives a String array as an argument from 
     * Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     */
    public TestAgent( String[] args ) {
        System.out.println("Inside Test Agent");
	    destination = args;
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println("INIT called");
        System.out.println( "agent( " + agentId + ") invoked init: " +
                    "hop count = " + hopCount +
                    ", next dest = " + destination[hopCount] );
        String[] args = new String[1];
        args[0] = "Hello!";
        hopCount++;
        hop( destination[0], "step1", args );
    }
    
    /**
     * step1( ) is invoked upon an agent migration to destination[0] after 
     * init( ) calls hop( ).
     * 
     * @param args arguments passed from init( ).
     */
    public void step1( String[] args ) {
        System.out.println("STEP 1 called");
        System.out.println( "agent( " + agentId + ") invoked step: " +
                    "hop count = " + hopCount +
                    ", next dest = " + destination[hopCount] + 
                    ", message = " + args[0] );
        args[0] = "Ola!";
        hopCount++;
        hop( destination[1], "step2", args );

    }
     /**
     * step2( ) is invoked upon an agent migration to destination[1] after 
     * step1( ) calls hop( ).
     * 
     * @param args arguments passed from step1( ).
     */
    public void step2( String[] args ) {
        System.out.println("STEP 2 called");
        System.out.println( "agent( " + agentId + ") invoked step: " +
                    "hop count = " + hopCount +
                    ", next dest = " + destination[hopCount] + 
                    ", message = " + args[0] );
        args[0] = "Namaste!";
        hopCount++;
        hop( destination[2], "jump", args );

    }

    /**
     * jump( ) is invoked upon an agent migration to destination[2] after
     * step2( ) calls hop( ).
     *
     * @param args arguments passed from step2( ).
     */
    public void jump( String[] args ) {
        System.out.println("JUMP called");
        System.out.println( "agent( " + agentId + ") invoked jump: " +
                    "hop count = " + hopCount +
                    ", message = " + args[0] );
    }
}
