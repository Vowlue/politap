package cse416.districting;

import org.json.simple.JSONObject;

import cse416.districting.Enums.States;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StateObject {
    private States state;
    private long population;
    private Districting defaultDistricting;
    private JSONObject map;
}