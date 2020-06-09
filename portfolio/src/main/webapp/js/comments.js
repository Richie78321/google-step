/**
 * @typedef {Object} AuthData
 * @property {string} loginUrl The URL to log in.
 * @property {boolean} authorized Whether the user is currently authorized.
 * @property {{string: email, string: id}} [user] Information about the 
 * current authorized user. Only present if the user is authorized.
 */
/**
 * @type {AuthData}
 */
let commentAuthData = null;

/**
 * Initializes the comment system.
 */
function initCommentsSystem() {
  getAuthorization().then(() => {
    applyAuthorizationToUI();
    loadComments();
  }).catch((err) => {
    console.error(err);

    addNotification(
        "Failed to determine comment authentication! Please try again later", 
        "alert-danger");
  });

  initCommentControls();
}

/**
 * Initializes the control event handlers for posting and viewing comments.
 */
function initCommentControls() {
  const commentForm = document.getElementById("comment-form");
  commentForm.addEventListener("submit", postComment);

  const commentControl = document.getElementById("comment-control");
  commentControl.addEventListener("change", loadComments);

  commentControl.elements["refresh"].addEventListener("click", (event) => {
    event.preventDefault();
    loadComments();
  });
}

/**
 * Updates the comments UI according to user authorization. 
 */
function applyAuthorizationToUI() {
  if (commentAuthData.authorized) {
    const withAuthElements = 
        Array.from(document.getElementsByClassName("only-display-with-auth"));
    // Make authorization-only UI visible
    withAuthElements.forEach((elem) => elem.style.display = "inherit");
  } else {
    const withoutAuthElements = Array.from(
        document.getElementsByClassName("only-display-without-auth"));
    // Make no-authorization-only UI visible
    withoutAuthElements.forEach((elem) => elem.style.display = "inherit");
    
    const loginButton = document.getElementById("comment-auth-login");
    loginButton.href = commentAuthData.loginUrl;
  }
}

/**
 * Gets the user's authorization status.
 * @return {Promise} Returns a promise that fetches the user's 
 * authorization data {@link AuthData}.
 */
function getAuthorization() {
  return formatFetchResponse(fetch('/auth')).then((authData) => {
    console.log("Got comment authorization:");
    console.log(authData);
    commentAuthData = authData;
  });
}

/**
 * Gets comments from the '/comments' endpoint
 * and displays them in the comments section.
 */
function loadComments() {
  const commentControl = document.getElementById("comment-control");
  const loadUrl = 
      new URL("/comments", `${location.protocol}//${location.hostname}`);
  loadUrl.searchParams.set(
      "numPerPage", commentControl.elements["numPerPage"].value);
  loadUrl.searchParams.set(
      "page", commentControl.elements["pageNum"].value)

  formatFetchResponse(fetch(loadUrl)).then(comments => {
    console.log("Received comments: ");
    console.log(comments);

    removeCommentsOnPage();
    comments.forEach((comment) => addCommentToPage(comment));  
  }).catch(err => {
    console.error(err);

    addNotification(
        "Failed to populate the comments section!", "alert-danger");
  });
}

/**
 * Removes the comments currently displaying on the page.
 */
function removeCommentsOnPage() {
  const commentContainer = document.getElementById("comment-container");

  let lastComment = commentContainer.lastElementChild;
  while (lastComment) {
    commentContainer.removeChild(lastComment);
    lastComment = commentContainer.lastElementChild;
  }
}

/**
 * @typedef {Object} Comment
 * @property {string} author
 * @property {string} commentBody
 * @property {number} id
 * @property {number} timePosted Time the comment was posted in unix timestamp.
 */
/**
 * Adds a comment to the comments section UI.
 * @param {Comment} comment
 */
function addCommentToPage(comment) {
  const commentContainer = document.getElementById("comment-container");
  
  const newComment = document.createElement("div");
  newComment.classList.add("p-4", "border-bottom");
  newComment.innerText = comment.commentBody;

  const authorFooter = document.createElement("footer");
  authorFooter.classList.add("blockquote-footer");
  const formattedTime = moment(comment.timePosted).fromNow();
  authorFooter.innerText = `${comment.author}, ${formattedTime}`;

  const deleteButton = document.createElement("button");
  deleteButton.classList.add("btn", "btn-muted", "btn-sm", "ml-2");
  deleteButton.innerText = "Delete";
  deleteButton.addEventListener("click", createCommentDeleter(comment.id));

  authorFooter.appendChild(deleteButton);

  newComment.appendChild(authorFooter);

  commentContainer.appendChild(newComment);
}

/**
 * Attempts to post a comment using the comment form data.
 * @param {Event} event
 */
function postComment(event) {
  // Prevents the form submission from causing a browser refresh
  event.preventDefault();

  const commentForm = document.getElementById("comment-form");

  // Form data is encoded strictly in the form multipart/form-data
  // However, Java servlet only supports application/x-www-form-urlencoded
  // So use URLSearchParams to convert the data to the supported format
  const formData = new URLSearchParams(new FormData(commentForm)).toString();

  const requestOptions = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: formData
  };

  formatFetchResponse(fetch('/comments', requestOptions)).then(() => {
    addNotification("Comment posted successfully!", "alert-success");
    loadComments();
  }).catch(err => {
    console.error(err);

    addNotification(
        "Failed to post your comment! Please try again later.", 
        "alert-danger");
  });
}

/**
 * Creates a function that sends a comment delete request.
 * @param {number} id
 * @return Returns a function that deletes the comment associated with an ID.
 */
function createCommentDeleter(id) {
  return () => {
    const deleteUrl = 
        new URL("/comments", `${location.protocol}//${location.hostname}`);
    deleteUrl.searchParams.set("id", id);

    formatFetchResponse(fetch(deleteUrl, { method: 'DELETE' })).then(() => {
      addNotification("Comment deleted successfully.", "alert-success");
      loadComments();
    }).catch(err => {
        console.log(err);

        addNotification(
          "Failed to delete the comment! Please try again later.", 
          "alert-danger");
    });
  };
}

/**
 * Formats the fetch response according to the portfolio's API.
 * 
 * If the response is okay, the response format is JSON. Otherwise the error 
 * message is formatted as raw text and the promise is rejected.
 * @param {Promise} fetchRequest Fetch request to portfolio API.
 * @return {Promise}
 */
function formatFetchResponse(fetchRequest) {
  return fetch.then((resp) => {
    if (resp.ok) {
      return resp.json();
    } else {
      return resp.text().then(
          text => Promise.reject(`Error ${resp.status}: ${text}`));
    }
  });
}
