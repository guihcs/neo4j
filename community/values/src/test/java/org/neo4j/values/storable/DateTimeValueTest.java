/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.values.storable;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.neo4j.values.storable.AssertingStructureBuilder.asserting;
import static org.neo4j.values.storable.DateTimeValue.builder;
import static org.neo4j.values.storable.DateTimeValue.datetime;
import static org.neo4j.values.storable.DateTimeValue.parse;
import static org.neo4j.values.storable.DateValue.date;
import static org.neo4j.values.storable.FrozenClockRule.assertEqualTemporal;
import static org.neo4j.values.storable.InputMappingStructureBuilder.fromValues;
import static org.neo4j.values.storable.LocalDateTimeValue.inUTC;
import static org.neo4j.values.storable.LocalDateTimeValue.localDateTime;
import static org.neo4j.values.storable.LocalTimeValue.localTime;
import static org.neo4j.values.storable.TimeValue.time;
import static org.neo4j.values.storable.TimeValueTest.inUTC;
import static org.neo4j.values.storable.TimeValueTest.orFail;
import static org.neo4j.values.storable.Values.stringValue;
import static org.neo4j.values.utils.AnyValueTestUtil.assertThrows;

public class DateTimeValueTest
{
    @Rule
    public final FrozenClockRule clock = new FrozenClockRule();

    @Test
    public void shouldParseDateTime() throws Exception
    {
        assertEquals(
                datetime( date( 2017, 12, 17 ), time( 17, 14, 35, 123456789, UTC ) ),
                parse( "2017-12-17T17:14:35.123456789", inUTC ) );
        assertEquals(
                datetime( date( 2017, 12, 17 ), time( 17, 14, 35, 123456789, UTC ) ),
                parse( "2017-12-17T17:14:35.123456789Z", orFail ) );
        assertEquals(
                datetime( date( 2017, 12, 17 ), time( 17, 14, 35, 123456789, UTC ) ),
                parse( "2017-12-17T17:14:35.123456789+0000", orFail ) );
        assertEquals(
                datetime( date( 10000, 12, 17 ), time( 17, 14, 35, 123456789, UTC ) ),
                parse( "+10000-12-17T17:14:35.123456789+0000", orFail ) );
        assertEquals(
                datetime( date( -1, 12, 17 ), time( 17, 14, 35, 123456789, UTC ) ),
                parse( "-1-12-17T17:14:35.123456789+0000", orFail ) );
    }

    @Ignore
    public void shouldSupportLeadSeconds() throws Exception
    {
        // Leap second according to https://www.timeanddate.com/time/leap-seconds-future.html
        assertEquals( datetime( 2016, 12, 31, 23, 59, 60, 0, UTC ), parse( "2016-12-31T23:59:60Z", orFail ) );
    }

    @Test
    public void shouldRejectInvalidDateTimeString() throws Exception
    {
        // Wrong year
        assertThrows( DateTimeException.class, () -> parse( "10000-12-17T17:14:35", inUTC ) );
        assertThrows( DateTimeException.class, () -> parse( "10000-12-17T17:14:35Z", orFail ) );

        // Wrong month
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-13-17T17:14:35", inUTC ) ).getMessage(),
                startsWith( "Invalid value for MonthOfYear" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-00-17T17:14:35", inUTC ) ).getMessage(),
                startsWith( "Invalid value for MonthOfYear" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-13-17T17:14:35Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for MonthOfYear" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-00-17T17:14:35Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for MonthOfYear" ) );

        // Wrong day of month
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-32T17:14:35", inUTC ) ).getMessage(),
                startsWith( "Invalid value for DayOfMonth" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-00T17:14:35", inUTC ) ).getMessage(),
                startsWith( "Invalid value for DayOfMonth" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-32T17:14:35Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for DayOfMonth" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-00T17:14:35Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for DayOfMonth" ) );

        // Wrong hour
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-17T24:14:35", inUTC ) ).getMessage(),
                startsWith( "Invalid value for HourOfDay" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-17T24:14:35Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for HourOfDay" ) );

        // Wrong minute
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-17T17:60:35", inUTC ) ).getMessage(),
                startsWith( "Invalid value for MinuteOfHour" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-17T17:60:35Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for MinuteOfHour" ) );

        // Wrong second
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-17T17:14:61", inUTC ) ).getMessage(),
                startsWith( "Invalid value for SecondOfMinute" ) );
        assertThat( assertThrows( DateTimeException.class, () -> parse( "2017-12-17T17:14:61Z", orFail ) ).getMessage(),
                startsWith( "Invalid value for SecondOfMinute" ) );
    }

    @Test
    public void shouldWriteDateTime() throws Exception
    {
        // given
        for ( DateTimeValue value : new DateTimeValue[] {
                datetime( date( 2017, 3, 26 ), localTime( 1, 0, 0, 0 ), ZoneId.of( "Europe/Stockholm" ) ),
                datetime( date( 2017, 3, 26 ), localTime( 2, 0, 0, 0 ), ZoneId.of( "Europe/Stockholm" ) ),
                datetime( date( 2017, 3, 26 ), localTime( 3, 0, 0, 0 ), ZoneId.of( "Europe/Stockholm" ) ),
                datetime( date( 2017, 10, 29 ), localTime( 2, 0, 0, 0 ), ZoneId.of( "Europe/Stockholm" ) ),
                datetime( date( 2017, 10, 29 ), localTime( 3, 0, 0, 0 ), ZoneId.of( "Europe/Stockholm" ) ),
                datetime( date( 2017, 10, 29 ), localTime( 4, 0, 0, 0 ), ZoneId.of( "Europe/Stockholm" ) ),
        } )
        {
            List<DateTimeValue> values = new ArrayList<>( 1 );
            List<LocalDateTimeValue> locals = new ArrayList<>( 1 );
            ValueWriter<RuntimeException> writer = new ThrowingValueWriter.AssertOnly()
            {
                @Override
                public void writeDateTime( long epochSecondUTC, int nano, int offsetSeconds ) throws RuntimeException
                {
                    values.add( datetime( epochSecondUTC, nano, ZoneOffset.ofTotalSeconds( offsetSeconds ) ) );
                    locals.add( localDateTime( epochSecondUTC, nano ) );
                }

                @Override
                public void writeDateTime( long epochSecondUTC, int nano, String zoneId ) throws RuntimeException
                {
                    values.add( datetime( epochSecondUTC, nano, ZoneId.of( zoneId ) ) );
                    locals.add( localDateTime( epochSecondUTC, nano ) );
                }
            };

            // when
            value.writeTo( writer );

            // then
            assertEquals( singletonList( value ), values );
            assertEquals( singletonList( inUTC( value ) ), locals );
        }
    }

    @Test
    @FrozenClockRule.TimeZone( "Europe/Stockholm" )
    public void shouldAcquireCurrentDateTime() throws Exception
    {
        assertEqualTemporal(
                datetime( ZonedDateTime.now( clock ) ),
                DateTimeValue.now( clock ) );

        assertEqualTemporal( // Using the named UTC timezone
                datetime( ZonedDateTime.now( clock.withZone( "UTC" ) ) ),
                DateTimeValue.now( clock, "UTC" ) );

        assertEqualTemporal( // Using the timezone defined as 0 hours offset from UTC
                datetime( ZonedDateTime.now( clock.withZone( UTC ) ) ),
                DateTimeValue.now( clock, "Z" ) );
    }

    @Test
    @FrozenClockRule.TimeZone( {"Europe/Stockholm", "America/Los_Angeles"} )
    public void shouldCopyDateTime() throws Exception
    {
        assertEqualTemporal(
                datetime( ZonedDateTime.now( clock ) ),
                builder( clock ).add( "datetime", datetime( ZonedDateTime.now( clock ) ) ).build() );
        assertEqualTemporal( // TODO: change some stuff here...
                datetime( ZonedDateTime.now( clock ) ),
                builder( clock )
                        .add( "datetime", localDateTime( LocalDateTime.now( clock ) ) )
                        .build() );
        assertEqualTemporal(
                datetime( ZonedDateTime.now( clock ).withZoneSameLocal( ZoneId.of( "America/New_York" ) ) ),
                builder( clock )
                        .add( "datetime", localDateTime( LocalDateTime.now( clock ) ) )
                        .add( "timezone", stringValue( "America/New_York" ) )
                        .build() );
    }

    @Test
    @FrozenClockRule.TimeZone( "Europe/Stockholm" )
    public void shouldConstructDateTimeFromComponents() throws Exception
    {
        assertEqualTemporal(
                parse( "2018-01-10T10:35:57", clock::getZone ),
                fromValues( builder( clock ) )
                        .add( "year", 2018 )
                        .add( "month", 1 )
                        .add( "day", 10 )
                        .add( "hour", 10 )
                        .add( "minute", 35 )
                        .add( "second", 57 )
                        .build() );
    }

    @Test
    public void shouldRejectInvalidFieldCombinations() throws Exception
    {
        asserting( fromValues( builder( clock ) ) )
                .add( "year", 2018 )
                .add( "month", 12 )
                .add( "dayOfWeek", 5 )
                .assertThrows( IllegalArgumentException.class, "Cannot assign dayOfWeek to calendar date." );
        asserting( fromValues( builder( clock ) ) )
                .add( "datetime", localDateTime( LocalDateTime.now( clock ) ) )
                .add( "day", 12 )
                .assertThrows( IllegalArgumentException.class, "Cannot assign day when selecting datetime." );
        asserting( fromValues( builder( clock ) ) )
                .add( "datetime", localDateTime( LocalDateTime.now( clock ) ) )
                .add( "hour", 12 )
                .assertThrows( IllegalArgumentException.class, "Cannot assign hour when selecting datetime." );
        asserting( fromValues( builder( clock ) ) )
                .add( "year", 2018 )
                .add( "week", 12 )
                .add( "day", 12 )
                .assertThrows( IllegalArgumentException.class, "Cannot assign day to week date." );
        asserting( fromValues( builder( clock ) ) )
                .add( "year", 2018 )
                .add( "ordinalDay", 12 )
                .add( "dayOfWeek", 1 )
                .assertThrows( IllegalArgumentException.class, "Cannot assign dayOfWeek to ordinal date." );
        asserting( fromValues( builder( clock ) ) )
                .add( "year", 2018 )
                .add( "month", 12 )
                .add( "day", 5 )
                .add( "hour", 5 )
                .add( "minute", 5 )
                .add( "second", 5 )
                .add( "millisecond", 1 )
                .add( "nanosecond", 1 )
                .assertThrows( IllegalArgumentException.class, "Cannot assign ..." );
    }

    @Test
    public void shouldRejectInvalidComponentValues() throws Exception
    {
        asserting( fromValues( builder( clock ) ) ).add( "year", 2018 ).add( "moment", 12 ).assertThrows( IllegalArgumentException.class,
                "No such field: moment" );
        asserting( fromValues( builder( clock ) ) ).add( "year", 2018 ).add( "month", 12 ).add( "day", 5 ).add( "hour", 5 ).add( "minute", 5 ).add( "second",
                5 ).add( "picosecond", 12 ).assertThrows( IllegalArgumentException.class, "No such field: picosecond" );
    }

    @Test
    public void shouldAddDurationToDateTimes() throws Exception
    {
        assertEquals( datetime( date( 2018, 2, 1 ), time( 1, 17, 3, 0, UTC ) ),
                datetime( date( 2018, 1, 1 ), time( 1, 2, 3, 0, UTC ) ).add( DurationValue.duration( 1, 0, 900, 0 ) ) );
        assertEquals( datetime( date( 2018, 2, 28 ), time( 0, 0, 0, 0, UTC ) ),
                datetime( date( 2018, 1, 31 ), time( 0, 0, 0, 0, UTC ) ).add( DurationValue.duration( 1, 0, 0, 0 ) ) );
        assertEquals( datetime( date( 2018, 1, 28 ), time( 0, 0, 0, 0, UTC ) ),
                datetime( date( 2018, 2, 28 ), time( 0, 0, 0, 0, UTC ) ).add( DurationValue.duration( -1, 0, 0, 0 ) ) );
    }

    @Test
    public void shouldReuseInstanceInArithmetics() throws Exception
    {
        final DateTimeValue datetime = datetime( date( 2018, 2, 1 ), time( 1, 17, 3, 0, UTC ) );
        assertSame( datetime,
                datetime.add( DurationValue.duration( 0, 0, 0, 0 ) ) );
    }

    @Test
    public void shouldSubtractDurationFromDateTimes() throws Exception
    {
        assertEquals( datetime( date( 2018, 1, 1 ), time( 1, 2, 3, 0, UTC ) ),
                datetime( date( 2018, 2, 1 ), time( 1, 17, 3, 0, UTC ) ).sub( DurationValue.duration( 1, 0, 900, 0 ) ) );
        assertEquals( datetime( date( 2018, 1, 28 ), time( 0, 0, 0, 0, UTC ) ),
                datetime( date( 2018, 2, 28 ), time( 0, 0, 0, 0, UTC ) ).sub( DurationValue.duration( 1, 0, 0, 0 ) ) );
        assertEquals( datetime( date( 2018, 2, 28 ), time( 0, 0, 0, 0, UTC ) ),
                datetime( date( 2018, 1, 31 ), time( 0, 0, 0, 0, UTC ) ).sub( DurationValue.duration( -1, 0, 0, 0 ) ) );
    }
}
