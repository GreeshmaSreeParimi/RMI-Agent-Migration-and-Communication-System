import java.io.*;
import java.rmi.*;
import java.util.*;

public class ServerImpl implements ServerInterface {
    private Map<String, Set<String>> fileAccessMap = new HashMap<>();
    private Map<String, String> clientPermissionsMap = new HashMap<>();

    public byte[] download(String client, String filename, String mode) throws RemoteException {
        // Check if the client requested write mode
        if ("write".equals(mode)) {
            // Check if the file is already in write mode by another client
            Set<String> clients = fileAccessMap.get(filename);
            if (clients != null) {
                Iterator<String> iterator = clients.iterator();
                while (iterator.hasNext()) {
                    String c = iterator.next();
                    if ("write".equals(clientPermissionsMap.getOrDefault(c, ""))) {
                       
                        // Add the client to the write queue for the file
                        Queue<String> writeQueue = writeQueueMap.computeIfAbsent(filename, k -> new LinkedList<>());
                        writeQueue.add(client);

                        throw new RemoteException("File is already in write mode by another client");
                    }
                }
            }
        }

        // Update the file's access mode for the current client
        Set<String> accessClients = fileAccessMap.getOrDefault(filename, new HashSet<>());
        accessClients.add(client);
        fileAccessMap.put(filename, accessClients);

        // Update the client's permission
        clientPermissionsMap.put(client, mode);

        // Read the file from disk
        return readFileFromDisk(filename);
    }

    public boolean upload(String client, String filename, byte[] contents) throws RemoteException {
        
        // Check if the client has read permission for the file
        String clientPermission = clientPermissionsMap.get(client);
        if (clientPermission != null && (clientPermission.equals("read") || clientPermission.equals("write"))) {
            
            // remove client from the file list
            Set<String> clients = fileAccessMap.get(filename);
            if(clients !=null && clients.contains(client)){
                clients.remove(client);
                fileAccessMap.set(clients);
                clientPermissionsMap.remove(client);
            }     

           if(clientPermission.equals("read")) return true;
        }
        
        // if write mode
        // Write the file content to disk
        boolean success = writeFileToDisk(filename, contents);

        // Notify other clients to invalidate their cache for the file
        Set<String> clients = fileAccessMap.get(filename);
        if (clients != null) {
            Iterator<String> iterator = clients.iterator();
            while (iterator.hasNext()) {
                String c = iterator.next();
                if (!c.equals(client)) { // check if the client is not equals to current client
                    try {
                        int port = 28210;
                        ClientInterface clientInterface = (ClientInterface)  Naming.lookup( "rmi://" + c + ":" 
                + port + "/Client" );
                        clientInterface.invalidate();
                    } catch (Exception e) {
                        // Handle the exception as per your requirements
                    }
                }
            }
        }

        // Grant access to the next client in the write queue, if any
        Queue<String> writeQueue = writeQueueMap.getOrDefault(filename, new LinkedList<>());
        if (!writeQueue.isEmpty()) {
            String nextClient = writeQueue.poll();
            clientPermissionsMap.put(nextClient, "write");

            // Read the file from disk
            byte[] fileContent = readFileFromDisk(filename);

            // Send the file to the next client
            try {
                ClientInterface clientInterface = (ClientInterface) Naming.lookup(nextClient);
                clientInterface.receiveFile(filename, fileContent);
            } catch (Exception e) {
                // Handle the exception as per your requirements
            }
        }

        // Return true for successful write operations
        return true;
    }

    private byte[] readFileFromDisk(String filename) throws RemoteException {
        try {
            File file = new File(filename);
            byte[] fileContent = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            fis.read(fileContent);
            fis.close();

            return fileContent;
        } catch (IOException e) {
            throw new RemoteException("Failed to read file from disk: " + e.getMessage());
        }
    }

    private boolean writeFileToDisk(String filename, byte[] contents) throws RemoteException {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(contents);
            fos.close();

            return true;
        } catch (IOException e) {
            throw new RemoteException("Failed to write file to disk: " + e.getMessage());
        }
    }
}