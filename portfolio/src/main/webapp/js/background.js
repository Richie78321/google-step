const HEIGHT_TO_WIDTH = 0.3;
/**
 * Runs initial set up of the game and game canvas
 */
function setup() {
  const backgroundContainer = document.getElementById("background-container");

  const gameCanvas =
    createCanvas(backgroundContainer.clientWidth,
      backgroundContainer.clientWidth * HEIGHT_TO_WIDTH);
  gameCanvas.parent("background-container");
}

/**
 * Resizes the canvas when the window is resized
 */
function windowResized() {
  const canvasContainer = document.getElementById("canvas-container");
  resizeCanvas(canvasContainer.clientWidth,
    canvasContainer.clientWidth * HEIGHT_TO_WIDTH);
}

/**
 * Called every draw loop; used for updating game logic
 * and drawing to the screen
 */
function draw() {
  background(120);
}
