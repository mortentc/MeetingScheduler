import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface Scheduler {
    public Person CreatePerson(String name, String email) throws RuntimeException;
    public Meeting CreateMeeting(Collection<Person> people, Date timeslot) throws RuntimeException;
    public List<Meeting> ShowSchedule(String email);
    public List<Meeting> ShowSchedule(Person person);
    public List<Date> SuggestTimeslot(Collection<String> emails);
}