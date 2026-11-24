# Minesweeper

A console implementation of the classic Minesweeper game, written in plain
Java with no external dependencies. Clear every safe cell without stepping
on a mine: the numbers tell you how many mines touch that cell, and flags
mark the ones you have figured out.

## Features

- Three classic difficulties (Beginner 9x9 / 10 mines, Intermediate 16x16 /
  40 mines, Expert 16x30 / 99 mines) plus fully custom board sizes
- First click is always safe — mines are placed after your opening move,
  away from the cell you picked
- Flood-fill reveal: opening an empty cell clears the whole connected
  empty area and its numbered border in one move
- Flagging, a live mine counter, and a game timer
- Win and loss detection, with the full board (mines and wrong flags)
  shown when the game ends

## Requirements

- Java 17 or newer (JDK to compile, JRE is not enough on its own)

## Compile and run

From the project root:

```sh
javac -d out src/*.java
java -cp out Minesweeper
```

You can also skip the difficulty menu by passing a board size directly:

```sh
java -cp out Minesweeper 9 9 10
```

## How to play

At the start you pick a difficulty (just press Enter for Beginner). The
board is printed with row numbers down the left side and column numbers
along the top; those are the coordinates you type.

| Command         | Action                          |
| --------------- | ------------------------------- |
| `r <row> <col>` | Reveal a cell                   |
| `f <row> <col>` | Toggle a flag on a cell         |
| `h`             | Show the command list           |
| `q`             | Quit                            |

Example: `r 4 3` reveals the cell at row 4, column 3, and `f 0 8` plants
or removes a flag in the top-right corner of a beginner board.

Board legend:

- `#` — covered cell
- `F` — flag
- `.` — revealed cell with no neighbouring mines
- `1`–`8` — revealed cell, with that many mines next to it
- `*` — a mine (shown when the game ends)
- `X` — a flag that turned out to be wrong (shown when the game ends)

You win by revealing every cell that does not contain a mine. You lose by
revealing a mine.

## Project layout

- `src/Cell.java` — one cell: mine, revealed and flagged state, neighbour count
- `src/Board.java` — the grid and the game rules: mine placement, flood-fill
  reveal, flagging, win/loss detection, text rendering
- `src/Minesweeper.java` — the entry point: difficulty selection, the turn
  loop, input parsing, and the timer

## Tech

Java 17, standard library only.

## License

MIT — see [LICENSE](LICENSE).
