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

/** Class containing comment data. */
public final class Comment {

  private final String author;
  private final String commentBody;
  private long id = -1;

  /**
    * Creates a new comment object without an ID.
    * @param author The author of the comment.
    * @param commentBody The text body of the comment.
    */
  public Comment(String author, String commentBody) {
    this.author = author;
    this.commentBody = commentBody;
  }

  /**
    * Creates a new comment object with an ID.
    * @param author The author of the comment.
    * @param commentBody The text body of the comment.
    * @param id The associated ID of the comment.
    */
  public Comment(String author, String commentBody, long id) {
    this.author = author;
    this.commentBody = commentBody;
    this.id = id;
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

  public void setId(long id) {
    this.id = id;
  }
}
