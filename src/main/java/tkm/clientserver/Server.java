
package tkm.clientserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tkm.Main;
import tkm.gamelogic.GameBoard;

/**
 * @file Server.java
 * @date 10/16/2024
 * @author Stephen Snelling
 * 
 * The purpose of this class is to create a server that runs on a host's local 
 * machine by utilizing Java Sockets, letting up to 6 players(including the host)
 * join. The Server has a helper class called ClientHandler that is utilized to
 * manage joined clients.
 */

public class Server implements Runnable{
    
    public static final int PORT = 25565;            //place holder
    private ServerSocket socket;
    private ExecutorService clientPool;             // Handles Client Threads concurrently
    private ArrayList<ClientHandler> clientList;    // List of all Clients on the server
    //private Map<ClientHandler, Player> 
    private boolean acceptingClients;               // Should the server accept clients
    private GameBoard gameBoard;
    private Main main;
    
    /* TO DO
    Game state/logic class variable for server to manage and send updates to
    players
    */
    
    // Constructor, attempts to set up a server socket on PORT, while creating
    // a Executor pool to manage concurrency, and a client list that has references
    // to all connected clients
    public Server(Main main) {
        this.main = main;
        this.gameBoard = new GameBoard();
        this.gameBoard.setServer(this);
        try {
            socket = new ServerSocket(PORT);
            clientPool = Executors.newCachedThreadPool();
            clientList = new ArrayList<>();
            // Can be used to toggle the server accepting new clients.
            acceptingClients = true;                
            System.out.println("Server is running on port: " + PORT);
            
        } catch(IOException e) {
            System.out.println("There was an error trying to open a server socket: "
            + e.getMessage());
        } 
        
    } // end constructor
    
    // method that initializes the server
    @Override
    public void run() {
        try {
            // Server will accept clients until 6 people have joined
            while(acceptingClients) {
                Socket clientSocket = socket.accept();
                
                /**
                 * TO DO
                 * Add a way to toggle client acceptance when the game starts
                 * with 3-6 players
                 */
                
                // Checks if there are less than 6 players, allowing the client
                // to join if so, otherwise close the socket that accepts new
                // clients
                if(clientList.size() < 6) {
                    System.out.println("New Client Connected: " + clientSocket.getInetAddress());

                    ClientHandler clientHandler;
                    
                    // Is the client the host or not
                    if(clientList.isEmpty()) {
                        clientHandler = new ClientHandler(clientSocket, this, true);
                    } else {
                        clientHandler = new ClientHandler(clientSocket, this, false);
                    }
                    clientList.add(clientHandler);
                    // use the pool to handle communication between clients
                    clientPool.execute(clientHandler);
                } else {
                    clientSocket.close();
                    System.out.println("Connection Refused: Maximum players reached.");
                }
            }
        } catch(IOException e) {
            System.out.println("There was an error accepting a client: " 
                    + e.getMessage());
        } finally {
            // Ensure server shutdown
            this.shutdown();
        }
    }
    
    // broadcasts updates to all clients that are connected to the Server
    public synchronized void broadcast(String message) {
        // Goes through the list of connected clients, and sends the message
        for(ClientHandler client: clientList) {
            client.sendMessage(message);
        }
    }
    
    // broadcast update to a specific client, used for gameboard initialization
    public synchronized void broadcast(String message, ClientHandler client) {
        client.sendMessage(message);
    }
    
    // Helper method to shut down sockets and threads
    private void shutdown() {
        try {
            // Check if server socket exists, and hasnt been closed already
            if(socket != null && (socket.isClosed() == false)) {
                socket.close();
                System.out.println("Server Socket Closed.");
            }
        } catch(IOException e) {
            System.out.println("An error occured attempting to close server socket: " 
                    + e.getMessage());
        } 
        
        // Close the executor pool if need be
        if(clientPool != null && (clientPool.isShutdown() == false)) {
            clientPool.shutdown();
            System.out.println("Client pool shutdown.");
        }
    }
    
    public GameBoard getGameBoard() {
        return gameBoard;
    }
    
    public Main getMain() {
        return main;
    }
    
    public int getClientListSize() {
        return clientList.size();
    }
    
} // end of class Server