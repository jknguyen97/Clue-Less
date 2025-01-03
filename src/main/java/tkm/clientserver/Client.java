
package tkm.clientserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import tkm.Main;
import tkm.enums.CharacterType;
import tkm.gamelogic.GamePiece;

/**
 * @file Client.java
 * @date 10/16/2024
 * @author Mike
 * 
 * This class creates a client using Java Sockets, and connects to our Server
 * Class to send and receive messages from the server.
 */

public class Client implements Runnable{
    
    private int port;
    private String serverAddress;
    private Socket socket;
    private BufferedReader incoming;    // getting updates from the server
    private PrintWriter outgoing;       // writing messages to the server
    private boolean host;
    
    // Possibly bad design to pass in the whole app class, maybe just chatArea?
    private Main main;                    // Reference to game so client can update
    private int[][] tileMap;
    private ArrayList<GamePiece> pieces;
    private Map<String, String> hallwayMap;
    private Map<Integer, String> roomMap;
    
    // Constructor, creates a socket, and connects to the server
    public Client(String serverAddress, int port, Main main, boolean host) {
        this.host = host;
        this.serverAddress = serverAddress;
        this.port = port;
        this.main = main;
        this.pieces = new ArrayList<>();
        
        // Try to connect to the server, and create input/output streams
        try {
            socket = new Socket(this.serverAddress, this.port);
            incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outgoing = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server at: " + serverAddress + ":" 
                    + port);
        } catch(IOException e) {
            System.out.println("Could not connect to server: " 
                    + e.getMessage());
        }
    }
    
    // This controls what happens while the client is connected.
    @Override
    public void run() {
        
        String username = JOptionPane.showInputDialog(null, "Enter your username:");
        this.sendMessage("PLAYER:" + username + "|END|");
        
        try {
        
            StringBuilder messageBuffer = new StringBuilder();
            String line;
        
            while ((line = incoming.readLine()) != null) {
                messageBuffer.append(line).append("\n");

                // If we receive the end delimiter, process the full message
                if (line.contains("|END|")) {
                    String fullMessage = messageBuffer.toString();
                    messageBuffer.setLength(0); // Clear buffer after processing
                    handleServerMessage(fullMessage);
                }
            }
        } catch(IOException e) {
            System.out.println("Error communicating with server: " 
                    + e.getMessage());
        } finally {
            /*
            Ensures all sockets and streams close
            TO DO
            Add a check to see if they exist != null
            */
            try {
                socket.close();
                incoming.close();
                outgoing.close();
            } catch(IOException e) {
                System.out.println("Error closing client socket: " 
                        + e.getMessage());
            }
        }
    }
    
    // For sending a message to the Server
    public void sendMessage(String message) {
        outgoing.println(message);
    }
    
    /*
    This method is important for anything you want to change on the client's
    system based on messages recieved from the server. This would be when a different
    client moves for example. It receives a message in the form 
    "MESSAGE_TYPE: [DATA] | [DATA] |END|"
    or
    "MESSAGE_TYPE"
    */
    private void handleServerMessage(String fullMessage) {
        // Removing the "|END|" from the message
        fullMessage = fullMessage.replace("|END|", "").trim();
        //System.out.println(fullMessage);

        // Update chat if a chat message comes from the server
        if (fullMessage.contains("CHAT: ")) {
            main.updateChat(fullMessage.replace("CHAT: ", ""));
        } 
        
        // Tells the client it is their turn, enabling turn based options.
        else if(fullMessage.contains("YOUR_TURN")) {
            JOptionPane.showMessageDialog(main, "It is your turn!");
            main.getOptionsPanel().enableSwitch(true);
            sendMessage("CHECK_ELIMINATED|END|");
            sendMessage("REQUEST_LOCATION: " + "|END|");
            sendMessage("REQUEST_ROOM: " + "|END|");
        }
        
        else if(fullMessage.contains("CHECK_ELIMINATED")) {
            //String message = fullMessage.replace("CHECK_ELIMINATED", "").trim();
            if (fullMessage.contains("TRUE")) {
                main.elimPlayer();
            }
            
        }
        else if(fullMessage.contains("REQUEST_LOCATION")) {
            String message = fullMessage.replace("REQUEST_LOCATION: ", "").trim();
            if (fullMessage.contains("FALSE")) {
                main.getOptionsPanel().setSuggest(false);
            } else if (fullMessage.contains("TRUE")) {
                main.getOptionsPanel().setSuggest(true);
            }
        }

        else if(fullMessage.contains("PLAYERS_TURN:")) {
            String username = fullMessage.replace("PLAYERS_TURN:", "").trim();
            JOptionPane.showMessageDialog(main, "It is " + username + "'s turn.");
        }
        
        else if(fullMessage.contains("DISPROVE:")) {
            String message = fullMessage.replace("DISPROVE: ", "").trim();
            
            String[] options = message.split("\\|");
            
            String selectedOption = (String) JOptionPane.showInputDialog(
                    null, 
                    "Disprove the Suggestion",
                    "Pick a Card to Reveal",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);
            
            sendMessage("CARD_REVEAL: " + selectedOption + "|END|");
            
        }
        
        // If the client makes a suggestion and no player can disprove it
        else if(fullMessage.contains("NONE")) {
            JOptionPane.showMessageDialog(null, "No Players could disprove your suggestion!");
        }
        
        else if(fullMessage.contains("CARD_REVEAL:")) {
            String message = fullMessage.replace("CARD_REVEAL:", "").trim();
            
            String[] options = message.split("\\|");
            
            JOptionPane.showMessageDialog(null, options[1] + " shows you the " 
                    + options[0] + " card.");
        }
        
        // Updates the amount of players that have joined in the lobby
        else if (fullMessage.startsWith("PLAYERJOINED: ")) {
            main.updatePlayerCount(Integer.parseInt(fullMessage.substring(14)));
        }
        
        // Starts the game for non-host players, proceeds to the gamepanel
        else if (fullMessage.equals("START")) {
            main.startGameForJoinedPlayers();
        } 
        
        // This server message is used to create the starting gameboard based on the
        // server data
        else if (fullMessage.startsWith("GAMEBOARD: ")) {
            this.tileMap = this.parseTileMap(fullMessage.substring(11));
            
            this.hallwayMap = new HashMap<>();
            this.initializeHallwayMap();
            this.roomMap = new HashMap<>();
            this.initializeRoomMap();
        }
        
        // This server message is used to both create the starting game piece locations
        // and when updating their locations.
        else if (fullMessage.startsWith("PIECES: ")) {
            this.pieces = (this.parseGamePieces(fullMessage.substring(8)));
        } 
        
        // This server message is used to create the client's game panel when the game
        // starts
        else if (fullMessage.equals("INITIALIZE")) {
            main.initializeGamePanel(tileMap, pieces);
        } 
        
        // This server message is used to create the client's card Panel when the
        // game starts
        else if(fullMessage.startsWith("PLAYER_CARDS:")) {
            // removes the message type, and the data separators |.
            String[] cards = fullMessage.substring(13).split("\\|");
            main.createCardPanel(cards);
        }

        else if(fullMessage.equals("SUGGEST_DISABLE")) {
            main.getOptionsPanel().setSuggest(false);
        }

        else if(fullMessage.contains("PLAYER_CHARACTER")) {
            //String character = fullMessage;
            //main.setCharacter("TESTING TESTING");
            String message = fullMessage.replace("PLAYER_CHARACTER", "").trim();
            this.main.createCharacterPanel(message);
        }
        
        else if (fullMessage.contains("RESET_UI")) {
            main.resetUI();
        }
        // This server message is used to present the player with possible moves
        // when they click on the move button
        else if(fullMessage.startsWith("VALID_MOVES:")) {
            // removes the message type, and the data separators |.
            String[] moves = fullMessage.substring(12).split("\\|");
            
            // Map to hold the coordinate string as key and the location name as value
            Map<String, String> moveOptionsMap = new HashMap<>();

            for (String move : moves) {
                System.out.println(move);
                if (!move.isEmpty()) {
                    // Parse the coordinate (x, y) from the move string
                    String[] coordinates = move.split(",");
                    int x = Integer.parseInt(coordinates[0].trim());
                    int y = Integer.parseInt(coordinates[1].trim());

                    // Get the name of the location using your locationName() method
                    String locationName = locationName(new int[]{x, y});

                    // Add to the map: coordinate string as key, human-readable name as value
                    moveOptionsMap.put(move, locationName);
                }
            }

            // Create an array of the human-readable move options
            Object[] readableOptions = moveOptionsMap.values().toArray();

            // Show the JOptionPane to the user with human-readable options
            String selectedOption = (String) JOptionPane.showInputDialog(
                null,
                "Select your move:",
                "Move Options",
                JOptionPane.PLAIN_MESSAGE,
                null,
                readableOptions,
                readableOptions[0]
            );
            
            // Find the original coordinate string corresponding to the selected option
            if (selectedOption != null && !selectedOption.isEmpty()) {
                // Find the coordinate key by value (the selected human-readable name)
                for (Map.Entry<String, String> entry : moveOptionsMap.entrySet()) {
                    if (entry.getValue().equals(selectedOption)) {
                        String originalCoordinate = entry.getKey();
                        // Send the original coordinate back to the server
                        sendMessage("MOVE: " + originalCoordinate + "|END|");
                        main.getOptionsPanel().setMove(false);
                        sendMessage("REQUEST_LOCATION: " + "|END|");
                        sendMessage("REQUEST_ROOM: " + "|END|");
                        break;
                    }
                }
            }
        } 

        else if(fullMessage.contains("CURRENT_ROOM")) {
            String[] coordinates = fullMessage.split("\\|");
            int x = Integer.parseInt(coordinates[1]);
            int y = Integer.parseInt(coordinates[2]);
            int[] intArray = {x,y};
            String currentRoom = locationName(intArray);
            main.setCurrentRoom(currentRoom);
        }
        // This server message tells the client to redraw their gamepanel to
        // represent any changes.
        else if(fullMessage.equals("REDRAW")) {
            main.redrawGamePanel(pieces);
        }

    }

    
    // Returns a location name when given coordinates
    private String locationName(int[] location) {
        // Check to see if the location is a hallway, if so return it
        String key = location[0] + "," + location[1];
        if(hallwayMap.containsKey(key))
            return hallwayMap.get(key);
        
        // Must be a room
        int room = tileMap[location[1]][location[0]];
        if(roomMap.containsKey(room))
            return roomMap.get(room);
        
        return "Error finding location name!";
    }
    
    // Makes a map of the hallway coordinates to enable a string representation
    // of player move choices
    private void initializeHallwayMap() {
        hallwayMap.put("3,6", "Study-Library Hallway");
        hallwayMap.put("6,10", "Library-Billiard Room Hallway");
        hallwayMap.put("3,13", "Library-Conservatory Hallway");
        hallwayMap.put("6,17", "Conservatory-Ballroom Hallway");
        hallwayMap.put("13,17", "Ballroom-Kitchen Hallway");
        hallwayMap.put("10,13", "Ballroom-Billiard Room Hallway");
        hallwayMap.put("13,10", "Billiard Room-Dining Room Hallway");
        hallwayMap.put("10,6", "Billiard Room-Hall Hallway");
        hallwayMap.put("6,3", "Study-Hall Hallway");
        hallwayMap.put("13,3", "Hall-Lounge Hallway");
        hallwayMap.put("17,6", "Lounge-Dining Room Hallway");
        hallwayMap.put("17,13", "Dining Room-Kitchen Hallway");
    }
    
    // Makes a map of the room types to enable string representation of player
    // move choices.
    private void initializeRoomMap() {
        roomMap.put(3, "Study");
        roomMap.put(4, "Hall");
        roomMap.put(5, "Lounge");
        roomMap.put(6, "Library");
        roomMap.put(7, "Billiard Room");
        roomMap.put(8, "Dining Room");
        roomMap.put(9, "Conservatory");
        roomMap.put(10, "Ballroom");
        roomMap.put(11, "Kitchen");
    }
    
    // Used to put the string tileMap data received from the server back
    // together again.
    private int[][] parseTileMap(String serializedTileMap) {
        String[] rows = serializedTileMap.split("\n");
        int[][] newTileMap = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String[] values = rows[i].split(",");
            newTileMap[i] = new int[values.length];
            for (int j = 0; j < values.length; j++) {
                newTileMap[i][j] = Integer.parseInt(values[j]);
            }
        }

        return newTileMap;
    }
    
    // Used to put the string representation of the gamepieces and their locations
    // back into usable data.
    private ArrayList<GamePiece> parseGamePieces(String serializedPieces) {
        ArrayList<GamePiece> jpieces = new ArrayList<>();
        String[] lines = serializedPieces.split("\n");

        for (String line : lines) {
            //System.out.println(line);
            String[] parts = line.split(",");
            
            if (parts.length == 3) {
                String characterName = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);

                 // Find the matching CharacterType
                CharacterType character = null;
                for (CharacterType type : CharacterType.values()) {
                    if (type.getName().equalsIgnoreCase(characterName)) {
                        character = type;
                        break;
                    }
                }

                if (character != null) {
                    jpieces.add(new GamePiece(x, y, character));
                } else {
                    System.err.println("Character not found for name: " + characterName);
                }
            }
        }
        return jpieces;
    }

    public boolean getHost() {
        return host;
    }
    
}