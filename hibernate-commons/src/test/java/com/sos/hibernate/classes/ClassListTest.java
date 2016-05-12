package com.sos.hibernate.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/** @author stefan schaedlich */
public class ClassListTest {

    private static final List<String> EXPECTED_CLASSES = Arrays.asList("com.sos.hibernate.classes.DbItem");

    @Test
    public void test() {
        ClassList classList = new ClassList();
        classList.addClassIfExist("com.sos.scheduler.db.SchedulerInstancesDBItem");
        classList.addClassIfExist("com.sos.hibernate.classes.DbItem");
        assertEquals(EXPECTED_CLASSES.size(), classList.getClasses().size());
        for (Class c : classList.getClasses()) {
            assertTrue(EXPECTED_CLASSES.contains(c.getName()));
        }
    }

}