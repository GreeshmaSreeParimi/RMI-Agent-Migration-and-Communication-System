import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GRAM extends UnicastRemoteObject implements GRAMInterface {
    private int cpus;
    private long mem;
    private int port;
    private String job;
    private boolean prepared;

    public GRAM() throws RemoteException {
        super();
        this.mem = 0;
        this.port = 0;
        this.job = "";
        this.prepared = false;
    }

    public void launch(String job, long mem, int port) {
        this.job = job;
        this.mem = mem;
        this.port = port;
    }

    public boolean prepare() throws RemoteException {
        try{
            int CPU = runtime.availableProcessors();
            long MEM = runtime.freeMemory();
            Socket socket = new ServerSocket(port)


            if (MEM >= mem && CPU >= cpus && socket != null) {
                prepared = true;
                return true;
            } else {
                prepared = false;
                return false;
            }
        }catch(Exception e){
            prepared = false;
            return false;
        }
       
    }

    public boolean doCommit(String transaction) throws RemoteException {
        if (prepared) {
            Process process = null;
            try {
                process = runtime.exec(job);
                int exitValue = process.waitFor();

                if (exitValue == 0) {
                    job = "";
                    cpus = 0;
                    mem = 0;
                    port = 0;
                    prepared = false;

                    return true;
                } else {
                    job = "";
                    cpus = 0;
                    mem = 0;
                    port = 0;
                    prepared = false;

                    return false;
                }
            } catch (IOException | InterruptedException e) {
                job = "";
                cpus = 0;
                mem = 0;
                port = 0;
                prepared = false;

                return false;
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        } else {
            // Participant not prepared, cannot commit
            return false;
        }
    }

    public boolean doAbort(String transaction) throws RemoteException {
        if (prepared) {
            // Perform abort actions for the participant
            // ...

            // Reset variables for the next transaction
            job = "";
            cpus = 0;
            mem = 0;
            port = 0;
            prepared = false;

            return true;
        } else {
            // Participant not prepared, no need to abort
            return false;
        }
    }

    // Other methods and variables as needed
}

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private List<GRAMInterface> participants;

    public TransactionManager() {
        participants = new ArrayList<>();
    }

    public void join(GRAMInterface participant) {
        participants.add(participant);
    }

    public boolean commit() {
        boolean allPrepared = true;
        boolean anyParticipantAbort = false;

        // Phase 1: Prepare phase
        for (GRAMInterface participant : participants) {
            try {
                boolean prepared = participant.prepare();
                if (!prepared) {
                    allPrepared = false;
                    anyParticipantAbort = true;
                }
            } catch (RemoteException e) {
                // Error occurred while preparing, consider it as an abort
                allPrepared = false;
                anyParticipantAbort = true;
            }
        }

        // Phase 2: Commit or Abort phase
        if (allPrepared) {
            for (GRAMInterface participant : participants) {
                try {
                    boolean commit = participant.doCommit();
                    if (!commit) {
                        anyParticipantAbort = true;
                    }
                } catch (RemoteException e) {
                    // Error occurred while committing, consider it as an abort
                    anyParticipantAbort = true;
                }
            }
        }

        // Abort the transaction if any participant failed to commit
        if (anyParticipantAbort) {
            abort();
            return false;
        }

        // All participants committed successfully
        return true;
    }

    private void abort() {
        for (GRAMInterface participant : participants) {
            try {
                participant.doAbort();
            } catch (RemoteException e) {
                // Error occurred while aborting, ignore it
            }
        }
    }
}