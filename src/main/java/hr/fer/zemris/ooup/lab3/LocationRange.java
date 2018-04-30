package hr.fer.zemris.ooup.lab3;

public class LocationRange {
    private Location begin;
    private Location end;

    public LocationRange(Location begin, Location end) {
        this.begin = begin;
        this.end = end;
    }

    public static LocationRange copy(LocationRange locationRange) {
        if (locationRange == null)
            return null;

        return new LocationRange(locationRange.getBegin().clone(), locationRange.getEnd().clone());
    }

    public void setBegin(Location begin) {
        this.begin = begin;
    }

    public void setEnd(Location end) {
        this.end = end;
    }

    public Location getBegin() {
        return begin;
    }

    public Location getEnd() {
        return end;
    }
}
