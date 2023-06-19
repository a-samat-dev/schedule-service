package kz.smarthealth.scheduleservice.util;

public enum MessageSource {

    RESERVED_SCHEDULES_EXIST("There are already reserved time periods.");

    private String text;

    MessageSource(String text) {
        this.text = text;
    }

    public String getText(String... params) {
        return String.format(this.text, params);
    }
}
