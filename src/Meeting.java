import java.util.Calendar;
import java.util.Collection;

public record Meeting(Calendar timeslot, Collection<Person> people) {}