package cse416.districting;

public class Enums {
    public enum States {
        ARKANSAS, VIRGINIA, SOUTH_CAROLINA
    }
    public enum JobStatus {
        NOT_STARTED, RUNNING, DONE, ERROR
    }
    public enum Demographic {
        WHITE, HISPANIC_OR_LATINO, BLACK_OR_AFRICAN_AMERICAN, ASIAN, AMERICAN_INDIAN_OR_ALASKA_NATIVE, NATIVE_HAWAIIAN_OR_OTHER_PACIFIC_ISLANDER, OTHER, TOTAL
    }
    public enum Compactness {
        NOT, SOMEWHAT, VERY, EXTREMELY
    }

    public static String getStateShortcut(States state) {
        switch(state.toString())
        {
            case "ARKANSAS":
                return "AR";
            case "VIRGINIA":
                return "VA";
            case "SOUTH_CAROLINA":
                return "SC";
            default:
                return null;
        }
    }
}
