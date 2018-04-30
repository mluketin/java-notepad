package hr.fer.zemris.ooup.lab3;

public class Location {
    private int row;
    private int column;

    public Location(int row, int column) {
        this.row = row;
        this.column = column;
    }


    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Creates new Location object with field values same as original object
     */
    public Location clone() {
        return new Location(row, column);
    }

    public boolean isLesserThan(Location loc) {
        if (row < loc.getRow())
            return true;

        if (row == loc.getRow()) {
            if (column <= loc.getColumn())
                return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Location other = (Location) obj;
        if (column != other.column)
            return false;
        if (row != other.row)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Location [row=" + row + ", column=" + column + "]";
    }


}
