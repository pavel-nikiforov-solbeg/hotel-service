package com.example.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrivalTime {

    @NotBlank
    @Column(name = "arrival_check_in")
    private String checkIn;

    @Column(name = "arrival_check_out")
    private String checkOut;
}
