/**
 * Adds a notification to the screen with a specified display time.
 * @param {string} msg
 * @param {string} alertClass The class of the alert. Useful for applying style.
 * @param {number} displayTime Time to display the alert in milliseconds.
 */
function addNotification(msg, alertClass, displayTime = 10000) {
  const NOTIFICATION_FADE_OUT_TIME = 1000; // 1 second
  const notificationContainer = document.getElementById("notification-container");

  const notificationElement = document.createElement("div");
  notificationElement.classList.add(
      "notification", alertClass, "p-2", "mb-2", "w-100");
  notificationElement.innerText = msg;

  notificationContainer.appendChild(notificationElement);

  setTimeout(() => {
    notificationElement.style.opacity = "0";
  }, displayTime);

  setTimeout(() => {
    notificationContainer.removeChild(notificationElement);
  }, displayTime + NOTIFICATION_FADE_OUT_TIME);
}
