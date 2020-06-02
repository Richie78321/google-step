/**
 * Gets comments from the '/comments' endpoint
 * and displays them in the comments section.
 */
function loadComments() {
  fetch('/comments')
    .then(resp => resp.json())
    .then(comments => {
      console.log("Received comments: ");
      console.log(comments);

      removeCommentsOnPage();
      comments.forEach((comment) => addCommentToPage(comment));  
    })
    .catch(err => {
      console.error(err);
      alert("Failed to populate comments.");
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
  
  // Create a new comment element
  const newComment = document.createElement("div");
  newComment.classList.add("p-4");
  newComment.classList.add("border-bottom");
  newComment.innerText = comment.commentBody;

  const authorFooter = document.createElement("footer");
  authorFooter.classList.add("blockquote-footer");
  authorFooter.innerText = comment.author;

  newComment.appendChild(authorFooter);

  // Add it to the page
  commentContainer.appendChild(newComment);
}
