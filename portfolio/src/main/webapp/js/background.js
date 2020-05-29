const HEIGHT_TO_WIDTH = 0.3;
/**
 * Runs initial set up of the game and game canvas.
 */
function setup() {
  const FRAMERATE = 60;
  const backgroundContainer = document.getElementById("background-container");

  const gameCanvas =
    createCanvas(backgroundContainer.clientWidth,
      backgroundContainer.clientWidth * HEIGHT_TO_WIDTH);
  gameCanvas.parent("background-container");

  frameRate(FRAMERATE);
}

/**
 * Resizes the canvas when the window is resized.
 */
function windowResized() {
  const canvasContainer = document.getElementById("canvas-container");
  resizeCanvas(canvasContainer.clientWidth,
    canvasContainer.clientWidth * HEIGHT_TO_WIDTH);
}

/**
 * Called every draw loop; used for updating game logic
 * and drawing to the screen.
 */
function draw() {
  background(120);
}

/**
 * Tank that holds, updates, and draws statically-charged balls.
 */
class Tank {
  /**
   * @param {number} width
   * @param {number} height
   * @param {number} staticRadius
   * @param {number} ballSize
   */
  constructor(width, height, staticRadius, ballSize) {
    /**
     * Height of the tank.
     * @type {number} @const
     */
    this.height = height;

    /**
     * Width of the tank.
     * @type {number} @const
     */
    this.width = width;

    /**
     * Effective radius of static effect.
     * @type {number} @const
     */
    this.staticRadius = staticRadius;

    /**
     * Effective diameter of static effect.
     * @type {number} @const
     */
    this.staticDiameter = staticRadius * 2;

    /**
     * The size of the static balls.
     * @type {number} @const
     */
    this.ballSize = ballSize;

    /**
     * Spatial hashing grid used to reduce calls to balls
     * beyond effective radius.
     * @type {Object.<string, Array<Ball>>} @private
     */
    this._spatialHash = {};

    /**
     * Array of statically-charged balls.
     * @type {Array<Ball>} @private
     */
    this._balls = [];
  }

  /**
   * Adds a ball to the tank.
   * @param {Ball} ball
   */
  addBoid(ball) {
    this._balls.push(ball);
    this._placeInHash(this._getSpatialHashPos(ball.pos), ball);
  }

  /**
   * Updates the spatial hashing position of a ball.
   * @param {p5.Vector} prevPos
   * @param {Ball} ball
   * @private
   */
  _updateHash(prevPos, ball) {
    const prevHashPos = this._getSpatialHashPos(prevPos);
    const currentHashPos = this._getSpatialHashPos(ball.pos);
    if (!prevHashPos.equals(currentHashPos)) {
      if (!this._removeFromHash(prevHashPos, ball)) {
        throw "Invalid previous ball position";
      }
      this._placeInHash(currentHashPos, ball);
    }
  }

  /**
   * Places a ball in spatial hash.
   * @param {p5.Vector} hashPos
   * @param {Ball} ball
   * @private
   */
  _placeInHash(hashPos, ball) {
    const hashKey = hashPos.x + "-" + hashPos.y;

    if (!this._spatialHash[hashKey]) this._spatialHash[hashKey] = [];
    this._spatialHash[hashKey].push(ball);
  }

  /**
   * Removes a ball from the spatial hash.
   * @param {p5.Vector} hashPos
   * @param {Ball} ball
   * @return {boolean}
   * @private
   */
  _removeFromHash(hashPos, ball) {
    const hashKey = hashPos.x + "-" + hashPos.y;

    if (this._spatialHash[hashKey]) {
      const ballIndex = this._spatialHash[hashKey].indexOf(ball);
      if (ballIndex !== -1) {
        this._spatialHash[hashKey].splice(ballIndex, 1);
        return true;
      }
    }

    return false;
  }

  /**
   * Gets the spatial hashing position of a ball
   * @param {p5.Vector} pos
   * @return {p5.Vector}
   * @private
   */
  _getSpacialHashPos(pos) {
    return createVector(Math.floor(pos.x / this.staticDiameter),
      Math.floor(pos.y / this.staticDiameter));
  }
}
