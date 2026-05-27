import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public record Meeting(Calendar timeslot, Collection<Person> people) {}