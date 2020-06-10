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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

  public static final String AUTH_URL_REDIRECT = "/";

  public Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {    
    JsonObject responseObject = new JsonObject();

    UserService userService = UserServiceFactory.getUserService();
    String loginUrl = userService.createLoginURL(AUTH_URL_REDIRECT);
    String logoutUrl = userService.createLogoutURL(AUTH_URL_REDIRECT);

    responseObject.addProperty("loginUrl", loginUrl);
    responseObject.addProperty("logoutUrl", logoutUrl);

    if (userService.isUserLoggedIn()) {
      responseObject.addProperty("authorized", true);

      UserData userData = new UserData(userService.getCurrentUser());

      responseObject.add("user", gson.toJsonTree(userData));
    } else {
      responseObject.addProperty("authorized", false);
    }

    String responseJson = gson.toJson(responseObject);
    
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json;");
    response.getWriter().println(responseJson);
  }

  private class UserData {
    private final String email;
    private final String id;

    /**
     * Creates a user data object intended for JSON serialization.
     */
    public UserData(User user) {
      this.email = user.getEmail();
      this.id = user.getUserId();
    }

    public String getEmail() {
      return this.email;
    }

    public String getId() {
      return this.id;
    }
  }
}
