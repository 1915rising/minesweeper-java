import java.util.Scanner;

/**
 * Minesweeper — a console implementation of the classic game.
 *
 * Pick a difficulty (or pass rows, columns and mines on the command line),
 * then reveal cells and plant flags until every safe cell is uncovered.
 * The first reveal is always safe. The clock starts on that first reveal.
 *
 * Commands:
 *   r &lt;row&gt; &lt;col&gt;   reveal a cell
 *   f &lt;row&gt; &lt;col&gt;   toggle a flag on a cell
 *   h               show the command list
 *   q               quit
 */
public class Minesweeper {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        int[] config = parseArgs(args);
        if (config == null) {
            config = chooseDifficulty(in);
        }
        if (config == null) {
            System.out.println("Goodbye.");
            return;
        }

        Board board = new Board(config[0], config[1], config[2]);
        play(in, board);
    }

    /** Runs the turn loop until the game is won, lost, or abandoned. */
    private static void play(Scanner in, Board board) {
        System.out.println();
        System.out.println("Board: " + board.getRows() + "x" + board.getCols()
                + " with " + board.getMineCount() + " mines.");
        printHelp();

        long startMillis = -1;
        long endMillis = -1;

        while (board.getStatus() == Board.Status.IN_PROGRESS) {
            System.out.println(board.render());
            System.out.println(statusLine(board, startMillis));
            System.out.print("> ");

            if (!in.hasNextLine()) {
                System.out.println(); // EOF on input: leave cleanly
                return;
            }
            String line = in.nextLine().trim().toLowerCase();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0];

            switch (command) {
                case "q":
                case "quit":
                case "exit":
                    System.out.println("Game abandoned. Goodbye.");
                    return;
                case "h":
                case "help":
                    printHelp();
                    break;
                case "r":
                case "reveal":
                case "f":
                case "flag": {
                    int[] target = parseCoordinates(parts, board);
                    if (target == null) {
                        break;
                    }
                    if (command.equals("r") || command.equals("reveal")) {
                        board.reveal(target[0], target[1]);
                        if (startMillis < 0) {
                            startMillis = System.currentTimeMillis();
                        }
                    } else {
                        board.toggleFlag(target[0], target[1]);
                    }
                    break;
                }
                default:
                    System.out.println("Unknown command \"" + command + "\". Type h for help.");
            }
        }

        endMillis = System.currentTimeMillis();
        System.out.println(board.render());
        long seconds = startMillis < 0 ? 0 : (endMillis - startMillis) / 1000;
        if (board.getStatus() == Board.Status.WON) {
            System.out.println("You win! All safe cells revealed in " + seconds + " seconds.");
        } else {
            System.out.println("Boom! You hit a mine after " + seconds + " seconds. Game over.");
        }
    }

    private static String statusLine(Board board, long startMillis) {
        long seconds = startMillis < 0 ? 0 : (System.currentTimeMillis() - startMillis) / 1000;
        return "Mines left: " + board.getMinesRemaining() + " | Time: " + seconds + "s";
    }

    private static void printHelp() {
        System.out.println("Commands:  r <row> <col>  reveal a cell");
        System.out.println("           f <row> <col>  toggle a flag");
        System.out.println("           h              this help");
        System.out.println("           q              quit");
        System.out.println("Legend:    # covered   F flag   . empty   1-8 neighbouring mines   * mine");
    }

    /**
     * Parses "row col" out of a command line, validating the numbers against
     * the board. Returns null (after printing why) when the input is unusable.
     */
    private static int[] parseCoordinates(String[] parts, Board board) {
        if (parts.length != 3) {
            System.out.println("Usage: " + parts[0] + " <row> <col>");
            return null;
        }
        int row;
        int col;
        try {
            row = Integer.parseInt(parts[1]);
            col = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("Row and column must be whole numbers.");
            return null;
        }
        if (!board.inBounds(row, col)) {
            System.out.println("Out of bounds: row must be 0-" + (board.getRows() - 1)
                    + " and column 0-" + (board.getCols() - 1) + ".");
            return null;
        }
        return new int[]{row, col};
    }

    /**
     * Reads an optional "rows cols mines" triple from the command line.
     * Returns null when no (or invalid) arguments were supplied, in which
     * case the interactive difficulty menu is used instead.
     */
    private static int[] parseArgs(String[] args) {
        if (args.length == 0) {
            return null;
        }
        if (args.length != 3) {
            System.out.println("Usage: java Minesweeper [rows cols mines]");
            return null;
        }
        try {
            int rows = Integer.parseInt(args[0]);
            int cols = Integer.parseInt(args[1]);
            int mines = Integer.parseInt(args[2]);
            new Board(rows, cols, mines); // validate before committing to it
            return new int[]{rows, cols, mines};
        } catch (NumberFormatException e) {
            System.out.println("Rows, columns and mines must be whole numbers.");
            return null;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid board: " + e.getMessage());
            return null;
        }
    }

    /**
     * Interactive difficulty menu. Beginner (9x9, 10 mines) is the default.
     * Returns null on EOF or quit.
     */
    private static int[] chooseDifficulty(Scanner in) {
        System.out.println("MINESWEEPER");
        System.out.println("Choose a difficulty:");
        System.out.println("  b  Beginner      9x9,   10 mines (default)");
        System.out.println("  i  Intermediate  16x16, 40 mines");
        System.out.println("  e  Expert        16x30, 99 mines");
        System.out.println("  c  Custom");
        System.out.print("> ");

        if (!in.hasNextLine()) {
            return new int[]{9, 9, 10};
        }
        String choice = in.nextLine().trim().toLowerCase();
        switch (choice) {
            case "i":
                return new int[]{16, 16, 40};
            case "e":
                return new int[]{16, 30, 99};
            case "c":
                return readCustomBoard(in);
            case "q":
                return null;
            case "b":
            case "":
            default:
                return new int[]{9, 9, 10};
        }
    }

    private static int[] readCustomBoard(Scanner in) {
        while (true) {
            System.out.print("Rows, columns and mines (e.g. 12 12 24), or q to quit: ");
            if (!in.hasNextLine()) {
                return null;
            }
            String line = in.nextLine().trim().toLowerCase();
            if (line.equals("q")) {
                return null;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Please enter exactly three numbers.");
                continue;
            }
            try {
                int rows = Integer.parseInt(parts[0]);
                int cols = Integer.parseInt(parts[1]);
                int mines = Integer.parseInt(parts[2]);
                new Board(rows, cols, mines); // validate
                return new int[]{rows, cols, mines};
            } catch (NumberFormatException e) {
                System.out.println("Rows, columns and mines must be whole numbers.");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid board: " + e.getMessage());
            }
        }
    }
}
