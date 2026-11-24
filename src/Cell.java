/**
 * A single cell on the Minesweeper board.
 *
 * A cell may contain a mine, may be revealed or still covered, and may be
 * flagged by the player as a suspected mine. For safe cells, the cell also
 * knows how many of its (up to) eight neighbours contain mines.
 */
public class Cell {

    private boolean mine;
    private boolean revealed;
    private boolean flagged;
    private int adjacentMines;

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    /** Toggles the flag on this cell and returns the new flag state. */
    public boolean toggleFlag() {
        flagged = !flagged;
        return flagged;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}
