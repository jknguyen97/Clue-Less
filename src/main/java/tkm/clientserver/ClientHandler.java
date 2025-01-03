
package tkm.clientserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

import tkm.gamelogic.Card;
import tkm.gamelogic.GamePiece;
import tkm.gamelogic.Player;

/**
 * @file ClientHandler.java
 * @date 10/19/2024
 * @author Mike
 * 
 * This class helps the server, by handling the logic of communicating with
 * connected clients.
 */

public class ClientHandler implements Runnable{
    
    private Socket clientSocket;        // socket of the client
    private BufferedReader incoming;    // Handles incoming information from client
    private PrintWriter outgoing;       // Handles sending info to client
    private Server server;              // server that uses this Handler
    private Player player;
    private String username;
    private boolean host;
    
    public ClientHandler(Socket clientSocket, Server server, boolean host) {
        this.host = host;
        this.clientSocket = clientSocket;
        /**
         * TO DO
         * add a variable to keep track of the current gamestate
         */
        this.server = server;
        //this.username = username;
    }
    
    @Override
    public void run() {
        try {
            // Reads incoming data from the client
            incoming = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Outputs data from the server to the client
            outgoing = new PrintWriter(clientSocket.getOutputStream(), true);
            
            /** TO DO
            *
            * Logic to handle clients gameState changes
            * More Message types for the client handler to manage, (MOVE, SUGGEST)
            * 
            */
            
            // Asks for the host's username
//            username = JOptionPane.showInputDialog(null, "Enter your username:");
//            player = new Player(username);
//            server.getGameBoard().addPlayer(player);
//            
//            // Updates the player count when a player joins and sends them the starting
//            // state
//            server.broadcast("PLAYERJOINED: " + server.getClientListSize() + "|END|");
//            server.broadcast("GAMEBOARD: " + server.getGameBoard().stringTileMap() + "|END|", this);
//            server.broadcast("PIECES: " + server.getGameBoard().stringPieces() + "|END|", this);
//            server.broadcast("INITIALIZE" + "|END|", this);
            /*
            This reads any messages that are incoming from the client
            */

            StringBuilder messageBuffer = new StringBuilder();
            String line;
        
            while ((line = incoming.readLine()) != null) {
                messageBuffer.append(line).append("\n");

                // If we receive the end delimiter, process the full message
                if (line.contains("|END|")) {
                    String fullMessage = messageBuffer.toString();
                    messageBuffer.setLength(0); // Clear buffer after processing
                    handleClientMessage(fullMessage);
                }
            }
            
        } catch(IOException e) {
            System.out.println("Client handler encountered an issue: " 
                    + e.getMessage());
        } finally {
            /*
            Ensure that sockets and streams close
            */
            try {
                clientSocket.close();
                incoming.close();
                outgoing.close();
            } catch(IOException e) {
                System.out.println("Could not close client socket: " 
                        + e.getMessage());
            }
        }
    }
    
    // For sending a message from the server to this client being handled
    public void sendMessage(String message) {
        outgoing.println(message);
    }
    
    /*
    This is the important method in the Client Handler, as it decides what to do
    when receiving messages from the connected clients. Anytime you want a client
    to do a change on their end and have it reflected on everyones machine, you 
    have to first send the message on the client side and then process it here.
    */
    private void handleClientMessage(String fullMessage) {
        // Removing the "|END|" from the message
        //fullMessage = fullMessage.replace("|END|", "").trim();
        System.out.println(fullMessage);

        // Update chat if a chat message comes from the server
        if (fullMessage.startsWith("CHAT: ")) {
            server.broadcast(username + ": " + fullMessage); // Broadcast chat messages to all clients
        }
        
        else if(fullMessage.contains("END_TURN")) {
            server.getGameBoard().nextTurn();
        }
        
        // This message is recieved when a client clicks the move button, giving
        // them valid movement options to choose from
        else if(fullMessage.contains("REQUEST_MOVES")) {
            GamePiece piece = server.getGameBoard().getPlayerGamePiece(player);
            Set<int[]> validMoves = server.getGameBoard().generateValidMoves(piece);
            StringBuilder movesMessage = new StringBuilder("VALID_MOVES:");

            for (int[] move : validMoves) {
                movesMessage.append(move[0]).append(",").append(move[1]).append("|");
            }
            movesMessage.append("|END|");
            sendMessage(movesMessage.toString());
            
        } 
        
        // Once a client chooses a valid move option, they send a message with
        // their choice and the server updates and broadcasts.
        else if(fullMessage.startsWith("MOVE: ")) {
            this.handleMove(fullMessage);
        }
        
        // Once a client makes a suggestion, the server must decide which player,
        // if any of them, have cards to show the client.
        else if(fullMessage.contains("SUGGESTION:")) {
            String message = fullMessage.replace("SUGGESTION:", "").replace("|END|", "").trim();
            
            String[] suggestion = message.split("\\|");
            
            server.getGameBoard().handleSuggestion(suggestion, player);
            server.broadcast("PIECES: " + server.getGameBoard().stringPieces() + "|END|");
            server.broadcast("REDRAW|END|");
            server.getGameBoard().getCurrentClient().sendMessage("SUGGEST_DISABLE|END|");
        }

        else if (fullMessage.contains("ACCUSATION:")) {
            String message = fullMessage.replace("ACCUSATION:", "").replace("|END|", "").trim();

            String[] accusation = message.split("\\|");

            server.getGameBoard().handleAccusation(accusation, player);
            server.broadcast("REDRAW|END|");
        }
        
        else if(fullMessage.contains("CARD_REVEAL:")) {
            String message = fullMessage.replace("|END|", "");
            message += "|" + this.username + "|END|";
            server.getGameBoard().getCurrentClient().sendMessage(message);
        }
        
        // This message sends out the Player's Hand information, allowing the UI
        // to show their hand correctly.
        else if(fullMessage.contains("REQUEST_HAND")) {
            StringBuilder cardsMessage = new StringBuilder("PLAYER_CARDS:");
            
            for(Card card : this.player.getHand()) {
                cardsMessage.append(card.getName()).append("|");
            }
            
            cardsMessage.append("|END|");
            sendMessage(cardsMessage.toString());
        }
        
        // Once a client joins the server it sends its username, allowing the 
        // server to add them to the gameboard.
        else if(fullMessage.contains("PLAYER:")) {
            this.username = fullMessage.replace("PLAYER:", "").replace("|END|", "").trim();
            
            player = new Player(username);
            server.getGameBoard().addPlayer(player, this);
            
            // Updates the player count when a player joins and sends them the starting
            // state
            server.broadcast("PLAYERJOINED: " + server.getClientListSize() + "|END|");
            server.broadcast("GAMEBOARD: " + server.getGameBoard().stringTileMap() + "|END|", this);
            server.broadcast("PIECES: " + server.getGameBoard().stringPieces() + "|END|", this);
            server.broadcast("INITIALIZE" + "|END|", this);
        }
        
        // This is used to start the game, with only the host being allowed to do it
        else if (fullMessage.contains("START")) {
            if(this.host)
                server.broadcast(fullMessage);
        }
        
        // An unknown client message was received

    
    // handle the full game reset message
    else if (fullMessage.contains("GAME_RESET_FULL")) {
        String[] resetDetails = fullMessage.split("\\|");

        // Potentially update local game state or trigger UI reset
        server.broadcast("GAME_RESET_ACKNOWLEDGED|" + username + "|END|");
        server.broadcast("RESET_UI");
    }

    else if (fullMessage.contains("GAME_RESET_DETAILS")) {
        // handle detailed reset information if needed
        server.broadcast("RESET_DETAILS_ACKNOWLEDGED|" + username + "|END|");
    }

 //   else if (fullMessage.contains("RESET_UI")) {
 //       server.broadcast("RESET_UI");
 //   }
    else if (fullMessage.contains("REQUEST_LOCATION")) {
        GamePiece piece = server.getGameBoard().getPlayerGamePiece(player);
        boolean inRoom = piece.getinRoom();
        String message = fullMessage.replace("|END|", "");

        if (inRoom == false) {
            message += "|" + "FALSE" + "|END|";
            server.getGameBoard().getCurrentClient().sendMessage(message);
        } else {
            message += "|" + "TRUE" + "|END|";
            server.getGameBoard().getCurrentClient().sendMessage(message);
        }
    }

    else if (fullMessage.contains("CHECK_ELIMINATED")) {
        boolean isElim = player.isEliminated();

        if (isElim == true) {
            server.getGameBoard().getCurrentClient().sendMessage("CHECK_ELIMINATED|TRUE|END|");
        } else {
            server.getGameBoard().getCurrentClient().sendMessage("CHECK_ELIMINATED|FALSE|END|");
        }
    }
    else if (fullMessage.contains("REQUEST_ROOM")) {
        GamePiece piece = server.getGameBoard().getPlayerGamePiece(player);
        int x = piece.getX();
        int y = piece.getY();
        String message = "CURRENT_ROOM" + "|" + Integer.toString(x) + "|" + Integer.toString(y) + "|" + "|END|";
        server.getGameBoard().getCurrentClient().sendMessage(message);
    }

    else if(fullMessage.contains("REQUEST_CHARACTER")) {
        //GamePiece piece = server.getGameBoard().getPlayerGamePiece(this.player);
        //String character = piece.getCharacter().getName();
        String character = player.getCharacter().getName();
        //String character = "TEST TEST";
        sendMessage("PLAYER_CHARACTER" + character + "|END|");
    }
    // An unknown client message was received
    else {
        outgoing.println("Unknown command: " + fullMessage);
    }
}

    // This method receives the movement choice from the player, and updates
    // the game board accordingly. After the update, the server broadcasts the
    // change to all clients.
    private void handleMove(String message) {
        //System.out.println(message);
        message = message.replace("MOVE: ", "").replace("|END|", "").trim();
        String[] coordinates = message.split(",");
        if (coordinates.length == 2) {
            try {
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);

                // Move the player on the game board
                server.getGameBoard().movePlayer(player, x, y);
                server.broadcast("PIECES: " + server.getGameBoard().stringPieces() + "|END|");
                server.broadcast("REDRAW|END|");
            } catch (NumberFormatException e) {
                System.out.println("Invalid move coordinates from client: " + message);
            }
        }
    }
    
    public Player getPlayer() {
        return player;
    }
}