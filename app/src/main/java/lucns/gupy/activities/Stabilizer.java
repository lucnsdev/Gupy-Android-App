package lucns.gupy.activities;

public class Stabilizer {

    private final int ARRAY_LENGTH = 16;
    private final long[] myArray = new long[ARRAY_LENGTH];
    private int index;

    public long put(long newValue) {
        long value = 0;
        if (index + 1 == ARRAY_LENGTH) {
            for (int i = 1; i < ARRAY_LENGTH; i++) myArray[i - 1] = myArray[i];
            myArray[index] = newValue;
            for (int i = 0; i < ARRAY_LENGTH; i++) value += myArray[i];
        } else {
            myArray[index] = newValue;
            index++;
            for (int i = 0; i < index; i++) value += myArray[i];
        }

        return value / index;
    }

    public void clear() {
        index = 0;
    }

    public boolean isAcceptable() {
        return index >= (ARRAY_LENGTH / 4);
    }

    public boolean isReliable() {
        return index + 1 == ARRAY_LENGTH;
    }

    public long peek() {
        long value = 0;
        for (int i = 0; i < index + 1; i++) value += myArray[i];
        return value / (index + 1);
    }
}
