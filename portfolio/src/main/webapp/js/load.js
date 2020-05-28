/**
 * Starts necessary JS after DOM has loaded
 */
function onLoad() {
  startActivitiesScroll();
  loadFreeze();
}

/**
 * Initializes freezeframe from CDN
 */
function loadFreeze() {
  const freezeframe = new Freezeframe();

  freezeframe.stop();
}
