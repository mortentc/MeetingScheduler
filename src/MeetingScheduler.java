import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private Map<UUID, Meeting> meetingMap;
    private MeetingComparator meetingComp;

    public MeetingScheduler(){
        this.peopleMap = new ConcurrentHashMap<>();
        this.meetingMap = new HashMap<>();
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
        this.peopleMap.put(email, new ArrayList<UUID>());
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

    @Override
    public Meeting CreateMeeting(Collection<Person> people, Calendar timeslot) throws RuntimeException {
        int
        hour = timeslot.get(Calendar.HOUR_OF_DAY),
        minute = timeslot.get(Calendar.MINUTE),
        second = timeslot.get(Calendar.SECOND);
        if(minute > 0 || second > 0)
            throw new IllegalArgumentException(
                String.format("Meetings must start on the hour. Time given: %d.2:%d.2:%d.2", hour, minute, second)
            );
        people.forEach(p -> validateEmail(p.email(), true));

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
    public Stream<Date> SuggestTimeslot(Collection<String> emails) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'SuggestTimeslot'");
    }
    
}
