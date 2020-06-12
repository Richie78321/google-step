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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FindMeetingQuery {
  /**
   * Get the collection of time ranges when an event can be held.
   *
   * If time ranges are available with optional attendees, those ranges are returned.
   * Otherwise, the time ranges when required attendees are available are returned.
   * @param events
   * @param request
   * @return Returns the collection of time ranges when the event can be held.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }

    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Collection<String> allAttendees = 
        Stream.concat(attendees.stream(), optionalAttendees.stream()).collect(Collectors.toList());
    
    Collection<TimeRange> rangesAvailableAllAttendees =
        getRangesWithoutConflict(events, allAttendees, request.getDuration());
    
    // First check for time ranges available with all attendees.
    if (!rangesAvailableAllAttendees.isEmpty() || 
        attendees.isEmpty() || 
        optionalAttendees.isEmpty()) {
      // If required attendees is empty: treat optional attendees as required.
      // If optional attendees is empty: no need to check for times without optional attendees.
      return rangesAvailableAllAttendees;
    }


    return getRangesWithoutConflict(events, attendees, request.getDuration());
  }

  private Collection<TimeRange> getRangesWithoutConflict(
      Collection<Event> events, Collection<String> attendees, long rangeDuration) {
    Iterator<TimeRange> conflictsIterator = getOrderedAttendingEvents(events, attendees).iterator();
    
    int prevConflictEndTime = TimeRange.START_OF_DAY;
    Collection<TimeRange> validTimeRanges = new ArrayList<TimeRange>();
    while (conflictsIterator.hasNext()) {
      TimeRange conflict = conflictsIterator.next();
      int conflictStartTime = conflict.start();
      
      // Check for gap between prev conflict and this conflict.
      if (conflictStartTime - prevConflictEndTime >= rangeDuration) {
        validTimeRanges.add(TimeRange.fromStartEnd(prevConflictEndTime, conflictStartTime, false));
      }

      // Only update conflict end time if it is greater than the existing previous end time.
      // This is to handle nested events where previous event will have later end time.
      if (conflict.end() > prevConflictEndTime) {
        prevConflictEndTime = conflict.end();  
      }
    }

    if (TimeRange.END_OF_DAY - prevConflictEndTime >= rangeDuration) {
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
