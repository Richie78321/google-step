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

import com.google.gson.Gson;
import com.google.sps.data.Comment;
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

  private final String UNSAFE_CHARACTERS_REGEX = "[^A-Za-z0-9._~()'!*:@,;+?\\s-]";

  private final List<Comment> sessionComments = new ArrayList<Comment>();
  private final Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Gson gson = new Gson();
    String commentJson = gson.toJson(sessionComments);

    // Send the comment JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(commentJson);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String commentAuthor = getParameter(request, "author", "");
    String commentBody = getParameter(request, "comment-body", "");

    commentAuthor = commentAuthor.replaceAll(UNSAFE_CHARACTERS_REGEX, "");
    commentBody = commentBody.replaceAll(UNSAFE_CHARACTERS_REGEX, "");

    String validationError = validateIncomingComment(commentAuthor, commentBody);
    if (validationError != null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("text/html;");
      response.getWriter().println(validationError);
      return;
    }

    Comment newComment = new Comment(commentAuthor, commentBody);
    sessionComments.add(newComment);

    String commentJson = gson.toJson(newComment);

    // Return the new comment JSON as confirmation
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setContentType("application/json;");
    response.getWriter().println(commentJson);
    response.sendRedirect("/index.html");
  }

  /**
   * Validates the comment parameters of a new comment.
   * @return Return the error string or null if there are no errors.
   */
  private String validateIncomingComment(String commentAuthor, String commentBody) {
    String validationErrors = "";

    if (commentAuthor.isBlank()) {
      validationErrors += "Please include a comment author (cannot be whitespace).";
    }
    if (commentBody.isBlank()) {
      if (validationErrors.length() > 0) validationErrors += " ";
      validationErrors += "Please include a comment body (cannot be whitespace).";
    }

    if (validationErrors.isEmpty()) {
      return null;
    }
    else {
      return validationErrors;
    }
  }

  /**
   * @return Returns the request parameter associated with the inputted name,
   * or returns the default value if the specified parameter is not defined.
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
