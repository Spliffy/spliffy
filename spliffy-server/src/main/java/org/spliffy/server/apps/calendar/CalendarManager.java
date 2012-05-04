/*
 * Copyright (C) 2012 McEvoy Software Ltd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spliffy.server.apps.calendar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.io.output.NullOutputStream;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spliffy.common.HashUtils;
import org.spliffy.server.db.*;

/**
 *
 * @author brad
 */
public class CalendarManager {

    private static final Logger log = LoggerFactory.getLogger(CalendarManager.class);

    private String defaultColor = "blue";
    
    public Calendar createCalendar(BaseEntity owner, String newName) {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();
        
        Calendar c = new Calendar();
        c.setColor(defaultColor);
        c.setCreatedDate(new Date());
        c.setModifiedDate(new Date());
        c.setName(newName);
        c.setOwner(owner);

        session.save(c);

        tx.commit();
        
        return c;
    }
    
    
    public void delete(CalEvent event) {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        session.delete(event);

        tx.commit();
    }

    public void move(CalEvent event, Calendar destCalendar, String name) {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        if (!name.equals(event.getName())) {
            event.setName(name);
        }

        Calendar sourceCal = event.getCalendar();
        if (destCalendar != sourceCal) {
            sourceCal.getEvents().remove(event);
            event.setCalendar(destCalendar);
            if (destCalendar.getEvents() == null) {
                destCalendar.setEvents(new ArrayList<CalEvent>());
            }
            destCalendar.getEvents().add(event);
            updateCtag(sourceCal);
            updateCtag(destCalendar);
            session.save(sourceCal);
            session.save(destCalendar);
        }

        tx.commit();
    }

    public void copy(CalEvent event, Calendar destCalendar, String name) {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        if (destCalendar.getEvents() == null) {
            destCalendar.setEvents(new ArrayList<CalEvent>());
        }
        CalEvent newEvent = new CalEvent();
        newEvent.setCalendar(destCalendar);
        destCalendar.getEvents().add(newEvent);

        newEvent.setCreatedDate(new Date());
        newEvent.setDescription(event.getDescription());
        newEvent.setEndDate(event.getEndDate());
        newEvent.setModifiedDate(new Date());
        newEvent.setName(name);
        newEvent.setStartDate(event.getStartDate());
        newEvent.setSummary(event.getSummary());
        newEvent.setTimezone(event.getTimezone());
        updateCtag(newEvent);
        session.save(newEvent);

        tx.commit();
    }

    public void delete(Calendar calendar) {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();

        session.delete(calendar);
        tx.commit();
    }

    public CalEvent createEvent(Calendar calendar, String newName, String icalData, String contentType) throws UnsupportedEncodingException {
        Session session = SessionManager.session();
        Transaction tx = session.beginTransaction();
        CalEvent e = new CalEvent();

        ByteArrayInputStream fin = new ByteArrayInputStream(icalData.getBytes("UTF-8"));
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar cal4jCalendar;
        try {
            cal4jCalendar = builder.build(fin);
        } catch (IOException | ParserException ex) {
            throw new RuntimeException(ex);
        }
        setCalendar(cal4jCalendar, e);
        tx.commit();
        return e;
    }

    public net.fortuna.ical4j.model.Calendar getCalendar(CalEvent calEvent) {

        net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
        calendar.getProperties().add(new ProdId("-//ettrema.com//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        String sTimezone = calEvent.getTimezone();
        TimeZone timezone = null;
        if (sTimezone != null && sTimezone.length() > 0) {
            registry.getTimeZone(sTimezone); // Eg Pacific/Auckland
        }
        if (timezone == null) {
            timezone = registry.getTimeZone("Pacific/Auckland");
            log.warn("Couldnt find timezone: " + sTimezone + ", using default: " + timezone);
        }
        VTimeZone tz = timezone.getVTimeZone();
        calendar.getComponents().add(tz);
        net.fortuna.ical4j.model.DateTime start = CalUtils.toCalDateTime(calEvent.getStartDate(), timezone);
        net.fortuna.ical4j.model.DateTime finish = CalUtils.toCalDateTime(calEvent.getEndDate(), timezone);
        String summary = calEvent.getSummary();
        VEvent vevent = new VEvent(start, finish, summary);
        vevent.getProperties().add(new Uid(vevent.getUid().toString()));
        vevent.getProperties().add(tz.getTimeZoneId());
        // initialise as an all-day event..
        //        christmas.getProperties().getProperty( Property.DTSTART ).getParameters().add( Value.DATE );
        calendar.getComponents().add(vevent);
        return calendar;

    }

    public void setCalendar(net.fortuna.ical4j.model.Calendar calendar, CalEvent calEvent) {
        VEvent ev = event(calendar);
        calEvent.setStartDate(ev.getStartDate().getDate());
        Date endDate = null;
        if (ev.getEndDate() != null) {
            endDate = ev.getEndDate().getDate();
        }
        calEvent.setEndDate(endDate);
        String summary = null;
        if (ev.getSummary() != null) {
            summary = ev.getSummary().getValue();
        }
        calEvent.setSummary(summary);
    }

    private VEvent event(net.fortuna.ical4j.model.Calendar cal) {
        return (VEvent) cal.getComponent("VEVENT");
    }

    private void updateCtag(CalEvent event) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());
        HashUtils.appendLine(event.getDescription(), cout);
        HashUtils.appendLine(event.getSummary(), cout);
        HashUtils.appendLine(event.getTimezone(), cout);
        HashUtils.appendLine(event.getStartDate(), cout);
        HashUtils.appendLine(event.getEndDate(), cout);
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        event.setCtag(crc);
        updateCtag(event.getCalendar());
    }

    private void updateCtag(Calendar sourceCal) {
        OutputStream nulOut = new NullOutputStream();
        CheckedOutputStream cout = new CheckedOutputStream(nulOut, new Adler32());

        HashUtils.appendLine(sourceCal.getColor(), cout);
        if (sourceCal.getEvents() != null) {
            for (CalEvent r : sourceCal.getEvents()) {
                String name = r.getName();
                String line = HashUtils.toHashableText(name, r.getCtag(), "");
                HashUtils.appendLine(line, cout);
            }
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        sourceCal.setCtag(crc);
    }

    public String getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(String defaultColor) {
        this.defaultColor = defaultColor;
    }

    
}
