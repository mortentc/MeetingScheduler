import java.util.Calendar;
import java.util.Collection;
import java.util.stream.Stream;

public interface Scheduler {
    public Person CreatePerson(String name, String email) throws IllegalArgumentException;
    public Meeting CreateMeeting(Collection<Person> people, Calendar timeslot) throws IllegalArgumentException;
    public Stream<Meeting> ShowSchedule(String email);
    public Stream<Meeting> ShowSchedule(Person person);
    public Stream<Calendar> SuggestTimeslot(Collection<String> emails);
    public Stream<Calendar> SuggestTimeslot(Collection<String> emails, Calendar after, Calendar before);
}