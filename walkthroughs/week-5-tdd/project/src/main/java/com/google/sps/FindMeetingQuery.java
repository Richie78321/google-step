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

import org.apache.commons.math3.util.CombinatoricsUtils;
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
    Collection<TimeRange> rangesAvailableRequired =
        getRangesWithoutConflict(events, attendees, request.getDuration());
    
    if (rangesAvailableRequired.isEmpty() || 
        request.getOptionalAttendees().isEmpty()) {
      // If the available time ranges for required attendees is empty: no need to check optional.
      // If there are no optional attendees: no need to check optional. 
      return rangesAvailableRequired;
    }

    Collection<TimeRange> rangesAvailableOptional = findOptimalOptionalAttendee(events, request);
    
    if (rangesAvailableOptional.isEmpty()) {
      return rangesAvailableRequired;
    } else {
      return rangesAvailableOptional;
    }
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

  /**
   * Find the optimal time ranges for optional attendees.
   *
   * @param events
   * @param request
   * @return Returns the optimal time ranges for optional attendees. Can be empty.
   */
  private Collection<TimeRange> findOptimalOptionalAttendee(
      Collection<Event> events, MeetingRequest request) {
    // This works by checking each combination of optional attendees in increasing quantity.
    // Starting with groups of one attendee, then two, etc. Until less than two combinations
    // had non-empty ranges (see optimization notes below) or all combinations have been checked.
    // 
    // For large numbers of optional attendees, this can quickly become inefficient.
    // There is room here for a dynamic programming approach following the logic that a combination
    // of three elements could be more efficiently computed by using a previously-computed
    // combination of two elements. 

    ArrayList<String> optionalAttendees = 
        new ArrayList<String>(request.getOptionalAttendees());
    Collection<String> attendees = request.getAttendees();

    int successfulCombinations = 0;
    Collection<TimeRange> bestTimeRange = Arrays.asList();
    int longestDuration = 0;
    for (int i = 1; i <= optionalAttendees.size(); i++) {
      Iterator<int[]> attendeeCombinations = 
          CombinatoricsUtils.combinationsIterator(optionalAttendees.size(), i);
      
      // Find the longest duration of time ranges for each combination of i attendees.
      while (attendeeCombinations.hasNext()) {
        int[] combination = attendeeCombinations.next();

        Stream<String> optionalAttendeeCombination = 
            Arrays.stream(combination).mapToObj(index -> optionalAttendees.get(index));
        Collection<String> attendeeCombination = 
            Stream.concat(optionalAttendeeCombination, attendees.stream())
                .collect(Collectors.toList());

        Collection<TimeRange> availableRanges = 
            getRangesWithoutConflict(events, attendeeCombination, request.getDuration());

        if (!availableRanges.isEmpty()) {
          successfulCombinations++;

          int totalTime = getTotalDurationOfRanges(availableRanges);
          if (totalTime > longestDuration) {
            bestTimeRange = availableRanges;
            longestDuration = totalTime;
          }
        }
      }

      // This is an optimization following the logic that if only one combination of n elements
      // was successful, there can be no combination of (n + 1) elements that is also successful.
      if (successfulCombinations < 2) {
        break;
      }
      longestDuration = 0;
    }

    return bestTimeRange;
  }

  /**
   * Gets the total duration of a collection of non-overlapping time ranges.
   * Precondition: The time ranges are not overlapping.
   * @param timeRanges
   * @return Returns the total duration of the time ranges.
   */
  private int getTotalDurationOfRanges(Collection<TimeRange> timeRanges) {
    int totalTime = 0;
    for (TimeRange timeRange : timeRanges) {
      totalTime += timeRange.duration();
    }

    return totalTime;
  }
}
