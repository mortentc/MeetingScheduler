import java.util.Calendar;
import java.util.Collection;
import java.util.stream.Stream;

public interface Scheduler {
    public Person CreatePerson(String name, String email) throws IllegalArgumentException;
    public Meeting CreateMeeting(Collection<String> emails, Calendar timeslot) throws IllegalArgumentException;
    public Stream<Meeting> ShowSchedule(String email) throws IllegalArgumentException;
    public Stream<Meeting> ShowSchedule(Person person) throws IllegalArgumentException;
    public Stream<Calendar> SuggestTimeslot(Collection<String> emails) throws IllegalArgumentException;
    public Stream<Calendar> SuggestTimeslot(Collection<String> emails, Calendar after, Calendar before) throws IllegalArgumentException;
}