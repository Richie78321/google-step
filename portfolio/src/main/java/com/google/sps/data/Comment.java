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

package com.google.sps.data;

import com.google.appengine.api.datastore.Entity;
import com.google.sps.helper.ValidationResult;
import com.google.sps.servlets.DataServlet;
import javax.servlet.http.HttpServletRequest;

/** Class containing comment data. */
public final class Comment {

  // Whitelist to avoid the possibility of XSS
  private static final String UNSAFE_CHARACTERS_REGEX = "[^A-Za-z0-9._~()'!*:@,;+?\\s-]";

  public static final String AUTHOR_KEY = "author";
  public static final String BODY_KEY = "comment-body";
  public static final String TIME_POSTED_KEY = "timePosted";
  
  /**
   * Get and validate a comment object from an incoming request.
   * @param request
   * @return Returns a new comment object or a validation error message.
   */
  public static ValidationResult<Comment> getIncomingComment(HttpServletRequest request) {
    String commentAuthor = DataServlet.getParameter(request, AUTHOR_KEY, "");
    String commentBody = DataServlet.getParameter(request, BODY_KEY, "");

    commentAuthor = commentAuthor.replaceAll(UNSAFE_CHARACTERS_REGEX, "");
    commentBody = commentBody.replaceAll(UNSAFE_CHARACTERS_REGEX, "");

    String validationError = validateIncomingComment(commentAuthor, commentBody);
    if (validationError != null) {
      return new ValidationResult<Comment>(validationError);
    } else {
      Comment newComment = new Comment(commentAuthor, commentBody);
      return new ValidationResult<Comment>(newComment);
    }
  }

  /**
   * Validates the comment parameters of a new comment.
   * @return Return the error string or null if there are no errors.
   */
  private static String validateIncomingComment(String commentAuthor, String commentBody) {
    String validationErrors = "";

    if (commentAuthor.isBlank()) {
      validationErrors += "Please include a comment author (cannot be whitespace).";
    }
    if (commentBody.isBlank()) {
      if (validationErrors.length() > 0) {
        validationErrors += " ";
      }
      validationErrors += "Please include a comment body (cannot be whitespace).";
    }

    if (validationErrors.isEmpty()) {
      return null;
    } else {
      return validationErrors;
    }
  }

  private final String author;
  private final String commentBody;
  private final long id;
  private final long timePosted;

  /**
    * Creates a new comment object without an ID.
    * @param author The author of the comment.
    * @param commentBody The text body of the comment.
    */
  public Comment(String author, String commentBody) {
    this.author = author;
    this.commentBody = commentBody;
    this.id = -1;
    this.timePosted = -1;
  }

  /**
    * Creates a new comment object associated with a database entry.
    * @param author The author of the comment.
    * @param commentBody The text body of the comment.
    * @param id The associated ID of the comment.
    * @param timePosted The time the comment was posted to the database.
    */
  public Comment(String author, String commentBody, long id, long timePosted) {
    this.author = author;
    this.commentBody = commentBody;
    this.id = id;
    this.timePosted = timePosted;
  }

  /**
    * Creates a new comment object associated with a database entry.
    * @param comment
    * @param id The associated ID of the comment.
    * @param timePosted The time the comment was posted to the database.
    */
  public Comment(Comment comment, long id, long timePosted) {
    this.author = comment.getAuthor();
    this.commentBody = comment.getCommentBody();
    this.id = id;
    this.timePosted = timePosted;
  }

  /**
   * Creates a comment object from a database entry.
   * @param commentEntity
   */
  public Comment(Entity commentEntity) {
    this.id = commentEntity.getKey().getId();
    this.author = (String) commentEntity.getProperty(AUTHOR_KEY);
    this.commentBody = (String) commentEntity.getProperty(BODY_KEY);
    this.timePosted = (long) commentEntity.getProperty(TIME_POSTED_KEY);
  }

  /**
   * Fills a datastore entity with the comment's data.
   * @param commentEntity
   */
  public void fillEntity(Entity commentEntity) {
    long timePosted = System.currentTimeMillis();
    commentEntity.setProperty(AUTHOR_KEY, author);
    commentEntity.setProperty(BODY_KEY, commentBody);
    commentEntity.setProperty(TIME_POSTED_KEY, timePosted);
  }

  public String getAuthor() {
    return author;
  }

  public String getCommentBody() {
    return commentBody;
  }

  public long getId() {
    return id;
  }

  public long getTimePosted() {
    return timePosted;
  }
}
