package com.mokujin.user.model.government;


import lombok.Data;

import java.time.LocalDate;

@Data
public class PlaceOfResidence {

    private LocalDate startDate;

    private LocalDate endDate;

    private Address address;

}
