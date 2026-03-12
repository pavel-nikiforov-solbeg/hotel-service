package com.example.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalTime {

    @NotBlank
    @Column(name = "arrival_check_in")
    private String checkIn;

    @NotBlank
    @Column(name = "arrival_check_out")
    private String checkOut;
}
