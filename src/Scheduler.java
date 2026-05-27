import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public interface Scheduler {
    public Person CreatePerson(String name, String email) throws RuntimeException;
    public Meeting CreateMeeting(Collection<Person> people, Calendar timeslot) throws RuntimeException;
    public Stream<Meeting> ShowSchedule(String email);
    public Stream<Meeting> ShowSchedule(Person person);
    public Stream<Date> SuggestTimeslot(Collection<String> emails);
}