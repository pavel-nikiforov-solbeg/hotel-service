package com.example.hotelservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArrivalTime {

    @Column(name = "arrival_check_in", nullable = false)
    private LocalTime checkIn;

    @Column(name = "arrival_check_out", nullable = false)
    private LocalTime checkOut;
}
