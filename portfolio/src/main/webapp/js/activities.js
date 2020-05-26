let scroller = null;
/**
 * Gathers repos from GitHub and scrolls through them in the About Me section
 */
async function startActivitiesScroll() {
  let repos = await fetch("https://api.github.com/users/Richie78321/repos")
    .then(res => res.json());
  repos =
    repos.filter(repo => repo.description && repo.description.length !== 0);

  scroller = new Scroller(repos);
  scroller.start();
}

/**
 * Scrolls through a set of GitHub repository descriptions at a set speed.
 */
class Scroller {
  /**
   * @param {Array<Object>} repos 
   */
  constructor(repos) {
    /**
     * Collection of user GitHub repos
     * @type {Array<Object>}
     */
    this.repos = repos;

    /** @private */
    this._index = 0;

    /** @private */
    this._timeoutID = null;;
  }

  /**
   * Increments the scrolling activity 
   * @private
   */
  _scrollActivity() {
    const NEXT_TIMEOUT = 5000;
    const repo = this.repos[this._index];

    const activityContainer = document.getElementById('activity-scroller');
    activityContainer.innerText = repo.description;

    // Increment index and wait to update scroller
    this._index++;
    this._index %= this.repos.length;
    this._timeoutID =
      setTimeout(() => { this._scrollActivity(); }, NEXT_TIMEOUT);
  }

  /**
   * Starts the scrolling effect
   */
  start() {
    if (this.isPaused()) {
      this._scrollActivity();
    }
  }


  /**
   * Pauses the scrolling effect 
   */
  pause() {
    if (!this.isPaused()) {
      clearTimeout(this._timeoutID);
      this._timeoutID = null;
    }
  }

  /**
   * Determines if the scroller is currently paused
   * @returns {boolean}
   */
  isPaused() {
    return !this._timeoutID;
  }
}

/**
 * Toggles activity scrolling feature
 */
function toggleActivityScroll() {
  const toggleButton = document.getElementById('activity-scoller-toggle');
  if (scroller.isPaused()) {
    scroller.start();
    toggleButton.innerText = "Pause this";
  }
  else {
    scroller.pause();
    toggleButton.innerText = "Resume this";
  }
}
