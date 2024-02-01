import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends Thread {
    private int playerIndex;
    private int denomination;
    private Deck leftDeck;
    private Deck rightDeck;
    private List<Card> hand;
    private FileWriter outputFile;
    private boolean hasWon;
    private Player winningPlayer = null;
    private List<Player> players;

    public Player(int playerIndex, Deck leftDeck, Deck rightDeck, FileWriter outputFile, List<Player> players) {
        this.playerIndex = playerIndex;
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        this.outputFile = outputFile;
        this.hand = new ArrayList<>();
        this.hasWon = false;
        this.denomination = -1;
        this.winningPlayer = null;
        this.players = players;
    }

    @Override
    public void run() {
        try {
            printInitialHand();
            
            while (!hasWon && !leftDeck.isEmpty()) {
                drawCard();
                discardCard();
                checkForWin();
            }
            
            if (hasWon) {
                declareWin();
            } else {
                declareExit();
            }

            printFinalHand();
            
            outputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    // Draws a card from the left deck and adds it to the player's hand, synchronized to prevent race conditions.
    // Logs the discarded card, deck details, and the player's current hand to the output file.
    public synchronized void drawCard() throws InterruptedException {
    	synchronized (leftDeck) {
	        Card drawnCard = leftDeck.drawCardFromTop();
	        hand.add(drawnCard);
	        try {
	            outputFile.write("player " + playerIndex + " draws a " + drawnCard.getFaceValue() + " from deck " + leftDeck.getDeckIndex());
	            outputFile.write("\n");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
    }

    // Discards a card from the player's hand to the right deck based on certain rules.
    // Logs the discarded card, deck details, and the player's current hand to the output file.
    public synchronized void discardCard() throws InterruptedException {
        int preferredDenomination = playerIndex;
        denomination = playerIndex;

        Card cardToDiscard = null;
        for (Card card : hand) {
            if (card.getFaceValue() != preferredDenomination) {
                cardToDiscard = card;
                break;
            }
        }

        if (cardToDiscard == null) {
            // If no card with a different denomination was found, discard a random card
            cardToDiscard = hand.get(0);
        }

    	synchronized (rightDeck) {
	        hand.remove(cardToDiscard);
	        rightDeck.insertCardAtBottom(cardToDiscard);
	
	        if (cardToDiscard != null) {
	            try {
	                outputFile.write("player " + playerIndex + " discards a " + cardToDiscard.getFaceValue() + " to deck " + rightDeck.getDeckIndex());
	                outputFile.write("\n");
	                outputFile.write("player " + playerIndex + " current hand is " + getHandContents());
	                outputFile.write("\n");
	                outputFile.write("\n");
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
    	}
    }
    
    // Checks if the player has a winning hand (four cards of the same value).
    public void checkForWin() {
        if (hand.size() == 4) {
            int firstCardDenomination = playerIndex;
            boolean hasFourOfAKind = true;
            for (Card card : hand) {
                if (card.getFaceValue() != firstCardDenomination) {
                    hasFourOfAKind = false;
                    break;
                }
            }

            if (hasFourOfAKind) {
                hasWon = true;
            }
        }
    }
    
    // Declares the player's win, logs the win and exit messages to the output file,
    // sets the winning player, and notifies other players about the win.
    public void declareWin() {
        try {
            outputFile.write("player " + playerIndex + " wins\n");
            outputFile.write("player " + playerIndex + " exits\n");
            outputFile.write("\n");
            
            // Set the winning player and notify other players
            setWinningPlayer(this);
            notifyOtherPlayers();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Declares the player's exit and logs the exit message to the output file.
    private void declareExit() {
        try {
            outputFile.write("player " + playerIndex + " exits\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Prints the initial hand of the player to the output file.
    public void printInitialHand() {
        try {
            outputFile.write("player " + playerIndex + " initial hand: " + getHandContents()+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Sets the winning player and notifies other players about the win.
    // Synchronized to avoid race conditions.
    private synchronized void setWinningPlayer(Player winningPlayer) {
        if (winningPlayer != null) {
            this.winningPlayer = winningPlayer;
            this.hasWon = true;
        }
    }

    
    // Notifies other players about the winning player.
    // Synchronized to avoid race conditions.
    private synchronized void notifyOtherPlayers() {
        for (Player player : players) {
            if (player != this) {
                player.setWinningPlayer(winningPlayer);
            }
        }
    }
    
    // Prints the final hand of the player to the output file, considering whether they won or not.
    public void printFinalHand() {
        try {
            if (hasWon) {
                outputFile.write("player " + playerIndex + " final hand: " + getHandContents());
            } else {
                outputFile.write("player " + playerIndex + " hand: " + getHandContents());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Generates a string representation of the player's hand for logging purposes.
    private String getHandContents() {
        StringBuilder contents = new StringBuilder();
        for (Card card : hand) {
            contents.append(card.getFaceValue()).append(" ");
        }
        return contents.toString().trim();
    }
    
    public List<Card> getHand() {
        return hand;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }
    
    public int getDenomination() {
    	return denomination;
    }

    public Deck getLeftDeck() {
        return leftDeck;
    }

    public boolean hasWon() {
        return hasWon;
    }

}
