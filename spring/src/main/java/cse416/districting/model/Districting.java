package cse416.districting.model;

import java.util.List;

import cse416.districting.Enums.States;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Districting {
    private int id;
    private States state;
    private List<District> districts;
}
