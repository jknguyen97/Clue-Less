package tkm.gamelogic;

import java.util.ArrayList;

import tkm.enums.CharacterType;

public class Player {
    private final String name;
    private CharacterType character;  // The character the player is playing as
    private GamePiece gamePiece;
    private final ArrayList<Card> hand;
    private boolean isEliminated;
    
    // initialize a player with a name and character
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>(); 
    }

    public boolean isEliminated() {
        return isEliminated;
    }

    public void resetStatus() {
        this.isEliminated = false; // Mark the player as active again
        this.clearHand();         // Remove all cards from the player's hand

    }

    public void setEliminated(boolean eliminated) {
        isEliminated = eliminated;
    }
    
    public void move(int x, int y) {
        this.gamePiece.setPosition(x, y);
    }
    
    public boolean hasCard(String cardName) {
        for(Card card : hand) {
            if(card.getName().equals(cardName))
                return true;
        }
        return false;
    }
    
    public String getName() {
        return name;
    }

    public CharacterType getCharacter() {
        return character;
    }
    
    public void setGamePiece(GamePiece gamePiece) {
        this.gamePiece = gamePiece;
        this.setCharacter();
    }
    
    private void setCharacter() {
        this.character = gamePiece.getCharacter();
    }
    
    public void addCard(Card card) {
        this.hand.add(card);
    }
    
    public void clearHand() {
        hand.clear();
    }

    public ArrayList<Card> getHand() {
        return hand;
    }
    
    public GamePiece getGamePiece() {
        return gamePiece;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append(name);
        b.append(" ");
        b.append(gamePiece.toString());
        b.append(" ");
        
        for(Card card: hand) {
            b.append(card.toString());
        }
        
        return b.toString();
    }
}
