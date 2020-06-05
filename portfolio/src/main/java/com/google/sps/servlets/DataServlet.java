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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.sps.helper.Pagination;
import com.google.sps.helper.ValidationResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final Gson gson = new Gson();

  /**
   * @return Returns the request parameter associated with the inputted name,
   * or returns the default value if the specified parameter is not defined.
   */
  public static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ValidationResult<Pagination> paginationResult = Pagination.getIncomingPagination(request);

    if (paginationResult.hasValidationError()) {
      sendRawTextError(
          response, HttpServletResponse.SC_BAD_REQUEST, paginationResult.getValidationMessage());
      return;
    }

    Query sortedCommentsQuery = new Query("Comment")
        .addSort(Comment.TIME_POSTED_KEY, SortDirection.DESCENDING);

    Pagination commentPagination = paginationResult.getCreatedObject();
    List<Comment> comments = queryCommentsDatastore(sortedCommentsQuery, commentPagination);

    String commentJson = gson.toJson(comments);

    // Send the comment JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(commentJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    ValidationResult<Comment> validationResult = Comment.getIncomingComment(request);
    if (validationResult.hasValidationError()) {
      sendRawTextError(
          response, HttpServletResponse.SC_BAD_REQUEST, validationResult.getValidationMessage());
      return;
    }

    Comment newComment = validationResult.getCreatedObject();
    newComment = addCommentToDatastore(newComment);

    String commentJson = gson.toJson(newComment);

    // Return the new comment JSON as confirmation
    response.setContentType("application/json;");
    response.getWriter().println(commentJson);
    response.setStatus(HttpServletResponse.SC_CREATED);
  }

  /**
   * Adds a comment to the datastore.
   * @return Return an updated comment object with information from the database entry.
   */
  private Comment addCommentToDatastore(Comment comment) {
    Entity commentEntity = new Entity("Comment");
    comment.fillEntity(commentEntity);

    Key datastoreKey = datastore.put(commentEntity);
    comment.setId(datastoreKey.getId());
    return comment;
  }

  /**
   * Queries comments from the datastore.
   * @return Return the resulting comments.
   */
  private List<Comment> queryCommentsDatastore(Query commentsQuery, Pagination pagination) {
    if (!commentsQuery.getKind().equals("Comment")) {
      throw new IllegalArgumentException("Query must be made to kind 'Comment'.");
    }

    PreparedQuery queryResults = datastore.prepare(commentsQuery);

    // Uses offset to implement pagination
    // Offset is a very inefficient means of skipping elements, and if this application were
    // anticipating a lot more load should instead use something like cursors:
    // https://cloud.google.com/datastore/docs/concepts/queries#cursors_limits_and_offsets
    FetchOptions fetchOptions = FetchOptions.Builder
        .withOffset(pagination.getOffset())
        .limit(pagination.getLimit());

    List<Comment> comments = new ArrayList<Comment>();
    for (Entity commentEntity : queryResults.asIterable(fetchOptions)) {
      Comment newComment = new Comment(commentEntity);
      comments.add(newComment);
    }

    return comments;
  }

  /**
   * Sends an error to the client as raw text instead of the default HTML page.
   */
  private void sendRawTextError(HttpServletResponse response, int errorCode, String errorMsg) 
      throws IOException {
    response.setStatus(errorCode);
    response.setContentType("text/html;");
    response.getWriter().println(errorMsg);
  }
}
