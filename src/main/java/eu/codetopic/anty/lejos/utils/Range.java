package eu.codetopic.anty.lejos.utils;

public class Range {

    private final double lower;
    private final double higher;
    private final double size;

    public Range(double lower, double higher) {
        this.lower = lower;
        this.higher = higher;
        this.size = higher - lower;
    }

    public double getLower() {
        return lower;
    }

    public double getHigher() {
        return higher;
    }

    public double getSize() {
        return size;
    }

    public boolean contains(double number) {
        return number >= lower && number <= higher;
    }

    @Override
    public String toString() {
        return "Range{" +
                "lower=" + lower +
                ", higher=" + higher +
                ", size=" + size +
                '}';
    }
}
