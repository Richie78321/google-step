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

package com.google.sps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public final class FindMeetingQuery {
  /**
   * Get the collection of time ranges when an event can be held.
   * @param events
   * @param request
   * @return Returns the collection of time ranges when the event can be held.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    Collection<TimeRange> validTimeRanges = new ArrayList<TimeRange>();

    Collection<TimeRange> orderedConflicts = 
        getOrderedAttendingEvents(events, request.getAttendees());
    Iterator<TimeRange> conflictsIterator = orderedConflicts.iterator();
    int prevConflictEndTime = TimeRange.START_OF_DAY;

    while (conflictsIterator.hasNext()) {
      TimeRange conflict = conflictsIterator.next();
      int conflictStartTime = conflict.start();
      
      // Check for gap between prev conflict and this conflict.
      if (conflictStartTime - prevConflictEndTime >= request.getDuration()) {
        validTimeRanges.add(TimeRange.fromStartEnd(prevConflictEndTime, conflictStartTime, false));
      }

      // Only update conflict end time if it is greater than the existing previous end time.
      // This is to handle nested events where previous event will have later end time.
      if (conflict.end() > prevConflictEndTime) {
        prevConflictEndTime = conflict.end();  
      }
    }

    if (TimeRange.END_OF_DAY - prevConflictEndTime >= request.getDuration()) {
      validTimeRanges.add(TimeRange.fromStartEnd(prevConflictEndTime, TimeRange.END_OF_DAY, true));  
    }

    return validTimeRanges;
  }

  /**
   * Get a collection of the events at least one of the attendees is attending, ordered by
   * ascending start time.
   * @param events
   * @param attendees
   * @return Return a collection of the events in ascending order of start time.
   */
  private Collection<TimeRange> getOrderedAttendingEvents(
      Collection<Event> events, Collection<String> attendees) {
    ArrayList<TimeRange> timeConflicts = new ArrayList<TimeRange>();

    Iterator<Event> eventsIterator = events.iterator();
    while (eventsIterator.hasNext()) {
      Event event = eventsIterator.next();
      if (!Collections.disjoint(event.getAttendees(), attendees)) {
        timeConflicts.add(event.getWhen());
      }
    }

    Collections.sort(timeConflicts, TimeRange.ORDER_BY_START);
    return timeConflicts;
  }
}
