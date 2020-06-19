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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.sps.helper.ValidationResult;
import com.google.sps.servlets.CommentBlobstoreServlet;
import com.google.sps.servlets.DataServlet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/** Class containing comment data. */
public final class Comment {

  // Whitelist to avoid the possibility of XSS
  private static final String UNSAFE_CHARACTERS_REGEX = "[^A-Za-z0-9._~()'!*:@,;+?\\s-]";

  public static final String AUTHOR_KEY = "author";
  public static final String BODY_KEY = "comment-body";
  public static final String TIME_POSTED_KEY = "timePosted";
  public static final String POSTER_ID_KEY = "posterId";
  public static final String ATTACHED_IMAGE_URL_KEY = "attachedImageUrl";
  public static final String ATTACHED_IMAGE_BLOBKEY_KEY = "attachedImageBlobKey";
  
  /**
   * Get and validate a comment object from an incoming request.
   * @param request
   * @return Returns a new comment object or a validation error message.
   */
  public static ValidationResult<Comment> getIncomingComment(
      HttpServletRequest request, String posterId) {
    String commentAuthor = DataServlet.getParameter(request, AUTHOR_KEY, "");
    String commentBody = DataServlet.getParameter(request, BODY_KEY, "");

    commentAuthor = commentAuthor.replaceAll(UNSAFE_CHARACTERS_REGEX, "").trim();
    commentBody = commentBody.replaceAll(UNSAFE_CHARACTERS_REGEX, "").trim();

    String validationError = validateIncomingComment(commentAuthor, commentBody);
    if (validationError != null) {
      return new ValidationResult<Comment>(validationError);
    } else {
      Comment newComment;

      BlobKey attachedImageBlobKey = 
          CommentBlobstoreServlet.getUploadedImageBlobKey(request, "attachedImage");
      if (attachedImageBlobKey != null) {
        String attachedImageUrl = CommentBlobstoreServlet.getUploadedImageURL(attachedImageBlobKey);
        newComment = new Comment(
            commentAuthor, 
            commentBody, 
            posterId, 
            attachedImageUrl, 
            attachedImageBlobKey.getKeyString());
      } else {
        newComment = new Comment(commentAuthor, commentBody, posterId, null, null);
      }

      return new ValidationResult<Comment>(newComment);
    }
  }

  /**
   * Validates the comment parameters of a new comment.
   * @return Return the error string or null if there are no errors.
   */
  private static String validateIncomingComment(String commentAuthor, String commentBody) {
    List<String> validationErrors = new ArrayList<String>();
    
    if (commentAuthor.length() == 0) {
      validationErrors.add("Please include a comment author (cannot be whitespace).");
    }
    if (commentBody.length() == 0) {
      validationErrors.add("Please include a comment body (cannot be whitespace).");
    }

    if (validationErrors.isEmpty()) {
      return null;
    } else {
      return String.join(" ", validationErrors);
    }
  }

  private final String author;
  private final String commentBody;
  private long id;
  private final long timePosted;
  private final String posterId;
  private final String attachedImageUrl;
  // Transient to not be JSON serialized. 
  private final transient String attachedImageBlobKey;

  /**
    * Creates a new comment object.
    * @param author The author of the comment.
    * @param commentBody The text body of the comment.
    * @param posterId The ID of the user that posted the comment.
    * @param attachedImageUrl The URL to the image attached with the comment or null if no image.
    * @param attachedImageBlobKey The blob key of the attached image. This is not serialized.
    */
  public Comment(
      String author, 
      String commentBody, 
      String posterId, 
      String attachedImageUrl, 
      String attachedImageBlobKey) {
    this.author = author;
    this.commentBody = commentBody;
    this.id = -1;
    this.timePosted = System.currentTimeMillis();
    this.posterId = posterId;
    this.attachedImageUrl = attachedImageUrl;
    this.attachedImageBlobKey = attachedImageBlobKey;
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
    this.posterId = (String) commentEntity.getProperty(POSTER_ID_KEY);
    this.attachedImageUrl = (String) commentEntity.getProperty(ATTACHED_IMAGE_URL_KEY);
    this.attachedImageBlobKey = (String) commentEntity.getProperty(ATTACHED_IMAGE_BLOBKEY_KEY);
  }

  /**
   * Fills a datastore entity with the comment's data.
   * @param commentEntity
   */
  public void fillEntity(Entity commentEntity) {
    commentEntity.setProperty(AUTHOR_KEY, author);
    commentEntity.setProperty(BODY_KEY, commentBody);
    commentEntity.setProperty(TIME_POSTED_KEY, timePosted);
    commentEntity.setProperty(POSTER_ID_KEY, posterId);
    commentEntity.setProperty(ATTACHED_IMAGE_URL_KEY, attachedImageUrl);
    commentEntity.setProperty(ATTACHED_IMAGE_BLOBKEY_KEY, attachedImageBlobKey);
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

  public String getPosterId() {
    return posterId;
  }

  public long getTimePosted() {
    return timePosted;
  }

  public String getAttachedImageUrl() {
    return attachedImageUrl;
  }

  public String getAttachedImageBlobKey() {
    return attachedImageBlobKey;
  }

  public void setId(long id) {
    this.id = id;
  }
}
