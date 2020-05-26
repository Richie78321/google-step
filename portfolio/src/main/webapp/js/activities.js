/**
 * Gathers repos from GitHub and scrolls through them in the About Me section
 */
let scroller = null;
async function startActivitiesScroll() {
  let repos = await fetch("https://api.github.com/users/Richie78321/repos")
    .then(res => res.json());
  repos =
    repos.filter(repo => repo.description && repo.description.length !== 0);

  scroller = new Scroller(repos);
  scroller.start();
}

function Scroller(repos) {
  this.repos = repos;
  this.index = 0;
  this.timeoutID = null;
}
Scroller.prototype.activityScroll = function() {
  const nextTimeout = 5000;
  const repo = this.repos[this.index];

  const activityContainer = document.getElementById('activity-scroller');
  activityContainer.innerText = repo.description;

  // Increment index and wait to update scroller
  this.index++;
  this.index %= this.repos.length;
  this.timeoutID =
    setTimeout(() => { this.activityScroll(); }, nextTimeout);
};
Scroller.prototype.start = function() {
  if (!this.timeoutID) {
    this.activityScroll();
  }
};
Scroller.prototype.pause = function() {
  if (this.timeoutID) {
    clearTimeout(this.timeoutID);
    this.timeoutID = null;
  }
};
Scroller.prototype.isPaused = function() {
  return !this.timeoutID;
};

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
