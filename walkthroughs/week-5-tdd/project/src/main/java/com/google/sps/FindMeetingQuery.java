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
import java.util.Iterator;
import java.util.Set;

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

    events = getEventsAttending(events, request.getAttendees());
    Collection<TimeRange> availableTimes = new ArrayList<TimeRange>();
    availableTimes.add(TimeRange.WHOLE_DAY);

    // Each event time range punches a hole in available time range
    Iterator<Event> eventIterator = events.iterator();
    Event event;
    while (eventIterator.hasNext()) {
      event = eventIterator.next();
      TimeRange eventTimeRange = event.getWhen();

      Collection<TimeRange> newRanges = new ArrayList<TimeRange>();
      Collection<TimeRange> rangesToRemove = new ArrayList<TimeRange>();
      Iterator<TimeRange> availableTimesIterator = availableTimes.iterator();
      TimeRange availableTime;
      while (availableTimesIterator.hasNext()) {
        availableTime = availableTimesIterator.next();

        if (availableTime.overlaps(eventTimeRange)) {
          removeRange(
              newRanges, rangesToRemove, availableTime, eventTimeRange, request.getDuration());
        }
      }

      availableTimes.removeAll(rangesToRemove);
      availableTimes.addAll(newRanges);
    }

    return availableTimes;
  }

  /**
   * Removes a range of time from a time range.
   * Precondition: The ranges are assumed to be overlapping.
   *
   * Broken down into three different scenarios:
   * 1. The time range is fully contained in the range to remove: just remove the time range.
   * 2. The range to remove is fully contained in the time range: split the time range into two
   * pieces.
   * 3. The time range overlaps with the range to remove but neither is fully contained in the
   * other: trim the time range.
   * 
   * @param newRanges
   * @param discardedRanges
   * @param timeRange
   * @param rangeToRemove
   * @param minDuration
   */
  private void removeRange(
      Collection<TimeRange> newRanges,
      Collection<TimeRange> discardedRanges,
      TimeRange timeRange, 
      TimeRange rangeToRemove, 
      long minDuration) {
    discardedRanges.add(timeRange);
    if (rangeToRemove.contains(timeRange)) {
      return;
    }
    
    if (timeRange.contains(rangeToRemove)) {
      // Split time
      addTimeRangeIfLongEnough(newRanges, timeRange.start(), rangeToRemove.start(), minDuration);
      addTimeRangeIfLongEnough(newRanges, rangeToRemove.end(), timeRange.end(), minDuration);
    }
    else {
      if (timeRange.start() < rangeToRemove.start()) {
        addTimeRangeIfLongEnough(
            newRanges, timeRange.start(), rangeToRemove.start(), minDuration);
      } else {
        addTimeRangeIfLongEnough(newRanges, rangeToRemove.end(), timeRange.end(), minDuration);
      }
    }
  }

  /**
   * Adds a time range to the collection if it meets the specified minimum duration
   * @param newRanges
   * @param startTime
   * @param endTime
   * @param minDuration
   */
  private void addTimeRangeIfLongEnough(
      Collection<TimeRange> newRanges, int startTime, int endTime, long minDuration) {
    if (endTime - startTime >= minDuration) {
      newRanges.add(TimeRange.fromStartEnd(startTime, endTime, false));
    }
  }

  /**
   * Gets all of the events being attended by at least one of the attendees.
   * @param events
   * @param attendees
   * @return Returns a collection of events being attended by at least one of the attendees.
   */
  private Collection<Event> getEventsAttending(
      Collection<Event> events, Collection<String> attendees) {
    Collection<Event> attendingEvents = new ArrayList<Event>();

    Iterator<Event> eventsIterator = events.iterator();
    Event event;
    while (eventsIterator.hasNext()) {
      event = eventsIterator.next();
      Set<String> eventAttendees = event.getAttendees();

      Iterator<String> attendeesIterator = attendees.iterator();
      String attendee;
      while (attendeesIterator.hasNext()) {
        attendee = attendeesIterator.next();
        
        if (eventAttendees.contains(attendee)) {
          attendingEvents.add(event);
          break;
        }
      }
    }

    return attendingEvents;
  }
}
