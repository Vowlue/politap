package cse416.districting.dto;

import cse416.districting.Enums.States;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenericRequest {
    private States state;
    private int ID;
}
