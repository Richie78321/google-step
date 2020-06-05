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

package com.google.sps.helper;

import com.google.sps.helper.ValidationResult;
import com.google.sps.servlets.DataServlet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public final class Pagination {

  public static final String PAGE_KEY = "page";
  public static final String NUM_PER_PAGE_KEY = "numPerPage";

  private static final int DEFAULT_NUM_PER_PAGE = 10;

  /**
   * Get and validate pagination options from an incoming request.
   * 
   * If either the page number parameter or the number per page parameter
   * are not supplied, they are supplemented with default values.
   * @return Returns a pagination object or a validation error message.
   */
  public static ValidationResult<Pagination> getIncomingPagination(
      HttpServletRequest request) {
    String pageNumParam = DataServlet.getParameter(request, PAGE_KEY, null);
    String numPerPageParam = DataServlet.getParameter(request, NUM_PER_PAGE_KEY, null);

    List<String> validationErrors = new ArrayList<String>();

    Integer pageNum = tryParseInt(pageNumParam, 0);
    Integer numPerPage = tryParseInt(numPerPageParam, DEFAULT_NUM_PER_PAGE);

    if (pageNum == null) {
      validationErrors.add("Page number is not a valid number.");
    }
    if (numPerPage == null) {
      validationErrors.add("Number per page is not a valid number.");
    }

    if (validationErrors.isEmpty()) {
      Pagination newPagination = new Pagination(pageNum, numPerPage);
      return new ValidationResult<Pagination>(newPagination);
    }
    else {
      return new ValidationResult<Pagination>(String.join(" ", validationErrors));
    }
  }

  /**
   * Attempts to parse an integer string. Returns the default value if the string is null.
   * @return The parsed value, default value, or null if parsing error.
   */
  public static Integer tryParseInt(String numberString, Integer defaultValue) {
    if (numberString == null) {
      return defaultValue;
    }

    try {
      return Integer.parseInt(numberString);
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  private final int pageNum;
  private final int numPerPage;

  /**
   * Creates a pagination object.
   * @param pageNum
   * @param numPerPage
   */
  public Pagination(int pageNum, int numPerPage) {
    this.pageNum = pageNum;
    this.numPerPage = numPerPage;
  }

  public int getOffset() {
    return pageNum * numPerPage;
  }

  public int getLimit() {
    return numPerPage;
  }
}
