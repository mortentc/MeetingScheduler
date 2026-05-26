import java.util.Date;
import java.util.List;

public record Meeting(Date timeslot, List<Person> people) {}