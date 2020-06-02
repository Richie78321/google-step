// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts = [
    "I really like reinforcement learning, particularly genetic algorithms!",
    "I went to Peru last summer and saw Machu Picchu!",
    "I have two dogs, one of which is 15 years old!",
    "I enjoy making video game engines in my spare time!"
  ];

  // Pick a random fact.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

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
