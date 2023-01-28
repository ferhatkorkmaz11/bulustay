package com.venividicode.bulustay.Enums;

public enum ServerResponseCodes {
    SUCCESS {
        @Override
        public String toString() {
            return "200";
        }
    },
    INVALID_REQUEST_BODY {
        @Override
        public String toString() {
            return "100";
        }
    },
    USER_ALREADY_EXISTS {
        @Override
        public String toString() {
            return "101";
        }
    },
    GENERAL_DATABASE_EXCEPTION {
        @Override
        public String toString() {
            return "102";
        }
    },
    USER_NOT_FOUND {
        @Override
        public String toString() {
            return "103";
        }
    },
    INVALID_CREDENTIALS {
        @Override
        public String toString() {
            return "104";
        }
    },
    ORGANIZER_EVENT_CREATION_ISSUE {
        @Override
        public String toString() {
            return "105";
        }
    },
    NEGATIVE_AGE_ERROR {
        @Override
        public String toString() {
            return "106";
        }
    },
    NEGATIVE_PRICE_ERROR {
        @Override
        public String toString() {
            return "106";
        }
    },
    QUOTA_CHECK_ERROR {
        @Override
        public String toString() {
            return "107";
        }
    },
    EVENT_ID_NOT_FOUND {
        @Override
        public String toString() {
            return "108";
        }
    },
    EVENT_ID_NOT_ENTERED {
        @Override
        public String toString() {
            return "109";
        }
    },
    EVENT_UPDATE_EXCEPTION {
        @Override
        public String toString() {
            return "110";
        }
    },
    DISCOUNT_PERCENTAGE_RANGE_ERROR {
        @Override
        public String toString() {
            return "111";
        }
    },
    ORGANIZER_ID_NOT_FOUND {
        @Override
        public String toString() {
            return "112";
        }
    },
    ORGANIZER_EVENT_MISMATCH {
        @Override
        public String toString() {
            return "113";
        }
    },
    DISCOUNT_ALREADY_EXISTS {
        @Override
        public String toString() {
            return "114";
        }
    },
    ORGANIZER_ID_NOT_ENTERED {
        @Override
        public String toString() {
            return "115";
        }
    }, INVALID_AMOUNT {
        @Override
        public String toString() {
            return "116";
        }
    }, PARTICIPANT_ID_NOT_FOUND {
        @Override
        public String toString() {
            return "117";
        }
    }, NOT_ENOUGH_BALANCE {
        @Override
        public String toString() {
            return "118";
        }
    }, PARTICIPANT_BALANCE_UPDATE_EXCEPTION {
        @Override
        public String toString() {
            return "119";
        }
    }, ORGANIZER_BALANCE_UPDATE_EXCEPTION {
        @Override
        public String toString() {
            return "119";
        }
    }, ALREADY_FOLLOWS_EXCEPTION {
        @Override
        public String toString() {
            return "120";
        }
    }, PARTICIPANT_ID_NOT_ENTERED {
        @Override
        public String toString() {
            return "121";
        }
    }, USER_ID_NOT_ENTERED {
        @Override
        public String toString() {
            return "122";
        }
    },
    EVENT_DATE_PASSED {
        @Override
        public String toString() {
            return "123";
        }
    },
    OWN_EVENT_EXCEPTION {
        @Override
        public String toString() {
            return "124";
        }
    },
    QUOTA_EXCEPTION {
        @Override
        public String toString() {
            return "125";
        }
    },
    AGE_EXCEPTION {
        @Override
        public String toString() {
            return "126";
        }
    },
    OVERLAPPING_EXCEPTION {
        @Override
        public String toString() {
            return "127";
        }
    },
    FUKARA_EXCEPTION {
        @Override
        public String toString() {
            return "128";
        }
    },
    ALREADY_ENROLLED {
        @Override
        public String toString() {
            return "129";
        }
    },
    WRONG_REPORT_TYPE {
        @Override
        public String toString() {
            return "130";
        }
    },
    USER_IS_BANNED {
        @Override
        public String toString() {
            return "131";
        }
    },
    ADMIN_NOT_FOUND {
        @Override
        public String toString() {
            return "132";
        }
    },
    USER_IS_ALREADY_BANNED {
        @Override
        public String toString() {
            return "133";
        }
    },
    USER_IS_NOT_ALREADY_BANNED {
        @Override
        public String toString() {
            return "134";
        }
    },
    NEGATIVE_QUOTA_ERROR {
        @Override
        public String toString() {
            return "135";
        }
    },
    REPORT_ID_NOT_ENTERED {
        @Override
        public String toString() {
            return "136";
        }
    },

}
