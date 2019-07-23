package com.kasian.trackme;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class})
public class UtilsTest {

    @Before
    public void setUp() throws Exception {
        //PowerMockito.mockStatic(Calendar.class);
    }

    @Test
    public void testDate() {
        System.out.println("OK");
        int hour = 6;
        int minute = 20;
        int sec = 35;

        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 6, 23, hour, minute, sec);

        PowerMockito.mockStatic(Calendar.class);
        when(Calendar.getInstance()).thenReturn(calendar);

         Utils.calculateStartDelay();
    }
}
