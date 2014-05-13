package com.sos.hibernate.classes;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @version 16.01.14 15:07
 * @uthor stefan.schaedlich@sos-berlin.com
 */
public class ClassListTest {

    private final static List<String> expectedClasses = Arrays.asList(
            "com.sos.scheduler.db.SchedulerInstancesDBItem"
    );

    @Test
    public void test() {
        ClassList classList = new ClassList();
        classList.addClassIfExist("com.sos.scheduler.db.SchedulerInstancesDBItem");
        classList.addClassIfExist("sos.ftphistory.db.JadeFilesDBItem");                 // is not in classpath
        assertEquals(expectedClasses.size(),classList.getClasses().size());
        for(Class c : classList.getClasses()) {
            assertTrue(expectedClasses.contains(c.getName()));
        }
    }
}
