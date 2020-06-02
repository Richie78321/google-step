/**
 * Gets comments from the '/comments' endpoint
 * and displays them in the comments section.
 */
function getComments() {
  fetch('/comments')
    .then(resp => resp.json())
    .then(comments => {
      console.log("Received comments: ");
      console.log(comments);

      comments.forEach((comment) => addCommentToPage(comment));  
    })
    .catch(err => {
      console.error(err);
      alert("Failed to populate comments.");
    });
}

/**
 * Adds a comment to the comments section UI.
 * @param {{author: string, commentBody: string}} comment
 */
function addCommentToPage(comment) {
  const commentContainer = document.getElementById("comment-container");
  
  // Create a new comment element
  const newComment = document.createElement("div");
  newComment.classList.add("card");
  newComment.classList.add("p-2");
  newComment.classList.add("my-2");
  newComment.innerText = comment.commentBody;

  // Add it to the page
  commentContainer.appendChild(newComment);
}
