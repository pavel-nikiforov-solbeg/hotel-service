package com.example.hotelservice;

import com.example.hotelservice.controller.HotelController;
import com.example.hotelservice.dto.AddressDto;
import com.example.hotelservice.dto.ArrivalTimeDto;
import com.example.hotelservice.dto.ContactsDto;
import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import com.example.hotelservice.exception.HotelNotFoundException;
import com.example.hotelservice.exception.InvalidHistogramParameterException;
import com.example.hotelservice.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HotelService hotelService;

    private HotelBriefDto sampleBrief() {
        return new HotelBriefDto(
                1L,
                "Grand Hotel",
                "Luxury hotel",
                new AddressDto("9", "Main St", "Minsk", "Belarus", "220001"),
                "+375-17-000-00-00");
    }

    private HotelFullDto sampleFull() {
        return new HotelFullDto(
                1L,
                "Grand Hotel",
                "Luxury hotel",
                "Marriott",
                new AddressDto("9", "Main St", "Minsk", "Belarus", "220001"),
                new ContactsDto("+375-17-000-00-00", "info@grand.by"),
                new ArrivalTimeDto(LocalTime.of(14, 0), LocalTime.of(12, 0)),
                Set.of("Free WiFi", "Parking"));
    }

    @Test
    void getAll_returns200_withPagination() throws Exception {
        List<HotelBriefDto> content = List.of(sampleBrief());
        Page<HotelBriefDto> page = new PageImpl<>(content, PageRequest.of(0, 20), 1);

        when(hotelService.getAllHotels(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/property-view/hotels")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Grand Hotel"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getById_found_returns200() throws Exception {
        when(hotelService.getHotelById(1L)).thenReturn(sampleFull());

        mockMvc.perform(get("/property-view/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Marriott"))
                .andExpect(jsonPath("$.amenities", hasItem("Free WiFi")));
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
    void search_returns200_withPagination() throws Exception {
        List<HotelBriefDto> content = List.of(sampleBrief());
        Page<HotelBriefDto> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);

        when(hotelService.searchHotels(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/property-view/search")
                        .param("city", "Minsk")
                        .param("brand", "Marriott")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Grand Hotel"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void createHotel_returns201() throws Exception {
        HotelCreateDto createDto = new HotelCreateDto(
                "Grand Hotel",
                null,
                "Marriott",
                new AddressDto("9", "Main St", "Minsk", "Belarus", "220001"),
                new ContactsDto("+375170000000", "info@grand.by"),
                new ArrivalTimeDto(LocalTime.of(14, 0), LocalTime.of(12, 0)));

        when(hotelService.createHotel(any(HotelCreateDto.class))).thenReturn(sampleBrief());

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
        doNothing().when(hotelService).addAmenities(eq(1L), anySet());

        mockMvc.perform(post("/property-view/hotels/1/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of("Spa", "Pool"))))
                .andExpect(status().isOk());
    }

    @Test
    void addAmenities_notFound_returns404() throws Exception {
        doThrow(new HotelNotFoundException(99L)).when(hotelService).addAmenities(eq(99L), anySet());

        mockMvc.perform(post("/property-view/hotels/99/amenities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of("Spa"))))
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