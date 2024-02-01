import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class CardGame {

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Please enter the number of players:");
        int numPlayers = scanner.nextInt();

        // Validate the number of players
        if (numPlayers <= 0) {
            System.out.println("Invalid number of players. Please enter a positive integer.");
            return;
        }

        System.out.println("Please enter the location of the pack to load:");
        String packFileName = scanner.next();

        // Read and validate the input pack
        List<Integer> pack = readInputPack(packFileName, numPlayers);

        if (pack == null) {
            System.out.println("Invalid pack file. Please provide a valid pack file.");
            return;
        }

        // Now can proceed with distributing hands, filling decks, and starting threads.

        try {
        	// Create player and deck instances.
	        List<Player> players = new ArrayList<>(numPlayers);
	        List<Deck> decks = new ArrayList<>(numPlayers);
	        
	        
	        // Create a new decks
	        for (int i = 0; i < numPlayers; i++) {
	            decks.add(new Deck());
	            decks.get(i).setDeckIndex(i+1);
	        }
	        
	        // Creates a new Player object with the player's number, their own deck, the deck of the next player (for card passing), the FileWriter, and the list of all players.
			for (int i = 0; i < numPlayers; i++) {
	            FileWriter playerOutput = new FileWriter("player" + (i + 1) + "_output.txt");
	            Player player = new Player(i + 1, decks.get(i), decks.get((i + 1) % numPlayers), playerOutput, players);
	            players.add(player);
	        }
			
            // Distribute hands and fill decks.
            distributeHands(players, pack);
            
            // Create and start player threads.
            for (Player player : players) {
                player.start();
            }
            
            // Wait for all player threads to finish.
            for (Player player : players) {
                player.join();
            }
            
            // Check for winning player and update the terminal output.
            Player winningPlayer = getWinningPlayer(players);
            if (winningPlayer != null) {
                System.out.println("player " + winningPlayer.getPlayerIndex() + " wins");
            }
            
            // Create deck output files.
            for (int i = 0; i < numPlayers; i++) {
                FileWriter deckOutput = new FileWriter("deck" + (i + 1) + "_output.txt");
                deckOutput.write("deck" + (i + 1) + " contents: " + decks.get(i).getDeckContents());
                deckOutput.close();
            }

            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        scanner.close();
    }

    private static List<Integer> readInputPack(String fileName, int numPlayers) {
        List<Integer> pack = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;

            // Read the pack file and add values to the pack list
            while ((line = reader.readLine()) != null) {
                int cardValue = Integer.parseInt(line);
                pack.add(cardValue);
            }

            reader.close();

            // Validate the pack size has 8n rows
            if (pack.size() % 8 != 0) {
                return null; // Invalid pack size
            }
        } catch (IOException | NumberFormatException e) {
            return null; // File not found or invalid values in the pack
        }

        return pack;
    }
    
    private static void distributeHands(List<Player> players, List<Integer> pack) {
        int numPlayers = players.size();
        int cardsPerPlayer = 4;

        // Distribute hands to players
        for (int i = 0; i < cardsPerPlayer; i++) {
            for (int j = 0; j < numPlayers; j++) {
                int cardValue = pack.remove(0); // Remove and get the first card from the pack
                players.get(j).getHand().add(new Card(cardValue));
            }
        }

        // Fill decks from the remaining pack in a round-robin fashion
        int currentDeckIndex = 0;
        for (int cardValue : pack) {
            players.get(currentDeckIndex).getLeftDeck().insertCardAtBottom(new Card(cardValue));
            currentDeckIndex = (currentDeckIndex + 1) % numPlayers;
        }
    }
    
    // checks each player's hand in a card game to determine the winner.
    private static Player getWinningPlayer(List<Player> players) {
        for (Player player : players) {
            List<Card> hand = player.getHand();
            if (hand.size() == 4 && allCardsHaveSameValue(hand)) {
                return player;
            }
        }
        return null;
    }
    
    // checks if all cards in a given hand have the same face value.
    private static boolean allCardsHaveSameValue(List<Card> hand) {
        int firstCardValue = hand.get(0).getFaceValue();
        for (Card card : hand) {
            if (card.getFaceValue() != firstCardValue) {
                return false;
            }
        }
        return true;
    }

}
