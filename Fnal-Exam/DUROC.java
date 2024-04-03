import java.io.*;

import java.rmi.*;

import java.util.*; // for scanner                                                                             

public class DUROC {

    public static void main( String args[] ) {

        int rmi_port = 0;

        try {

            rmi_port = Integer.parseInt( args[0] );

        } catch ( Exception e ) {

            System.exit( -1 );

        }

 

        GRAMInterface[] gram = new GRAMInterface[ args.length - 1 ];

        try {

            // obtain a transaction manager that carries out a two-phase commitment                            

            TransactionManager txmgr = new TransactionManager( );

 

            // find remote GRAMs                                                                               

            for ( int i = 1; i < args.length; i++ ) {

                gram[i - 1] =  ( GRAMInterface )

                    Naming.lookup( "rmi://" + args[i] + ":" + rmi_port + "/GRAM" );

                txmgr.join( gram[i - 1] );

            }

 

            Scanner keyboard = new Scanner( System.in );

 

            System.out.print( "job   = " );

            String command = keyboard.nextLine( );

 

            System.out.print( "#cpus = " );

            int cpus = keyboard.nextInt( );

 

            System.out.print( "mem   = " );

            int mem = keyboard.nextInt( );

 

            System.out.print( "port  = " );

            int port = keyboard.nextInt( );

 

            // read a keyboard input that is a command to be executed remotely                                 

 

            for ( int i = 0; i < gram.length; i++ )

                // pass this command to each remote GRAM.                                                      

                gram[i].launch( command, cpus, mem, port );

            // let the transaction manager conduct a two-phase commitment with the GRAMs                       

            if ( txmgr.commit( ) == true )

                System.out.println( "success" );

            else

                System.out.println( "fail" );

        }

        catch ( Exception e ) {

            e.printStackTrace( );

            System.exit( -1 );

        }

    }

}