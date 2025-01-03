
package tkm.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import tkm.clientserver.ClientHandler;
import tkm.clientserver.Server;
import tkm.enums.CharacterType;
import tkm.enums.RoomType;
import tkm.enums.WeaponType;

/**
 * @file GameBoard.
 * @date 10/26/2024
 * @author Mike
 * 
 * This class represents the game board, and its current state. Keeps track of
 * all player positions, room/hallway/startingSquare states, and updates when
 * changes are made.
 */
public class GameBoard {
    
    private ArrayList<Player> players;
    private ArrayList<Card> caseFile;
    private ArrayList<Card> deck;
    private ArrayList<GamePiece> pieces;
    private Map<Player, List<Card>> playerHands;
    private Map<Player, GamePiece> playerPieces;
    private Map<Player, ClientHandler> playerClients;
    //private ArrayList<BufferedImage> images;
    private int currentTurn = 0;
    private Server server;
    private ArrayList<ClientHandler> clientList;

    private static final int TILEMAP[][] = {
            {0, 0, 0, 0, 0, 0, 0, 0,  0,  0,  0,  0, 0, 0, 0,  0,  0,  0,  0, 0},
            {0, 3, 3, 3, 3, 0, 0, 0,  4,  4,  4,  4, 0, 0, 0,  5,  5,  5,  5, 0},
            {0, 3, 3, 3, 3, 0, 0, 0,  4,  4,  4,  4, 0, 2, 0,  5,  5,  5,  5, 0},
            {0, 3, 3, 3, 3, 1, 1, 1,  4,  4,  4,  4, 1, 1, 1,  5,  5,  5,  5, 0},
            {0, 3, 3, 3, 3, 0, 0, 0,  4,  4,  4,  4, 0, 0, 0,  5,  5,  5,  5, 0},
            {0, 0, 0, 1, 0, 0, 0, 0,  0,  0,  1,  0, 0, 0, 0,  0,  0,  1,  0, 0}, 
            {0, 0, 2, 1, 0, 0, 0, 0,  0,  0,  1,  0, 0, 0, 0,  0,  0,  1,  2, 0},
            {0, 0, 0, 1, 0, 0, 0, 0,  0,  0,  1,  0, 0, 0, 0,  0,  0,  1,  0, 0},
            {0, 6, 6, 6, 6, 0, 0, 0,  7,  7,  7,  7, 0, 0, 0,  8,  8,  8,  8, 0},
            {0, 6, 6, 6, 6, 0, 0, 0,  7,  7,  7,  7, 0, 0, 0,  8,  8,  8,  8, 0},
            {0, 6, 6, 6, 6, 1, 1, 1,  7,  7,  7,  7, 1, 1, 1,  8,  8,  8,  8, 0},
            {0, 6, 6, 6, 6, 0, 0, 0,  7,  7,  7,  7, 0, 0, 0,  8,  8,  8,  8, 0},
            {0, 0, 0, 1, 0, 0, 0, 0,  0,  0,  1,  0, 0, 0, 0,  0,  0,  1,  0, 0}, 
            {0, 0, 2, 1, 0, 0, 0, 0,  0,  0,  1,  0, 0, 0, 0,  0,  0,  1,  0, 0},
            {0, 0, 0, 1, 0, 0, 0, 0,  0,  0,  1,  0, 0, 0, 0,  0,  0,  1,  0, 0},
            {0, 9, 9, 9, 9, 0, 0, 0, 10, 10, 10, 10, 0, 0, 0, 11, 11, 11, 11, 0},
            {0, 9, 9, 9, 9, 0, 0, 0, 10, 10, 10, 10, 0, 0, 0, 11, 11, 11, 11, 0},
            {0, 9, 9, 9, 9, 1, 1, 1, 10, 10, 10, 10, 1, 1, 1, 11, 11, 11, 11, 0},
            {0, 9, 9, 9, 9, 0, 2, 0, 10, 10, 10, 10, 0, 2, 0, 11, 11, 11, 11, 0},
            {0, 0, 0, 0, 0, 0, 0, 0,  0,  0,  0,  0, 0, 0, 0,  0,  0,  0,  0, 0}
        };
    
    
    public GameBoard() {
       players = new ArrayList<>();
       deck = new ArrayList<>();
       caseFile = new ArrayList<>();
       pieces = new ArrayList<>();
       
       playerHands = new HashMap<>();
       playerPieces = new HashMap<>();
       playerClients = new HashMap<>();

       this.createGamePieces();
       this.createDeck();
    }
    
    /*
    This method is called when the host starts the game. It shuffles the deck of
    cards, creates the case file(murder deck), and shuffles the player order. It
    then proceeds to assign players to gamepieces, while dealing out cards based on 
    the asforementioned deck.
    */
    public void startGame() {
        //Shuffle the deck
        Collections.shuffle(deck);
        this.createCaseFile();
        System.out.println(this.caseFileToString());
        // Shuffle the players, in effect creating a turn order.
        Collections.shuffle(players);
        this.assignPlayerToGamePiece();
        this.dealCards();

        String solutionMessage = "CHAT: Case file: " +
                this.caseFileToString() + "|END|";
        server.broadcast(solutionMessage);

        // Tell the first player it is their turn
        playerClients.get(players.get(0)).sendMessage("YOUR_TURN|END|");
        
        // Let the other players know whose turn it is
        for(int i = 1; i < players.size(); i++) {
            playerClients.get(players.get(i)).sendMessage("PLAYERS_TURN: " 
                    + players.get(0).getName() + "|END|");
        }
        
        // for debugging
        for(Player player : players)
            System.out.println(player.toString());
    }
    
    // This method increments the turn
    public void nextTurn() {
        this.currentTurn = (currentTurn + 1) % players.size();
        
        System.out.println("Current Turn: " + currentTurn);
        
        playerClients.get(players.get(currentTurn)).sendMessage("YOUR_TURN|END|");
        
        for(int i = 0; i < players.size(); i++) {
            if(i != currentTurn)
                playerClients.get(players.get(i)).sendMessage("PLAYERS_TURN: " 
                        + players.get(currentTurn).getName() + "|END|");
        }
    }
    
    // This method handles a player making a suggestion
    public void handleSuggestion(String[] suggestion, Player player) {
        int nextPlayer = currentTurn + 1;
        
        // if the suspect is not the players character, update the suspects
        // position
        if(player.getCharacter().getName().equals(suggestion[0]) == false) {

            int roomNumber = this.getRoomNumber(suggestion[2]);
            int[] move = this.findFirstUnoccupiedTile(roomNumber);

            for(GamePiece piece : pieces) {
                if(piece.getCharacter().getName().equals(suggestion[0])) {
                    piece.setPosition(move[0], move[1]);
                    break;
                }
            }
        }

        StringBuilder disprove = new StringBuilder("DISPROVE: ");
        
        while(nextPlayer % players.size() != currentTurn) {
            for(String suggest : suggestion) {
                if(players.get(nextPlayer % players.size()).hasCard(suggest)) {
                    disprove.append(suggest).append("|");
                }
            }
            // This means the nextPlayer has a card to disprove
            if(disprove.length() > 10) {
                disprove.append("|END|");
                playerClients.get(players.get(nextPlayer % players.size())).sendMessage(disprove.toString());
                return;
            }
            nextPlayer++;
        }
        
        // None had the cards from the current suggestion, let the current player
        // know.
        playerClients.get(players.get(currentTurn)).sendMessage("NONE|END|");
    }

    public void handleAccusation(String[] accusation, Player player) {
        // Extract accusation details
        eliminatePlayer(player);
        String suspect = accusation[0];
        String weapon = accusation[1];
        String room = accusation[2];

        // Check if the accusation matches the case file
        boolean isCorrect = caseFile.get(0).getName().equals(suspect) &&
                caseFile.get(1).getName().equals(weapon) &&
                caseFile.get(2).getName().equals(room);

        if (isCorrect) {
            // Notify all players of the correct accusation
            String victoryMessage = "CHAT: " + player.getName() + " has solved the mystery! The case file reveals:\n" +
                    caseFileToString() + " Congratulations!|END|";
            String winMessage = "GAME_OVER|WINNER|" + player.getName() + "|END|";
            server.broadcast(winMessage);
            server.broadcast(victoryMessage);

            JOptionPane.showMessageDialog(null,
                    player.getName() + " has solved the murder mystery! They found out it was " +
                caseFileToString() + " Congratulations!",
                    "Mystery Solved!",
                    JOptionPane.INFORMATION_MESSAGE);
            // End the game
            endGame();

        } else {

            // Eliminate the player and notify everyone
            eliminatePlayer(player);
            String failMessage = "ACCUSATION_FAILED|" + player.getName() + "|END|";
            server.broadcast(failMessage);

            // Check if all players are eliminated
            if (players.isEmpty()) {
                handleAllPlayersEliminated();
            } else {
                // Update game visuals if needed
                server.broadcast("PIECES: " + server.getGameBoard().stringPieces() + "|END|");
                server.broadcast("REDRAW|END|");
            }
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }

    private void eliminatePlayer(Player player) {
        // Mark the player as eliminated instead of removing them
        player.setEliminated(true);

        // Notify other players about the elimination
        String eliminationMessage = "CHAT: " + player.getName() + " has been eliminated from the game!|END|";
        server.broadcast(eliminationMessage);

        // Show a popup notification
        JOptionPane.showMessageDialog(null,
                player.getName() + " has made an incorrect accusation! They are now eliminated from the game!",
                "Player Eliminated!",
                JOptionPane.INFORMATION_MESSAGE);

                handleAllPlayersEliminated();
    }

    private void handleAllPlayersEliminated() {
        // Track eliminated players
        boolean allPlayersEliminated = true;
        for (Player player : players) {
            if (!player.isEliminated()) {
                allPlayersEliminated = false;
                break;
            }
        }

        if (allPlayersEliminated) {
            String caseFileReveal = "CHAT: All players have been eliminated! The murder mystery remains unsolved! The case file reveals:\n" +
                    caseFileToString() + "|END|";

            // Broadcast the reveal to the chat panel
            server.broadcast(caseFileReveal);

            JOptionPane.showMessageDialog(null,
                    "The mystery remains unsolved! The case file reveals:\n\n" +
                            caseFileToString(),
                    "Mystery Unsolved!",
                    JOptionPane.INFORMATION_MESSAGE);
            server.getGameBoard().endGame();
        }
    }

    public void endGame() {
        System.out.println("The game is over!");

        // Create options for the user
        String[] options = {"New Game", "Exit"};

        // Show a dialog with Restart and Exit options
        int choice = JOptionPane.showOptionDialog(
                null,
                "The game has ended. Would you like the start a new game?",
                "Game Over",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]  // Default selection is Exit
        );

        // Handle user choice
        if (choice == 0) {  // Restart Game
            server.broadcast("RESET_UI");



            restartGame();
        } else {  // Exit or closed dialog
            System.exit(0);
        }
    }
    // method to handle game restarts
    public void restartGame() {
        
        if (players.isEmpty() && clientList != null) {
            System.out.println("Repopulating players from clientList...");
            for (ClientHandler client : clientList) {
                Player player = client.getPlayer();
                System.out.println("ClientHandler: " + client + ", Player: " + (player == null ? "null" : player.getName()));
                if (player != null) {
                    players.add(player);
                }
            }
        }

        if (players.isEmpty()) {
            System.out.println("No players available to restart the game.");
            JOptionPane.showMessageDialog(null,
                    "Cannot start a new game. No players available.",
                    "Game Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Comprehensive reset broadcast
        server.broadcast("GAME_RESET_FULL|" +
                "Players=" + players.stream().map(Player::getName).collect(Collectors.joining(",")) + "|" +
                "TileMap=" + stringTileMap() + "|" +
                "END|");

        // Detailed reset information
        String resetMessage = "GAME_RESET_DETAILS: " +
                "New Case File=" + this.caseFileToString() +
                "|First Player=" + players.get(0).getName() +
                "|END|";
        server.broadcast(resetMessage);

        // Reset game state while keeping players intact
        deck.clear();
        caseFile.clear();
        pieces.clear();
        playerHands.clear();
        playerPieces.clear();
        currentTurn = 0;

        // Recreate game components
        this.createGamePieces();
        this.createDeck();

        // Clear player hands but keep players
        for (Player player : players) {
            player.clearHand(); // Reset cards for each player
            player.setEliminated(false); // Reset elimination status
        }

        // Shuffle the deck and set up the new game state
        Collections.shuffle(deck);
        this.createCaseFile();
        this.assignPlayerToGamePiece();
        this.dealCards();

        // Broadcast piece and player information
        server.broadcast("PIECES: " + stringPieces() + "|END|");
        server.broadcast("GAMEBOARD: " + stringTileMap() + "|END|");

        // Broadcast the new game state to all connected clients
        if (server != null) {
            String solutionMessage = "CHAT: New case file: " + this.caseFileToString() + "|END|";
            server.broadcast(solutionMessage);

            // Notify each player of their hand
            for (Player player : players) {
                ClientHandler clientHandler = playerClients.get(player);
                if (clientHandler != null) {
                    StringBuilder cardsMessage = new StringBuilder("PLAYER_CARDS:");
                    for (Card card : player.getHand()) {
                        cardsMessage.append(card.getName()).append("|");
                    }
                    cardsMessage.append("|END|");
                    clientHandler.sendMessage(cardsMessage.toString());
                }
            }

            // Notify the first player it's their turn
            Player firstPlayer = players.get(0);
            server.broadcast("PLAYERS_TURN: " + firstPlayer.getName() + "|END|");
            playerClients.get(firstPlayer).sendMessage("YOUR_TURN|END|");
        }

        System.out.println("Game restarted successfully.");
    }



    private int getRoomNumber(String roomName) {
        switch(roomName) {
            case "Study" -> {
                return 3;
            }
            case "Hall" -> {
                return 4;
            }
            case "Lounge" -> {
                return 5;
            }
            case "Library" -> {
                return 6;
            }
            case "Billiard Room" -> {
                return 7;
            }
            case "Dining Room" -> {
                return 8;
            }
            case "Conservatory" -> {
                return 9;
            }
            case "Ballroom" -> {
                return 10;
            }
            case "Kitchen" -> {
                return 11;
            }
        }
        // Something bad Happend here!!
        System.out.println("Something wrong with suggestion message(getRoomNumber method)");
        return 0;
    }
    
    // Adds a player/client to the GameState
    public void addPlayer(Player player, ClientHandler client) {
        players.add(player);
        playerClients.put(player, client);
    }
    
    // assign players to game pieces.
    private void assignPlayerToGamePiece() {
        Collections.shuffle(pieces);
        for(int i = 0; i < players.size(); i++) {
            playerPieces.put(players.get(i), pieces.get(i));
            players.get(i).setGamePiece(pieces.get(i));
        }
    }
    
    /*
    This method deals out cards from the deck. It does this by looping through
    the deck until it is empty, decrementing the playerIndex each time(resets if
    it falls below 0), until the deck is empty. In effect it deals out the remaining
    deck to the players one by one.
    */
    private void dealCards() {
        int playerIndex = players.size() - 1;
        while(!deck.isEmpty()) {
            players.get(playerIndex).addCard(deck.getFirst());
            deck.removeFirst();
            playerIndex--;
            
            if(playerIndex < 0) {
                playerIndex = players.size() - 1;
            }
        }
        
        // Map players to their hand for easier retrieval later.
        for(int i = 0; i < players.size(); i++) {
            playerHands.put(players.get(i), players.get(i).getHand());
        }
    }

    // Helper method that creates all the cards from enum types
    private void createDeck() {
        for(CharacterType character : CharacterType.values())
            deck.add(new Card(character.getName(), 1));
        for(WeaponType weapon : WeaponType.values())
            deck.add(new Card(weapon.getName(), 2));
        for(RoomType room : RoomType.values())
            deck.add(new Card(room.getName(), 3));
    }
    
    // This helper method creates the gamepieces based on their information in
    // CharacterType
    private void createGamePieces() {
        for(CharacterType character : CharacterType.values()) {
            pieces.add(new GamePiece(character.getX(), character.getY(), character)); 
        }
    }
    
    // Creates the case file by picking one of each type of card, removing that
    // card from the deck.
    private void createCaseFile() {
        caseFile.add(this.findFirstCard(1));
        caseFile.add(this.findFirstCard(2));
        caseFile.add(this.findFirstCard(3));
    }
    
    // For debug purposes, and checking correct accusations
    // This prints out a string representation of the case file.
    private String caseFileToString() {
        StringBuilder b = new StringBuilder();
        
        b.append(caseFile.get(0).getName());
        b.append(" with the "); 
        b.append(caseFile.get(1).getName());
        b.append(" in the "); 
        b.append(caseFile.get(2).getName()); 
        b.append(".");
        
        return b.toString();
    }
    
    // Helper method to find the first card of a passed in type(character, weapon, room)
    // It loops through the deck until it finds the first of passed in type.
    private Card findFirstCard(int type) {
        for(Card card : deck) {
            if(card.getType() == type) {
                deck.remove(card);
                return card;
            }    
        }
        return null; // something wrong happened here!
    }
    
    // Method to generate valid moves for the current player's turn
    public Set<int[]> generateValidMoves(GamePiece piece) {
        Set<int[]> validMoves = new HashSet<>();
        int x = piece.getX();
        int y = piece.getY();

        if (TILEMAP[y][x] == 1) { // Player is in a hallway
            // Check adjacent rooms to move into
            addAdjacentRooms(validMoves, x, y);
        } else if (TILEMAP[y][x] >= 3 && TILEMAP[y][x] <= 11) { // Player is in a room
            // Move to adjacent hallway if unoccupied
            addAdjacentHallways(validMoves, x, y);
            // Secret passages
            addSecretPassages(validMoves, x, y);
        } else {
           // If player is on a starting square, move to adjacent hallway
            addAdjacentHallways(validMoves, x, y);
        }

        return validMoves;
    }
    
    private void addAdjacentRooms(Set<int[]> validMoves, int x, int y) {
        // Assuming you are in a hallway, check the adjacent rooms.

        // Check above (North)
        if (y > 0 && TILEMAP[y - 2][x] >= 3 && TILEMAP[y - 2][x] <= 11 && !isOccupied(x, y - 2)) {
            validMoves.add(findFirstUnoccupiedTile(TILEMAP[y - 2][x])); // Find the first available spot in the room above.
        }

        // Check below (South)
        if (y < TILEMAP.length - 1 && TILEMAP[y + 2][x] >= 3 && TILEMAP[y + 2][x] <= 11 && !isOccupied(x, y + 2)) {
            validMoves.add(findFirstUnoccupiedTile(TILEMAP[y + 2][x])); // Find the first available spot in the room below.
        }

        // Check left (West)
        if (x > 0 && TILEMAP[y][x - 2] >= 3 && TILEMAP[y][x - 2] <= 11 && !isOccupied(x - 2, y)) {
            validMoves.add(findFirstUnoccupiedTile(TILEMAP[y][x - 2])); // Find the first available spot in the room to the left.
        }

        // Check right (East)
        if (x < TILEMAP[y].length - 1 && TILEMAP[y][x + 2] >= 3 && TILEMAP[y][x + 2] <= 11 && !isOccupied(x + 2, y)) {
            validMoves.add(findFirstUnoccupiedTile(TILEMAP[y][x + 2])); // Find the first available spot in the room to the right.
        }
}
    
    // Use BFS in order to find the nearest hallways. Add one to whatever it finds
    // to use the middle hallway.
    private void addAdjacentHallways(Set<int[]> validMoves, int x, int y) {
        int roomNumber = TILEMAP[y][x];
        boolean[][] visited = new boolean[TILEMAP.length][TILEMAP[0].length];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{x, y});
        visited[y][x] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currX = current[0];
            int currY = current[1];

            for (int[] direction : DIRECTIONS) {
                int adjX = currX + direction[0];
                int adjY = currY + direction[1];

                if (this.isWithinBounds(adjX, adjY) && !visited[adjY][adjX]) {
                    int tileType = TILEMAP[adjY][adjX];

                    if (tileType == roomNumber) {
                        queue.offer(new int[]{adjX, adjY});
                    } else if (tileType == 1) {
                        // Found a hallway tile adjacent to the room
                        // Now find the next hallway tile (e.g., middle of the hallway)
                        int[] hallwayTile = this.findNextHallwayTile(adjX, adjY, direction);
                        if (hallwayTile != null && !this.isOccupied(hallwayTile[0], hallwayTile[1])) {
                            validMoves.add(hallwayTile);
                        }
                    }
                    visited[adjY][adjX] = true;
                }
            }
        }
    }

    // Helper data used for the BFS movement
    private static final int[][] DIRECTIONS = {
        {0, -1}, // Up
        {1, 0},  // Right
        {0, 1},  // Down
        {-1, 0}  // Left
    };

    // used in the BFS movement to check if the tile is out of bounds, 0 in our
    // case.
    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < TILEMAP[0].length && y >= 0 && y < TILEMAP.length;
    }

    // Helper method to find the next hallway tile
    private int[] findNextHallwayTile(int x, int y, int[] direction) {
        List<int[]> hallwayTiles = new ArrayList<>();
        int currX = x;
        int currY = y;

        // Collect hallway tiles along the direction
        while (isWithinBounds(currX, currY) && TILEMAP[currY][currX] == 1) {
            hallwayTiles.add(new int[]{currX, currY});
            currX += direction[0];
            currY += direction[1];
        }

        if (!hallwayTiles.isEmpty()) {
            // Choose the middle tile of the hallway
            int middleIndex = hallwayTiles.size() / 2;
            return hallwayTiles.get(middleIndex);
        }

        return null; // No valid hallway tile found
    }

    // Helper to add secret passage moves to valid moves
    private void addSecretPassages(Set<int[]> validMoves, int x, int y) {
        switch (TILEMAP[y][x]) {
            case 3 -> // Study to Kitchen
                validMoves.add(findFirstUnoccupiedTile(11));
            case 11 -> // Kitchen to Study
                validMoves.add(findFirstUnoccupiedTile(3));
            case 5 -> // Lounge to Conservatory
                validMoves.add(findFirstUnoccupiedTile(9));
            case 9 -> // Conservatory to Lounge
                validMoves.add(findFirstUnoccupiedTile(5));
            default -> {
            }
        }
    }

    // Helper to check if a specific tile is occupied
    private boolean isOccupied(int x, int y) {
        for (GamePiece piece : pieces) {
            if (piece.getX() == x && piece.getY() == y) {
                return true;
            }
        }
        return false;
    }

    // Helper to find the first unoccupied tile in a room
    // Used when there are more than on character/weapon in a room, as they should
    // not overlap.
    private int[] findFirstUnoccupiedTile(int roomNumber) {
        for (int y = 0; y < TILEMAP.length; y++) {
            for (int x = 0; x < TILEMAP[y].length; x++) {
                if (TILEMAP[y][x] == roomNumber && !isOccupied(x, y)) {
                    return new int[]{x, y};
                }
            }
        }
        return null; // In case no unoccupied tile is found
    }

    // Method to move a player to a new location
    public void movePlayer(Player player, int newX, int newY) {
        GamePiece piece = playerPieces.get(player);
        if (piece != null) {
            piece.setPosition(newX, newY);
            if (TILEMAP[newY][newX] == 1) { // Player is in a hallway
                piece.setInRoom(false);
            } else if (TILEMAP[newY][newX] >= 3 && TILEMAP[newY][newX] <= 11) { // Player is in a room
                piece.setInRoom(true);
            }
        }

        
    }

    // Method to validate a move before it's made, currently this is unused, I
    // thought it might be necessary to valid a move before it is made, but this
    // method is currently redundant.
//    public boolean validateMove(GamePiece piece, int newX, int newY) {
//        Set<int[]> validMoves = generateValidMoves(piece);
//        for (int[] move : validMoves) {
//            if (move[0] == newX && move[1] == newY) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    public int[][] getTileMap() {
        return TILEMAP;
    }
    
    public ArrayList<GamePiece> getGamePieces() {
        return pieces;
    }
    
    public GamePiece getPlayerGamePiece(Player player) {
        return playerPieces.get(player);
    }
    
    public List<Card> getPlayerCards(Player player) {
        return playerHands.get(player);
    }
    
    public ClientHandler getCurrentClient() {
        return playerClients.get(players.get(currentTurn));
    }
    
    // For debugging, prints a string representation of the deck
    private void printDeck() {
        for(Card card : deck) {
            System.out.print(card);
        }
        System.out.println();
    }
    
    // returns a string representation of the tileMap, used when creating the
    // game panel.
    public String stringTileMap() {
        StringBuilder b = new StringBuilder();
        
        for(int[] row : TILEMAP) {
            for(int col : row) {
                b.append(col);
                b.append(",");
            }
            b.append("\n");
        }
        return b.toString();
    }
    
    // returns a string representation of the game pieces. Used when updating or
    // creating the game panel
    public String stringPieces() {
        StringBuilder b = new StringBuilder();
        
        for(GamePiece piece : pieces) {
            b.append(piece.toString());
            b.append("\n");
        }
        
        return b.toString();
    }
    
} // end class GameBoard