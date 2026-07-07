const boardEl = document.getElementById("board");
const statusEl = document.getElementById("status");
const resetBtn = document.getElementById("reset");
const scoreEls = {
  X: document.getElementById("scoreX"),
  O: document.getElementById("scoreO"),
  Draw: document.getElementById("scoreDraw"),
};

const WINNING_LINES = [
  [0, 1, 2], [3, 4, 5], [6, 7, 8], // rows
  [0, 3, 6], [1, 4, 7], [2, 5, 8], // columns
  [0, 4, 8], [2, 4, 6],            // diagonals
];

let board = Array(9).fill("");
let currentPlayer = "X";
let gameOver = false;
const scores = { X: 0, O: 0, Draw: 0 };

function createBoard() {
  boardEl.innerHTML = "";
  board.forEach((_, index) => {
    const cell = document.createElement("button");
    cell.className =
      "aspect-square flex items-center justify-center text-5xl font-bold rounded-xl " +
      "bg-white/10 hover:bg-white/20 text-white transition shadow-inner select-none";
    cell.dataset.index = index;
    cell.addEventListener("click", () => handleMove(index));
    boardEl.appendChild(cell);
  });
}

function handleMove(index) {
  if (gameOver || board[index] !== "") return;

  board[index] = currentPlayer;
  const cell = boardEl.children[index];
  cell.textContent = currentPlayer;
  cell.classList.add(currentPlayer === "X" ? "text-cyan-300" : "text-pink-300");

  const winningLine = getWinningLine();
  if (winningLine) {
    endGame(currentPlayer, winningLine);
    return;
  }

  if (board.every((c) => c !== "")) {
    endGame("Draw");
    return;
  }

  currentPlayer = currentPlayer === "X" ? "O" : "X";
  statusEl.textContent = `Player ${currentPlayer}'s turn`;
}

function getWinningLine() {
  return WINNING_LINES.find(
    ([a, b, c]) => board[a] && board[a] === board[b] && board[a] === board[c]
  );
}

function endGame(result, winningLine) {
  gameOver = true;

  if (result === "Draw") {
    statusEl.textContent = "It's a draw!";
    scores.Draw++;
    scoreEls.Draw.textContent = scores.Draw;
    return;
  }

  statusEl.textContent = `Player ${result} wins!`;
  scores[result]++;
  scoreEls[result].textContent = scores[result];

  winningLine.forEach((i) => {
    boardEl.children[i].classList.add("bg-green-500/40");
  });
}

function resetGame() {
  board = Array(9).fill("");
  currentPlayer = "X";
  gameOver = false;
  statusEl.textContent = "Player X's turn";
  createBoard();
}

resetBtn.addEventListener("click", resetGame);
createBoard();
