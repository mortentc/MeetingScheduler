import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Calendar;

public class MeetingScheduler implements Scheduler {

    private class MeetingComparator implements Comparator<UUID>{

        @Override
        public int compare(UUID o1, UUID o2) {
            return meetingMap.get(o1).timeslot().compareTo(meetingMap.get(o2).timeslot());
        }
    }

    private Map<String, List<UUID>> peopleMap;
    private Map<String, String> nameMap;
    private Map<UUID, Meeting> meetingMap;
    private MeetingComparator meetingComp;

    public MeetingScheduler(){
        this.peopleMap = new ConcurrentHashMap<>();
        this.meetingMap = new HashMap<>();
        this.nameMap = new HashMap<>();
        this.meetingComp = new MeetingComparator();
    }

    private void validateEmail(String email, boolean shouldExist){
        if(this.peopleMap.containsKey(email) ^ shouldExist){
            String err = shouldExist ?
                String.format("The email (%s) is already in use.", email) :
                String.format("The email (%s) does not match a person.", email);
            throw new IllegalArgumentException(err);
        }
    }

    @Override
    public Person CreatePerson(String name, String email) throws IllegalArgumentException {
        validateEmail(email, false);
        peopleMap.put(email, new ArrayList<UUID>());
        nameMap.put(email, name);
        return new Person(name, email);
    }

    private void tryBook(Person person, UUID meetingID) throws IllegalArgumentException{
        List<UUID> meetings = peopleMap.get(person.email());
        int idx = Collections.binarySearch(meetings, meetingID, meetingComp);
        if(idx >= 0) throw new IllegalArgumentException(
            String.format("%s (%s) has a conflicting meeting at this time.", person.name(), person.email())
        );
        int insertionPoint = -(idx+1);
        meetings.add(insertionPoint, meetingID);
    }

    private void cancel(Person person, UUID meetingID){
        List<UUID> meetings = peopleMap.get(person.email());
        int idx = Collections.binarySearch(meetings, meetingID, meetingComp);
        if(idx < 0) return;
        meetings.remove(idx);
    }

    private void validateTimeslot(Calendar timeslot){
        int
        hour = timeslot.get(Calendar.HOUR_OF_DAY),
        minute = timeslot.get(Calendar.MINUTE),
        second = timeslot.get(Calendar.SECOND);
        if(minute > 0 || second > 0)
            throw new IllegalArgumentException(
                String.format("Meetings must start on the hour. Time given: %d.2:%d.2:%d.2", hour, minute, second)
            );
    }

    @Override
    public Meeting CreateMeeting(Collection<String> emails, Calendar timeslot) throws RuntimeException {
        validateTimeslot(timeslot);
        if(emails.isEmpty()) throw new IllegalArgumentException("Meeting with no person cannot be created.");
        emails.forEach(e -> validateEmail(e, true));

        List<Person> people = emails.parallelStream().map(e -> new Person(nameMap.get(e), e)).toList();
        Meeting m = new Meeting(timeslot, people);
        UUID uuid = UUID.randomUUID();
        meetingMap.put(uuid, m);

        try {
            people.parallelStream().forEach(p -> tryBook(p, uuid));
        } catch (IllegalArgumentException e) {
            people.parallelStream().forEach(p -> cancel(p, uuid));
            meetingMap.remove(uuid);
            throw e;
        }
        return m;
        
    }

    @Override
    public Stream<Meeting> ShowSchedule(String email) throws IllegalArgumentException {
        validateEmail(email, true);
        return peopleMap.get(email).parallelStream().map(meetingMap::get);
    }

    @Override
    public Stream<Meeting> ShowSchedule(Person person) throws IllegalArgumentException {
        return ShowSchedule(person.email());
    }

    @Override
    public Stream<Calendar> SuggestTimeslot(Collection<String> emails) {
        Calendar now = Calendar.getInstance(), end = Calendar.getInstance();
        end.add(Calendar.WEEK_OF_YEAR, 1);
        return SuggestTimeslot(emails, now, end);
    }

    private Calendar adjustTime(Calendar timeslot, int shift){
        int
        hour = timeslot.get(Calendar.HOUR_OF_DAY),
        date = timeslot.get(Calendar.DAY_OF_MONTH),
        month = timeslot.get(Calendar.MONTH),
        year = timeslot.get(Calendar.YEAR);
        Calendar adjustedTime = (Calendar)timeslot.clone();
        adjustedTime.set(year, month, date, hour, 0, 0);
        adjustedTime.add(Calendar.HOUR_OF_DAY, shift);
        return adjustedTime;
    }

    private Stream<Calendar> subspan(String email, Calendar after, Calendar before){
        return peopleMap.get(email).stream().map(meetingMap::get).map(m -> m.timeslot());
        // List<UUID> meetings = peopleMap.get(email);
        // UUID dummy1 = UUID.randomUUID(), dummy2 = UUID.randomUUID();
        // meetingMap.put(dummy1, new Meeting(after, new ArrayList<>()));
        // meetingMap.put(dummy2, new Meeting(before, new ArrayList<>()));
        // int startIdx = Collections.binarySearch(meetings, dummy1);
        // int endIdx = Collections.binarySearch(meetings, dummy2);
        // startIdx = (startIdx < 0) ? -(startIdx+1) : startIdx;
        // endIdx = (endIdx < 0) ? -(endIdx+1) : endIdx;
        // meetingMap.remove(dummy1); meetingMap.remove(dummy2);
        // return
        //     meetings
        //     .subList(startIdx, endIdx)
        //     .parallelStream()
        //     .map(id -> meetingMap.get(id).timeslot())
        //     .toList();
    }

    @Override
    public Stream<Calendar> SuggestTimeslot(Collection<String> emails, Calendar startAfter, Calendar endBefore) {
        emails.forEach(e -> validateEmail(e, true));
        Calendar after = adjustTime(startAfter, 1), before = adjustTime(endBefore, -1);
        if(after.compareTo(before) >= 0)
            throw new IllegalArgumentException("'endBefore' must indicate a time at least 2 hours later than 'startAfter'.");
        
        SortedSet<Calendar> suggestions = new ConcurrentSkipListSet<>();
        while(after.compareTo(before) <= 0){
            suggestions.add((Calendar)after.clone());
            after.add(Calendar.HOUR, 1);
        }

        emails.parallelStream().forEach(e -> subspan(e, after, before).forEach(suggestions::remove));

        return suggestions.stream();
    }
    
}
