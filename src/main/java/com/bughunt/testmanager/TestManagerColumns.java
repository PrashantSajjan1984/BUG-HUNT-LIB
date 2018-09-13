package com.bughunt.testmanager;

public enum TestManagerColumns {
	SL_NO("SlNo"), TEST_CASE_NAME("Test Case Name"), EXECUTE("Execute"), BROWSER("Browser");
	private final String name;
	TestManagerColumns(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
