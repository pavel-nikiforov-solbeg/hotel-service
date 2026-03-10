package com.example.hotelservice;

import com.example.hotelservice.dto.*;
import com.example.hotelservice.exception.HotelNotFoundException;
import com.example.hotelservice.exception.InvalidHistogramParameterException;
import com.example.hotelservice.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HotelControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HotelService hotelService;

    private HotelBriefDto sampleBrief() {
        return new HotelBriefDto(1L, "Grand Hotel", "Luxury hotel", "9 Main St, Minsk, 220001, Belarus", "+375-17-000-00-00");
    }

    private HotelFullDto sampleFull() {
        return new HotelFullDto(
                1L,
                "Grand Hotel",
                "Luxury hotel",
                "Marriott",
                new AddressDto(9, "Main St", "Minsk", "Belarus", "220001"),
                new ContactsDto("+375-17-000-00-00", "info@grand.by"),
                new ArrivalTimeDto("14:00", "12:00"),
                List.of("Free WiFi", "Parking"));
    }

    @Test
    void getAll_returns200() throws Exception {
        when(hotelService.getAllHotels()).thenReturn(List.of(sampleBrief()));

        mockMvc.perform(get("/property-view/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Grand Hotel"));
    }

    @Test
    void getById_found_returns200() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(sampleFull());

        mockMvc.perform(get("/property-view/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Marriott"))
                .andExpect(jsonPath("$.amenities[0]").value("Free WiFi"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(hotelService.getHotelById(99L)).thenThrow(new HotelNotFoundException(99L));

        mockMvc.perform(get("/property-view/hotels/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Hotel not found"))
                .andExpect(jsonPath("$.id").value(99));
    }

    @Test
    void search_returns200() throws Exception {
        when(hotelService.searchHotels(any(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleBrief()));

        mockMvc.perform(get("/property-view/search")
                        .param("city", "Minsk")
                        .param("brand", "Marriott"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Grand Hotel"));
    }

    @Test
    void createHotel_returns201() throws Exception {
        HotelCreateDto createDto = new HotelCreateDto("Grand Hotel", null, "Marriott", null, null, null);
        when(hotelService.createHotel(any())).thenReturn(sampleBrief());

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grand Hotel"));
    }

    @Test
    void createHotel_invalidBody_returns400() throws Exception {
        HotelCreateDto invalid = new HotelCreateDto(null, null, null, null, null, null);

        mockMvc.perform(post("/property-view/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addAmenities_returns200() throws Exception {
        doNothing().when(hotelService).addAmenities(eq(1L), anyList());

        mockMvc.perform(post("/property-view/hotels/1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("Spa", "Pool"))))
                .andExpect(status().isOk());
    }

    @Test
    void addAmenities_notFound_returns404() throws Exception {
        doThrow(new HotelNotFoundException(99L)).when(hotelService).addAmenities(eq(99L), anyList());

        mockMvc.perform(post("/property-view/hotels/99/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("Spa"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHistogram_returns200() throws Exception {
        when(hotelService.getHistogram("brand")).thenReturn(Map.of("Marriott", 3L, "Hilton", 2L));

        mockMvc.perform(get("/property-view/histogram/brand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Marriott").value(3));
    }

    @Test
    void getHistogram_unknownParam_returns400() throws Exception {
        when(hotelService.getHistogram("unknown")).thenThrow(new InvalidHistogramParameterException("unknown"));

        mockMvc.perform(get("/property-view/histogram/unknown"))
                .andExpect(status().isBadRequest());
    }
}
