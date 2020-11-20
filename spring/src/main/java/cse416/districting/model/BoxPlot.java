package cse416.districting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BoxPlot {
    private float q1;
    private float median;
    private float q3;
    private float max;
    private float min;
}
