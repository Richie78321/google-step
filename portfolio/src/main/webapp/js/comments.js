/**
 * Gets comments from the '/comments' endpoint
 * and displays them in the comments section.
 */
function loadComments() {
  fetch('/comments')
    .then(resp => {
      if (resp.ok) {
        return resp.json();
      } else {
        return resp.text()
          .then(text => Promise.reject(`Error ${resp.status}: ${text}`));
      }
    })
    .then(comments => {
      console.log("Received comments: ");
      console.log(comments);

      removeCommentsOnPage();
      comments.forEach((comment) => addCommentToPage(comment));  
    })
    .catch(err => {
      console.error(err);

      addNotification("Failed to populate the comments section!", 
        "alert-danger");
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
