const HEIGHT_TO_WIDTH = 0.3;
/**
 * Runs initial set up of the background and background canvas.
 */
function setup() {
  const FRAMERATE = 60;
  const backgroundContainer = document.getElementById("background-container");

  const backgroundCanvas =
    createCanvas(backgroundContainer.clientWidth,
      backgroundContainer.clientWidth * HEIGHT_TO_WIDTH);
  backgroundCanvas.parent("background-container");

  frameRate(FRAMERATE);

  initTank();
}

let ballTank;
/**
 * Initializes a tank for the background.
 */
function initTank() {
  const EFFECT_RAD_TO_WIDTH = 0.1;
  const BALL_SIZE_TO_WIDTH = 0.01;
  const NUM_BALLS = 25;
  const INIT_BALL_VEL = 1;

  const ballSize = BALL_SIZE_TO_WIDTH * width;
  ballTank = new Tank(width, height, EFFECT_RAD_TO_WIDTH * width, ballSize);

  for (let i = 0; i < NUM_BALLS; i++) {
    const randomPos = createVector(Math.random() * ballTank.width,
      Math.random() * ballTank.height);

    const randomVel = p5.Vector.random2D();
    randomVel.mult(INIT_BALL_VEL);

    const newBall = new Ball(randomPos, randomVel);
    ballTank.addBall(newBall);
  }
}

/**
 * Resizes the canvas when the window is resized.
 */
function windowResized() {
  const canvasContainer = document.getElementById("background-container");
  resizeCanvas(canvasContainer.clientWidth,
    canvasContainer.clientWidth * HEIGHT_TO_WIDTH);
}

/**
 * Called every draw loop; used for updating game logic
 * and drawing to the screen.
 */
function draw() {
  background(255);
  ballTank.update();
  ballTank.draw();
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

    // Minimum x and y values are found at the left and top edges of the circle.
    const xMin =
      p5.Vector.sub(ball.pos, createVector(this.staticRadius, 0));
    const yMin =
      p5.Vector.sub(ball.pos, createVector(0, this.staticRadius));

    const xMinHash = this._getSpatialHashPos(xMin).x;
    const yMinHash = this._getSpatialHashPos(yMin).y;

    // Can only ever occupy four squares, so iterate over
    // two in either direction.
    let nearbyBalls = [];
    for (let i = xMinHash; i < xMinHash + 2; i++) {
      for (let j = yMinHash; j < yMinHash + 2; j++) {
        const hashCollection = this._spatialHash[i + '-' + j];
        if (hashCollection) {
          nearbyBalls = nearbyBalls.concat(hashCollection);
        }
      }
    }

    // Do not count self in nearby
    const selfIndex = nearbyBalls.indexOf(ball);
    if (selfIndex !== -1) {
      nearbyBalls.splice(selfIndex, 1);
    }
    else throw "Self not present in spatial hashing. Error in spatial hashing.";

    // Only include balls that are actually within the effective radius
    return nearbyBalls.filter((nearbyBall) => {
      return p5.Vector.dist(ball.pos, nearbyBall.pos) <= this.staticRadius;
    });
  }
}

/**
 * A statically-charged ball that applies forces to other balls and bounces.
 */
class Ball {
  /**
   * @param {p5.Vector} initialPos
   * @param {p5.Vector} initialVel
   */
  constructor(initialPos, initialVel) {
    /**
     * The position of the ball.
     * @type {p5.Vector}
     */
    this.pos = initialPos;
    /**
     * The velocity of the ball.
     * @type {p5.Vector}
     */
    this.vel = initialVel;
    /**
     * The color of the ball.
     * @type {p5.Color} @const
     */
    this.color = color(Math.floor(Math.random() * 256),
      Math.floor(Math.random() * 256),
      Math.floor(Math.random() * 256));
  }

  /**
   * Updates the velocity and position of the ball.
   * @param {Tank} tank
   */
  update(tank) {
    const nearbyBalls = tank.getNearby(this);

    this.vel.add(this._getRepellingForce(nearbyBalls));
    this._bounceBounds(tank);

    this.pos.add(this.vel);
  }

  /**
   * Draws the ball.
   * @param  {Tank} tank
   */
  draw(tank) {
    fill(this.color);
    ellipse(this.pos.x, this.pos.y, tank.ballSize, tank.ballSize);
  }

  /**
   * Check if ball is out of bounds and invert the velocity to bounce it back.
   * @param {Tank} tank
   * @private
   */
  _bounceBounds (tank) {
    if (this.pos.x - tank.ballSize / 2 < 0) {
      this.vel.x = -this.vel.x;
    }
    else if (this.pos.x + tank.ballSize / 2 > tank.width) {
      this.vel.x = -this.vel.x;
    }
    if (this.pos.y - tank.ballSize / 2 < 0) {
      this.vel.y = -this.vel.y;
    }
    else if (this.pos.y + tank.ballSize / 2 > tank.height) {
      this.vel.y = -this.vel.y;
    }
  }

  /**
   * Get repelling force from nearby balls
   * @param {Array<Ball>} nearbyBalls
   * @return {p5.Vector}
   * @private
   */
  _getRepellingForce(nearbyBalls) {
    const FORCE_BASE_MAGNITUDE = 20;
    const MAX_FORCE = 0.5;

    const repellingForce = createVector(0, 0);

    nearbyBalls.forEach((ball, i) => {
      const separationNormal = p5.Vector.sub(this.pos, ball.pos);
      const distance = separationNormal.mag();

      // Inverse-square law for force
      let forceMagnitude = FORCE_BASE_MAGNITUDE / Math.pow(distance, 2)
      forceMagnitude = Math.min(forceMagnitude, MAX_FORCE);

      separationNormal.normalize();
      separationNormal.mult(forceMagnitude);

      repellingForce.add(separationNormal);

      this._drawStaticLine(forceMagnitude, ball);
    });

    return repellingForce;
  }

  /**
   * Draw a line to represent static force effect
   * @param {number} forceMagnitude
   * @param {Ball} otherBall
   */
  _drawStaticLine(forceMagnitude, otherBall) {
    const STROKE_MAG_MULT = 20;
    const MAX_STROKE_WEIGHT = 4;

    let effectWeight = forceMagnitude * STROKE_MAG_MULT;
    effectWeight = Math.min(effectWeight, MAX_STROKE_WEIGHT);

    // Draw visual connector depending on force magnitude
    stroke(0);
    fill(0);
    strokeWeight(effectWeight);
    line(this.pos.x, this.pos.y, otherBall.pos.x, otherBall.pos.y);
    strokeWeight(1);
  }
}
