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

public final class ValidationResult<T> {
  private final T createdObject;
  private final String validationMessage;

  /**
   * Creates a new validation factory result with a validation error.
   * @param validationMessage
   */
  public ValidationResult(String validationMessage) {
    this.validationMessage = validationMessage;
    this.createdObject = null;
  }

  /**
   * Creates a new validation factory result with no validation errors.
   * @param createdObject
   */
  public ValidationResult(T createdObject) {
    this.createdObject = createdObject;
    this.validationMessage = null;
  }

  /**
   * Whether or not there was a validation error.
   * @return Returns true if the validation message is not equal to null.
   */
  public Boolean hasValidationError() {
    return validationMessage != null;
  }

  public T getCreatedObject() {
    return createdObject;
  }

  public String getValidationMessage() {
    return validationMessage;
  }
}
