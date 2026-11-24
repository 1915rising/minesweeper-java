import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * The Minesweeper board: a rectangular grid of cells plus the rules for
 * placing mines, revealing cells (with flood fill over empty areas),
 * flagging, and detecting a win or a loss.
 *
 * Mines are placed lazily on the first reveal, and the first clicked cell
 * (plus its neighbours, when the board is big enough) is always kept safe,
 * so the opening move can never lose the game.
 */
public class Board {

    /** Lifecycle of a game played on this board. */
    public enum Status {
        IN_PROGRESS,
        WON,
        LOST
    }

    private final int rows;
    private final int cols;
    private final int mineCount;
    private final Cell[][] cells;

    private boolean minesPlaced;
    private int revealedCount;
    private int flagCount;
    private Status status = Status.IN_PROGRESS;

    /**
     * Creates a board. Mines are not placed until the first reveal.
     *
     * @throws IllegalArgumentException if the dimensions are unusable or the
     *                                  mine count leaves no safe cells
     */
    public Board(int rows, int cols, int mineCount) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("Board must have at least 1 row and 1 column.");
        }
        if (mineCount < 1 || mineCount >= rows * cols) {
            throw new IllegalArgumentException(
                    "Mine count must be between 1 and " + (rows * cols - 1) + " for this board size.");
        }
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMineCount() {
        return mineCount;
    }

    public Status getStatus() {
        return status;
    }

    /** Mines not yet accounted for by flags (may go negative if over-flagged). */
    public int getMinesRemaining() {
        return mineCount - flagCount;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Reveals the cell at (row, col). Revealing a cell with no neighbouring
     * mines flood-fills outward over the connected empty area and its
     * numbered border. Revealing a mine loses the game; revealing every safe
     * cell wins it. Flagged or already-revealed cells are left alone.
     */
    public void reveal(int row, int col) {
        if (status != Status.IN_PROGRESS || !inBounds(row, col)) {
            return;
        }
        Cell start = cells[row][col];
        if (start.isFlagged() || start.isRevealed()) {
            return;
        }
        if (!minesPlaced) {
            placeMines(row, col);
            minesPlaced = true;
        }
        if (start.isMine()) {
            start.setRevealed(true);
            status = Status.LOST;
            return;
        }

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{row, col});
        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            Cell cell = cells[pos[0]][pos[1]];
            if (cell.isRevealed() || cell.isFlagged() || cell.isMine()) {
                continue;
            }
            cell.setRevealed(true);
            revealedCount++;
            if (cell.getAdjacentMines() == 0) {
                for (int[] n : neighbours(pos[0], pos[1])) {
                    if (!cells[n[0]][n[1]].isRevealed()) {
                        stack.push(n);
                    }
                }
            }
        }

        if (revealedCount == rows * cols - mineCount) {
            status = Status.WON;
        }
    }

    /**
     * Toggles a flag on the cell at (row, col).
     *
     * @return true if the cell is flagged after the call, false otherwise
     *         (including when the cell was already revealed)
     */
    public boolean toggleFlag(int row, int col) {
        if (status != Status.IN_PROGRESS || !inBounds(row, col)) {
            return false;
        }
        Cell cell = cells[row][col];
        if (cell.isRevealed()) {
            return cell.isFlagged();
        }
        boolean nowFlagged = cell.toggleFlag();
        flagCount += nowFlagged ? 1 : -1;
        return nowFlagged;
    }

    /** Places mines everywhere except the first clicked cell and, if the board allows, its neighbours. */
    private void placeMines(int safeRow, int safeCol) {
        List<int[]> candidates = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) {
                    continue; // keep the opening area safe
                }
                candidates.add(new int[]{r, c});
            }
        }
        if (candidates.size() < mineCount) {
            // Very dense board: only the clicked cell itself can be guaranteed safe.
            candidates.clear();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (r != safeRow || c != safeCol) {
                        candidates.add(new int[]{r, c});
                    }
                }
            }
        }
        Collections.shuffle(candidates);
        for (int i = 0; i < mineCount; i++) {
            int[] pos = candidates.get(i);
            cells[pos[0]][pos[1]].setMine(true);
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!cells[r][c].isMine()) {
                    cells[r][c].setAdjacentMines(countNeighbouringMines(r, c));
                }
            }
        }
    }

    private int countNeighbouringMines(int row, int col) {
        int count = 0;
        for (int[] n : neighbours(row, col)) {
            if (cells[n[0]][n[1]].isMine()) {
                count++;
            }
        }
        return count;
    }

    /** Returns the in-bounds neighbour coordinates of a cell (3 to 8 of them). */
    private List<int[]> neighbours(int row, int col) {
        List<int[]> result = new ArrayList<>(8);
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int r = row + dr;
                int c = col + dc;
                if (inBounds(r, c)) {
                    result.add(new int[]{r, c});
                }
            }
        }
        return result;
    }

    /**
     * Renders the board as text. Coordinates are printed along the top and
     * left edges and match the coordinates used by the reveal/flag commands.
     * When the game is over, mines and wrongly placed flags are shown.
     */
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("    ");
        for (int c = 0; c < cols; c++) {
            sb.append(String.format("%-3d", c));
        }
        sb.append(System.lineSeparator());
        for (int r = 0; r < rows; r++) {
            sb.append(String.format("%-4d", r));
            for (int c = 0; c < cols; c++) {
                sb.append(displayChar(cells[r][c])).append("  ");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private char displayChar(Cell cell) {
        boolean gameOver = status != Status.IN_PROGRESS;
        if (cell.isRevealed()) {
            if (cell.isMine()) {
                return '*';
            }
            return cell.getAdjacentMines() == 0 ? '.' : Character.forDigit(cell.getAdjacentMines(), 10);
        }
        if (gameOver && cell.isMine()) {
            if (status == Status.WON) {
                return 'F'; // every mine is accounted for
            }
            return cell.isFlagged() ? 'F' : '*'; // loss: expose unflagged mines
        }
        if (gameOver && cell.isFlagged() && !cell.isMine()) {
            return 'X'; // a flag that was wrong
        }
        if (cell.isFlagged()) {
            return 'F';
        }
        return '#';
    }
}
