/**
 * @fileoverview Creates a live visualization of balls that repel each other
 * and bounce off of the walls.
 */
const HEIGHT_TO_WIDTH = 0.3;
const EFFECT_RADIUS_TO_WIDTH = 0.1;
const BALL_SIZE_TO_WIDTH = 0.01;
const NUM_BALLS = 25;
const INITIAL_BALL_VELOCITY_MAGNITUDE = 1;
/**
 * Runs initial set up of the background and background canvas.
 */
function setup() {
  const backgroundContainer = document.getElementById("background-container");

  const backgroundCanvas =
    createCanvas(backgroundContainer.clientWidth,
      backgroundContainer.clientWidth * HEIGHT_TO_WIDTH);
  backgroundCanvas.parent("background-container");

  frameRate(60);

  initTank();
}

let ballTank;
/**
 * Initializes a tank for the background.
 */
function initTank() {
  const ballSize = BALL_SIZE_TO_WIDTH * width;
  ballTank = new Tank(width, height, EFFECT_RADIUS_TO_WIDTH * width, ballSize);

  // Create balls and add them to the tank.
  // Each start with random positions and velocities.
  for (let i = 0; i < NUM_BALLS; i++) {
    const randomPosition = createVector(Math.random() * ballTank.width,
      Math.random() * ballTank.height);

    // Gets a random vector on the unit circle as to randomize 
    // the initial velocity direction.
    const randomVelocity = p5.Vector.random2D();
    randomVelocity.mult(INITIAL_BALL_VELOCITY_MAGNITUDE);

    const newBall = new Ball(randomPosition, randomVelocity);
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
  const grayScaleColor = 255; // white
  background(grayScaleColor);
  ballTank.update();
  ballTank.drawBalls();
}

/**
 * Tank that holds, updates, and draws balls that repel each other.
 */
class Tank {
  /**
   * @param {number} width
   * @param {number} height
   * @param {number} effectRadius
   * @param {number} ballSize
   */
  constructor(width, height, effectRadius, ballSize) {
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
     * Effective radius of the repulsion effect.
     * @type {number} @const
     */
    this.effectRadius = effectRadius;

    /**
     * Effective diameter of the repulsion effect.
     * @type {number} @const
     */
    this.effectDiameter = effectRadius * 2;

    /**
     * The size of the repelling balls.
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
    this._balls.forEach((ball, i) => ball.update(this));
  }

  /**
   * Draws the balls in the tank.
   */
  drawBalls() {
    this._balls.forEach((ball, i) => ball.draw(this));
  }

  /**
   * Adds a ball to the tank.
   * @param {Ball} ball
   */
  addBall(ball) {
    this._balls.push(ball);
  }

  /**
   * Gets the nearby balls that fall within the ball's effect radius.
   * @param {Ball} ball
   * @return {Array<Ball>}
   */
  getNearbyBalls(ball) {
    const nearbyBalls = [];
    this._balls.forEach((otherBall, i) => {
      // Adds balls that are within the effect radius and not equal to the
      // caller.
      if (ball !== otherBall &&
        p5.Vector.dist(otherBall.position, ball.position) <= this.effectRadius) {
          nearbyBalls.push(otherBall);
        }
    });

    return nearbyBalls;
  }
}

/**
 * A ball that repels other balls and bounces off of walls.
 */
class Ball {
  /**
   * @param {p5.Vector} initialPosition
   * @param {p5.Vector} initialVelocity
   */
  constructor(initialPosition, initialVelocity) {
    /**
     * The position of the ball.
     * @type {p5.Vector}
     */
    this.position = initialPosition;
    /**
     * The velocity of the ball.
     * @type {p5.Vector}
     */
    this.velocity = initialVelocity;
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
    const nearbyBalls = tank.getNearbyBalls(this);

    this.velocity.add(this._getRepellingForce(nearbyBalls));
    this._bounceBounds(tank);

    this.position.add(this.velocity);
  }

  /**
   * Draws the ball.
   * @param  {Tank} tank
   */
  draw(tank) {
    fill(this.color);
    ellipse(this.position.x, this.position.y, tank.ballSize, tank.ballSize);
  }

  /**
   * Checks if ball is out of bounds and invert the velocity to bounce it back.
   * @param {Tank} tank
   * @private
   */
  _bounceBounds (tank) {
    if (this.position.x - tank.ballSize / 2 < 0) {
      this.velocity.x = -this.velocity.x;
    }
    else if (this.position.x + tank.ballSize / 2 > tank.width) {
      this.velocity.x = -this.velocity.x;
    }
    if (this.position.y - tank.ballSize / 2 < 0) {
      this.velocity.y = -this.velocity.y;
    }
    else if (this.position.y + tank.ballSize / 2 > tank.height) {
      this.velocity.y = -this.velocity.y;
    }
  }

  /**
   * Gets repelling force from nearby balls.
   * @param {Array<Ball>} nearbyBalls
   * @return {p5.Vector}
   * @private
   */
  _getRepellingForce(nearbyBalls) {
    const FORCE_BASE_MAGNITUDE = 20;
    const MAX_FORCE = 0.5;

    const repellingForce = createVector(0, 0);

    // For each ball, creates a vector pointing from the nearby ball to this
    // ball with a magnitude equal to the magnitude of the repulsive force.
    // Sums all force vectors to the single repellingForce vector.
    nearbyBalls.forEach((ball, i) => {
      const separationNormal = p5.Vector.sub(this.position, ball.position);
      const distance = separationNormal.mag();

      // Force is related to inverse square of distance, like the
      // electrostatic force.
      let forceMagnitude = FORCE_BASE_MAGNITUDE / Math.pow(distance, 2)
      forceMagnitude = Math.min(forceMagnitude, MAX_FORCE);

      separationNormal.normalize();
      separationNormal.mult(forceMagnitude);

      repellingForce.add(separationNormal);

      this._drawForceLine(forceMagnitude, ball);
    });

    return repellingForce;
  }

  /**
   * Draws a line to represent repulsion force effect.
   * @param {number} forceMagnitude
   * @param {Ball} otherBall
   */
  _drawForceLine(forceMagnitude, otherBall) {
    const STROKE_MAG_MULT = 20;
    const MAX_STROKE_WEIGHT = 4;

    let effectWeight = forceMagnitude * STROKE_MAG_MULT;
    effectWeight = Math.min(effectWeight, MAX_STROKE_WEIGHT);

    // Draws visual connector depending on force magnitude
    stroke(0);
    fill(0);
    strokeWeight(effectWeight);
    line(this.position.x, this.position.y, otherBall.position.x, otherBall.position.y);
    strokeWeight(1);
  }
}
