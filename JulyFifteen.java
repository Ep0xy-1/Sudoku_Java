import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class JulyFifteen extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private int[][] board;
    private int[][] solvedBoard; // Stores the complete solution
    private JTextField[][] cells;
    private Random random;

    public JulyFifteen() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        random = new Random();
        board = new int[SIZE][SIZE];
        solvedBoard = new int[SIZE][SIZE];
        cells = new JTextField[SIZE][SIZE];

        initializeGUI();
        initializeGame();
    }

    private void initializeGUI() {
        JPanel mainPanel = new JPanel(new GridLayout(SIZE, SIZE, 2, 2));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.BLACK);

        Font font = new Font("Arial", Font.BOLD, 24);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(font);
                cell.setBorder(createBorder(row, col));
                cells[row][col] = cell;
                mainPanel.add(cell);

                // Add mouse listener for highlighting same numbers
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        highlightSameNumbers(cell);
                    }
                });
            }
        }

        JButton newGameBtn = new JButton("New Game");
        newGameBtn.addActionListener(e -> resetGame());

        JButton checkBtn = new JButton("Check Solution");
        checkBtn.addActionListener(e -> checkSolution());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(newGameBtn);
        buttonPanel.add(checkBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private Border createBorder(int row, int col) {
        int top = (row % SUBGRID_SIZE == 0) ? 2 : 0;
        int left = (col % SUBGRID_SIZE == 0) ? 2 : 0;
        int bottom = (row == SIZE - 1) ? 2 : 1;
        int right = (col == SIZE - 1) ? 2 : 1;

        return BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK);
    }

    private void initializeGame() {
        fillDiagonalSubgrids();
        solvePuzzle(0, 0);
        createPuzzle(40);
        updateBoardUI();
    }

    private void fillDiagonalSubgrids() {
        for (int i = 0; i < SIZE; i += SUBGRID_SIZE) {
            fillSingleSubgrid(i, i);
        }
    }

    private void fillSingleSubgrid(int row, int col) {
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                int num;
                do {
                    num = random.nextInt(SIZE) + 1;
                } while (!isValidPlacement(row + i, col + j, num));
                board[row + i][col + j] = num;
            }
        }
    }

    private boolean solvePuzzle(int row, int col) {
        if (row == SIZE) {
            return true;
        }

        if (col == SIZE) {
            return solvePuzzle(row + 1, 0);
        }

        if (board[row][col] != 0) {
            return solvePuzzle(row, col + 1);
        }

        for (int num = 1; num <= SIZE; num++) {
            if (isValidPlacement(row, col, num)) {
                board[row][col] = num;
                if (solvePuzzle(row, col + 1)) {
                    // Store the solved board
                    System.arraycopy(board[row], 0, solvedBoard[row], 0, SIZE);
                    return true;
                }
                board[row][col] = 0;
            }
        }
        return false;
    }

    private void createPuzzle(int cellsToRemove) {
        int count = 0;
        while (count < cellsToRemove) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                count++;
            }
        }
    }

    private boolean isValidPlacement(int row, int col, int num) {
        return !isInRow(row, num) && !isInColumn(col, num) && !isInSubgrid(row - row % SUBGRID_SIZE, col - col % SUBGRID_SIZE, num);
    }

    private boolean isInRow(int row, int num) {
        for (int col = 0; col < SIZE; col++) {
            if (board[row][col] == num) {
                return true;
            }
        }
        return false;
    }

    private boolean isInColumn(int col, int num) {
        for (int row = 0; row < SIZE; row++) {
            if (board[row][col] == num) {
                return true;
            }
        }
        return false;
    }

    private boolean isInSubgrid(int startRow, int startCol, int num) {
        for (int row = 0; row < SUBGRID_SIZE; row++) {
            for (int col = 0; col < SUBGRID_SIZE; col++) {
                if (board[startRow + row][startCol + col] == num) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateBoardUI() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JTextField cell = cells[row][col];
                if (board[row][col] != 0) {
                    cell.setText(Integer.toString(board[row][col]));
                    cell.setEditable(false);
                    cell.setBackground(new Color(240, 240, 240));
                } else {
                    cell.setText("");
                    cell.setEditable(true);
                    cell.setBackground(Color.WHITE);
                }
            }
        }
    }

    private void resetGame() {
        board = new int[SIZE][SIZE];
        solvedBoard = new int[SIZE][SIZE];
        initializeGame();
    }

    private void checkSolution() {
        boolean allCorrect = true;
        StringBuilder incorrectCells = new StringBuilder();

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = cells[row][col].getText();
                if (text.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please fill in all cells!",
                            "Incomplete",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int userInput = Integer.parseInt(text);
                    if (userInput != solvedBoard[row][col]) {
                        highlightConflict(row, col);
                        incorrectCells.append("Row ").append(row+1)
                                .append(", Column ").append(col+1)
                                .append(": Should be ").append(solvedBoard[row][col])
                                .append("\n");
                        allCorrect = false;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter numbers only (1-9)!",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (allCorrect) {
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Your solution is correct!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Incorrect cells:\n" + incorrectCells.toString(),
                    "Solution Check",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void highlightSameNumbers(JTextField clickedCell) {
        String text = clickedCell.getText();
        if (!text.isEmpty()) {
            // Reset all backgrounds first
            resetCellBackgrounds();

            // Highlight matching numbers
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if (cells[row][col].getText().equals(text)) {
                        cells[row][col].setBackground(new Color(173, 216, 230)); // Light blue
                    }
                }
            }
        }
    }

    private void resetCellBackgrounds() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] != 0) {
                    cells[row][col].setBackground(new Color(240, 240, 240));
                } else {
                    cells[row][col].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void highlightConflict(int row, int col) {
        cells[row][col].setBackground(Color.PINK);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JulyFifteen game = new JulyFifteen();
            game.setVisible(true);
        });
    }
}
