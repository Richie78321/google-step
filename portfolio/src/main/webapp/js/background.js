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
   * Updates the balls in the tank by one frame.
   */
  update() {
    this._balls.forEach((ball, i) => {
      // Record previous pos to update spatial hashing
      const prevPos = createVector(ball.pos.x, ball.pos.y);
      ball.update(this);
      this._updateHash(prevPos, ball);
    });
  }

  /**
   * Draws the balls in the tank.
   */
  draw() {
    this._balls.forEach((ball, i) => ball.draw(this));
  }

  /**
   * Adds a ball to the tank.
   * @param {Ball} ball
   */
  addBall(ball) {
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
  _getSpatialHashPos(pos) {
    return createVector(Math.floor(pos.x / this.staticDiameter),
      Math.floor(pos.y / this.staticDiameter));
  }

  /**
   * Get balls that could be affected by static effect. This is where spatial
   * hashing is used to optimize.
   * @param {Ball} ball
   * @return {Array<Ball>}
   */
  getNearby(ball) {
    // Approximate circular viewing limits to a square.
    // This approximation holds because the spacial hash is divided into
    // the diameter of the viewing circle, such that the circle can only
    // ever be in four squares at once.

    const x_min =
      p5.Vector.sub(ball.pos, createVector(this.staticRadius, 0));
    const y_min =
      p5.Vector.sub(ball.pos, createVector(0, this.staticRadius));

    const x_min_hash = this._getSpatialHashPos(x_min).x;
    const y_min_hash = this._getSpatialHashPos(y_min).y;

    let nearbyBalls = [];
    for (let i = x_min_hash; i < x_min_hash + 2; i++) {
      for (let j = y_min_hash; j < y_min_hash + 2; j++) {
        const hashCollection = this._spatialHash[i + '-' + j];
        if (hashCollection) {
          nearbyBalls = nearbyBalls.concat(hashCollection);
        }
      }
    }

    const selfIndex = nearbyBalls.indexOf(ball);
    if (selfIndex !== -1) {
      nearbyBalls.splice(selfIndex, 1);
    }

    return nearbyBalls.filter((nearbyBall) => {
      return p5.Vector.dist(ball.pos, nearbyBall.pos) <= this.staticRadius;
    });
  }
}
