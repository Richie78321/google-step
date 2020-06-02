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

  private final List<Comment> sampleComments = new ArrayList<Comment>();

  @Override
  public void init() {
    // Add sample data
    sampleComments.add(new Comment(
      "Richie Goulazian",
      "This is an example of a comment. This is one of multiple comments that I need to write"
    ));
    sampleComments.add(new Comment(
      "Rich Goulaz",
      "This is not an example of a comment... or maybe it is? How to tell..."
    ));
    sampleComments.add(new Comment(
      "Bob Guy",
      "My name is Bob. I like this page."
    ));
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Convert comments to JSON
      Gson gson = new Gson();
      String commentJson = gson.toJson(sampleComments);

      // Send the JSON as the response
      response.setContentType("application/json;");
      response.getWriter().println(commentJson);
  }
}
