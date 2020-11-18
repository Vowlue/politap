package cse416.districting.model;

import java.util.List;

import cse416.districting.Enums.States;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Districting{
    private int id;
    private States state;
    private List<District> districts;
}