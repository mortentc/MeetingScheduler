import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MeetingScheduler implements Scheduler {

    @Override
    public Person CreatePerson(String name, String email) throws RuntimeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'CreatePerson'");
    }

    @Override
    public Meeting CreateMeeting(Collection<Person> people, Date timeslot) throws RuntimeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'CreateMeeting'");
    }

    @Override
    public List<Meeting> ShowSchedule(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ShowSchedule'");
    }

    @Override
    public List<Meeting> ShowSchedule(Person person) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ShowSchedule'");
    }

    @Override
    public List<Date> SuggestTimeslot(Collection<String> emails) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'SuggestTimeslot'");
    }
    
}
