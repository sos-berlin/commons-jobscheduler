package com.sos.scheduler;

public class SOSJobSchedulerGlobal {

    public static enum JOB_CRITICALITY {
        NORMAL {

            public String toString() {
                return "normal";
            }
        },
        MINOR {

            public String toString() {
                return "minor";
            }
        },
        MAJOR {

            public String toString() {
                return "major";

            }
        }
    }
}
