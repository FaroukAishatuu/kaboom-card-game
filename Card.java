public class Card {
    private int faceValue;

    public Card(int faceValue) {
        this.faceValue = faceValue;
    }

    public int getFaceValue() {
        return faceValue;
    }

    @Override
    public String toString() {
        return Integer.toString(faceValue);
    }
}
