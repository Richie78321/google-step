/**
 * Initializes the comment system.
 *
 * Adds a submission event listener to the comment form and loads the comments.
 */
function initCommentsSystem() {
  const commentForm = document.getElementById("comment-form");
  commentForm.addEventListener("submit", attemptPostComment);

  loadComments();
}

/**
 * Gets comments from the '/comments' endpoint
 * and displays them in the comments section.
 */
function loadComments() {
  fetch('/comments').then(resp => {
        if (resp.ok) {
          return resp.json();
        } else {
          return resp.text().then(
              text => Promise.reject(`Error ${resp.status}: ${text}`));
        }
      }).then(comments => {
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
 * Adds a comment to the comments section UI.
 * @param {{author: string, commentBody: string}} comment
 */
function addCommentToPage(comment) {
  const commentContainer = document.getElementById("comment-container");
  
  const newComment = document.createElement("div");
  newComment.classList.add("p-4");
  newComment.classList.add("border-bottom");
  newComment.innerText = comment.commentBody;

  const authorFooter = document.createElement("footer");
  authorFooter.classList.add("blockquote-footer");
  authorFooter.innerText = comment.author;

  newComment.appendChild(authorFooter);
  commentContainer.appendChild(newComment);
}

/**
 * Attempts to post a comment using the comment form data.
 * @param {Event} event
 */
function attemptPostComment(event) {
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

  fetch('/comments', requestOptions).then(resp => {
        if (resp.ok) {
          addNotification("Comment posted successfully!", "alert-success");
          loadComments();
        } else {
          return resp.text().then(
              text => Promise.reject(`Error ${resp.status}: ${text}`));
        }
      }).catch(err => {
        console.error(err);

        addNotification(
            "Failed to post your comment! Please try again later.", 
            "alert-danger");
      });
}
