/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonschema.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.processing.LogLevel;
import com.github.fge.jsonschema.processing.ProcessingException;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class GenericProcessingReportTest
{
    /*
     * All levels except fatal
     */
    private static final EnumSet<LogLevel> LEVELS
        = EnumSet.complementOf(EnumSet.of(LogLevel.FATAL));

    @DataProvider
    public Iterator<Object[]> getLogLevels()
    {
        final List<Object[]> list = Lists.newArrayList();

        for (final LogLevel level: LEVELS)
            list.add(new Object[] { level });

        // We don't want the values in the same order repeatedly, so...
        Collections.shuffle(list);

        return list.iterator();
    }

    @Test(dataProvider = "getLogLevels")
    public void logLevelIsObeyed(final LogLevel wantedLevel)
        throws ProcessingException
    {
        final ProcessingMessage msg = new ProcessingMessage();
        final int nrInvocations  = LEVELS.size() - wantedLevel.ordinal();
        final GenericProcessingReport ctx = spy(new TestProcessingReport());

        ctx.setLogLevel(wantedLevel);

        for (final LogLevel level: LEVELS)
            ctx.doLog(level, msg);

        verify(ctx, times(nrInvocations)).log(msg);
    }

    @Test(dataProvider = "getLogLevels")
    public void successIsCorrectlyReported(final LogLevel wantedLevel)
        throws ProcessingException
    {
        final GenericProcessingReport ctx = new TestProcessingReport();
        final ProcessingMessage msg = new ProcessingMessage();

        final boolean expected = wantedLevel.compareTo(LogLevel.ERROR) < 0;

        ctx.doLog(wantedLevel, msg);

        final boolean actual = ctx.isSuccess();
        final String errmsg = "incorrect status report for level "
            + wantedLevel;

        assertEquals(actual, expected, errmsg);
    }

    @Test(dataProvider = "getLogLevels")
    public void levelIsCorrectlySetInMessages(final LogLevel wantedLevel)
        throws ProcessingException
    {
        final GenericProcessingReport ctx = new TestProcessingReport();
        final ProcessingMessage msg = new ProcessingMessage();
        ctx.setLogLevel(wantedLevel);
        ctx.doLog(wantedLevel, msg);

        final JsonNode node = msg.asJson().path("level");
        assertTrue(node.isTextual());
        assertEquals(node.textValue(), wantedLevel.toString());
    }

    @Test(dataProvider = "getLogLevels")
    public void exceptionThresholdIsObeyed(final LogLevel wantedLevel)
    {
        final EnumSet<LogLevel> notThrown = EnumSet.noneOf(LogLevel.class);

        for (final LogLevel level: LEVELS) {
            if (level.compareTo(wantedLevel) >= 0)
                break;
            notThrown.add(level);
        }

        final EnumSet<LogLevel> thrown = EnumSet.complementOf(notThrown);

        final GenericProcessingReport ctx = new TestProcessingReport();
        final ProcessingMessage msg = new ProcessingMessage();

        ctx.setExceptionThreshold(wantedLevel);

        for (final LogLevel safe: notThrown)
            try {
                ctx.doLog(safe, msg);
            } catch (ProcessingException ignored) {
                fail("exception thrown at level " + safe
                    + " whereas exception threshold is " + wantedLevel + '!');
            }

        for (final LogLevel oops: thrown)
            try {
                ctx.doLog(oops, msg);
                fail("exception not thrown at level " + oops
                    + " whereas exception threshold is " + wantedLevel + '!');
            } catch (ProcessingException ignored) {
            }
    }

    private static class TestProcessingReport
        extends GenericProcessingReport
    {

        @Override
        public void log(final ProcessingMessage msg)
        {
        }
    }
}