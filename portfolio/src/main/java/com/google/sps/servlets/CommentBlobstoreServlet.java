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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/comment-blobstore")
public class CommentBlobstoreServlet extends HttpServlet {
  
  public static String BLOBSTORE_URL_REDIRECT = "/comments";

  private Gson gson = new Gson();

  /** 
   * Returns a URL that points to the image associated with a BlobKey.
   * 
   * @param blobKey
   * @return Returns a URL that points to the image associated with the BlobKey.
   */
  public static String getUploadedImageURL(BlobKey blobKey) {
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's dev server, we must use the 
    // relative path to the image, rather than the path returned by imagesService 
    // which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }
  
  /**
   * Removes an uploaded image associated with a BlobKey.
   * Removes the image from the ImageService as well as the Blobstore.
   *
   * @param blobKey
   * @return Returns whether or not the uploaded image was removed successfully.
   */
  public static boolean removeUploadedImage(BlobKey blobKey) {
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    try {
      imagesService.deleteServingUrl(blobKey);
    } catch (IllegalArgumentException e) {
      return false;
    }

    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    blobstoreService.delete(blobKey);

    return true;
  }
  
  /**
   * Gets the BlobKey associated with an image uploaded with the request. Returns null
   * if no file was uploaded or the file uploaded was not an image.
   *
   * @param request
   * @param formInputName The property name of the image file from the request. This is also most
   * likely the name of the input element for the file on the comment posting form.
   * @return Returns the BlobKey for the uploaded image or null.
   */
  public static BlobKey getUploadedImageBlobKey(HttpServletRequest request, String formInputName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    // Or the user submitted a file but the file is not an image.
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    
    if (blobInfo.getSize() == 0 || !blobInfo.getContentType().startsWith("image/")) {
      blobstoreService.delete(blobKey);
      return null;
    }

    return blobKey;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl(BLOBSTORE_URL_REDIRECT);

    JsonObject responseObject = new JsonObject();
    responseObject.addProperty("uploadUrl", uploadUrl);
    responseObject.addProperty("redirectUrl", BLOBSTORE_URL_REDIRECT);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(responseObject));
  }
}
