import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.hamcrest.MatcherAssert;

import org.junit.Test;

public class JUnitTests {
    private final Person me = new Person("Morten Clausen", "morten.t.clausen@hotmail.com"),
    someoneElse = new Person("Someone Else", "somemail@mailserver.domain");
    @Test
    public void createPerson(){
        Scheduler scheduler = new MeetingScheduler();
        scheduler.CreatePerson("Morten Clausen", "morten.t.clausen@hotmail.com");
        MatcherAssert.assertThat("A new person was created", true);
        assertThrows(
            "Emails must be unique",
            IllegalArgumentException.class,
            () -> scheduler.CreatePerson("Someone else", "morten.t.clausen@hotmail.com")
        );
    }

    @Test
    public void createMeeting(){
        Scheduler scheduler = new MeetingScheduler();
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        List<String> emails = new LinkedList<>();
        assertThrows(
            "No empty meetings allowed",
            IllegalArgumentException.class, 
            () -> scheduler.CreateMeeting(emails, startTime)
        );
        
        emails.add(me.email());
        assertThrows(
            "Meeting with non-existant mail",
            IllegalArgumentException.class,
            () -> scheduler.CreateMeeting(emails, startTime)
        );

        scheduler.CreatePerson(me.name(), me.email());
        scheduler.CreateMeeting(emails, startTime);
        MatcherAssert.assertThat(
            "I should now have a meeting at the specified time",
            scheduler.ShowSchedule(me).findFirst().get().timeslot().compareTo(startTime) == 0
        );

        assertThrows(
            "A person cannot be double-booked for a meeting",
            IllegalArgumentException.class,
            () -> scheduler.CreateMeeting(emails, startTime)
        );

        startTime.set(Calendar.MINUTE,5);
        assertThrows(
            "Meetings can only start on the hour",
            IllegalArgumentException.class,
            () -> scheduler.CreateMeeting(emails, startTime)
        );
    }

    @Test
    public void showSchedule(){
        Scheduler scheduler = new MeetingScheduler();
        assertThrows(
            "Schedule of non-existant mail",
            IllegalArgumentException.class,
            () -> scheduler.ShowSchedule(me)
        );

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        List<String> emails = new LinkedList<>();
        emails.add(me.email());
        scheduler.CreatePerson(me.name(), me.email());
        scheduler.CreateMeeting(emails, startTime);
        MatcherAssert.assertThat(
            "I should now have a meeting at the specified time",
            scheduler.ShowSchedule(me).findFirst().get().timeslot().compareTo(startTime) == 0
        );
    }

    @Test
    public void suggestTimeslot(){
        Scheduler scheduler = new MeetingScheduler();
        scheduler.CreatePerson(me.name(), me.email());
        scheduler.CreatePerson(someoneElse.name(), someoneElse.email());
        Calendar startTime1 = Calendar.getInstance();
        startTime1.set(Calendar.MINUTE, 0);
        startTime1.set(Calendar.SECOND, 0);

        Calendar startTime2 = (Calendar)startTime1.clone();
        startTime2.add(Calendar.HOUR_OF_DAY, 2);

        List<String> justme = new LinkedList<>(), justelse = new LinkedList<>(), both = new LinkedList<>();
        justme.add(me.email()); justelse.add(someoneElse.email()); both.add(me.email()); both.add(someoneElse.email());

        scheduler.CreateMeeting(justme, startTime1);
        scheduler.CreateMeeting(justelse, startTime2);

        Calendar after = (Calendar)startTime1.clone(), before = (Calendar)startTime2.clone();
        after.add(Calendar.HOUR_OF_DAY, -2); before.add(Calendar.HOUR_OF_DAY, 2);

        // System.err.println("After:"+after.get(Calendar.HOUR_OF_DAY));
        // System.err.println("Before:"+before.get(Calendar.HOUR_OF_DAY));
        List<Calendar> suggestions = scheduler.SuggestTimeslot(both, after, before).toList();
        // suggestions.forEach(c -> System.err.println(c.get(Calendar.HOUR_OF_DAY)));

        
        MatcherAssert.assertThat(
            "Suggestions must not conflict with prior meetings",
            suggestions.stream().allMatch(s -> s.compareTo(startTime1) != 0 && s.compareTo(startTime2) != 0)
        );
        MatcherAssert.assertThat(
            "Suggestions must be within the supplied time frame",
            suggestions.stream().allMatch(s -> s.after(after) && s.before(before)));

        MatcherAssert.assertThat("At least one timeslot was suggested", suggestions.size() > 0);

        assertThrows("Time frame must have a positive length",
            IllegalArgumentException.class,
            () -> scheduler.SuggestTimeslot(both, before, after)
        );
    }
}
