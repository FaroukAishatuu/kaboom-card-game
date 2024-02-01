import java.util.ArrayList;
import java.util.List;

public class Deck {
    private int deckIndex;
    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
    }

    // Inserts a card at the bottom of the deck, synchronized to prevent race conditions.
    public synchronized void insertCardAtBottom(Card card) {
    	synchronized(cards) {
    		cards.add(card);
    	}
    }
    
    // Draws a card from the top of the deck, synchronized to prevent race conditions.
    public synchronized Card drawCardFromTop() {
    	synchronized(cards) {
	        if (!cards.isEmpty()) {
	            return cards.remove(0);
	        }
	        return null; // Handle empty deck
    	}
    }

    public boolean isEmpty() {
    	synchronized(cards) {
    		return cards.isEmpty();
    	}
    }

    public List<Card> getDeckContents() {
    	synchronized(cards) {
    		return new ArrayList<>(cards);
    	}
    }
    
    public int getDeckIndex() {
    	return deckIndex;
    }

    public void setDeckIndex(int deckIndex) {
    	this.deckIndex = deckIndex;
    }
    
    public int size() {
    	synchronized(cards) {
    		return cards.size();
    	}
    }
}
