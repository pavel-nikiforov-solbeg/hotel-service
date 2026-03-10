package com.example.hotelservice;

import com.example.hotelservice.dto.*;
import com.example.hotelservice.entity.Address;
import com.example.hotelservice.entity.ArrivalTime;
import com.example.hotelservice.entity.Contacts;
import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.exception.HotelNotFoundException;
import com.example.hotelservice.exception.InvalidHistogramParameterException;
import com.example.hotelservice.mapper.HotelMapper;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.service.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelBriefDto briefDto;
    private HotelFullDto fullDto;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Grand Hotel")
                .description("Luxury hotel")
                .brand("Marriott")
                .address(Address.builder().houseNumber(9).street("Main St").city("Minsk").country("Belarus").postCode("220001").build())
                .contacts(Contacts.builder().phone("+375-17-000-00-00").email("info@grand.by").build())
                .arrivalTime(ArrivalTime.builder().checkIn("14:00").checkOut("12:00").build())
                .amenities(new ArrayList<>(List.of("Free WiFi", "Parking")))
                .build();

        briefDto = new HotelBriefDto(1L, "Grand Hotel", "Luxury hotel", "9 Main St, Minsk, 220001, Belarus", "+375-17-000-00-00");

        fullDto = new HotelFullDto(
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
    void getAllHotels_returnsList() {
        when(hotelRepository.findAll()).thenReturn(List.of(hotel));
        when(hotelMapper.toBriefDto(hotel)).thenReturn(briefDto);

        List<HotelBriefDto> result = hotelService.getAllHotels();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Grand Hotel");
    }

    @Test
    void getHotelById_found() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toFullDto(hotel)).thenReturn(fullDto);

        HotelFullDto result = hotelService.getHotelById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.brand()).isEqualTo("Marriott");
    }

    @Test
    void getHotelById_notFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.getHotelById(99L))
                .isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    void createHotel_savesAndReturns() {
        HotelCreateDto createDto = new HotelCreateDto("Grand Hotel", null, "Marriott", null, null, null);

        when(hotelMapper.toEntity(createDto)).thenReturn(hotel);
        when(hotelRepository.save(hotel)).thenReturn(hotel);
        when(hotelMapper.toBriefDto(hotel)).thenReturn(briefDto);

        HotelBriefDto result = hotelService.createHotel(createDto);

        assertThat(result.name()).isEqualTo("Grand Hotel");
        verify(hotelRepository).save(hotel);
    }

    @Test
    void addAmenities_found() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        hotelService.addAmenities(1L, List.of("Spa", "Pool"));

        assertThat(hotel.getAmenities()).contains("Spa", "Pool");
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void addAmenities_notFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.addAmenities(99L, List.of("Spa")))
                .isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    void getHistogram_brand() {
        when(hotelRepository.countByBrand()).thenReturn(List.of(
                new HistogramEntry("Marriott", 3L),
                new HistogramEntry("Hilton", 2L)));

        Map<String, Long> result = hotelService.getHistogram("brand");

        assertThat(result).containsEntry("Marriott", 3L).containsEntry("Hilton", 2L);
    }

    @Test
    void getHistogram_city() {
        when(hotelRepository.countByCity()).thenReturn(List.of(new HistogramEntry("Minsk", 10L)));

        Map<String, Long> result = hotelService.getHistogram("city");

        assertThat(result).containsEntry("Minsk", 10L);
    }

    @Test
    void getHistogram_country() {
        when(hotelRepository.countByCountry()).thenReturn(List.of(new HistogramEntry("Belarus", 10L)));

        Map<String, Long> result = hotelService.getHistogram("country");

        assertThat(result).containsEntry("Belarus", 10L);
    }

    @Test
    void getHistogram_amenities() {
        when(hotelRepository.countByAmenity()).thenReturn(List.of(new HistogramEntry("Free WiFi", 10L)));

        Map<String, Long> result = hotelService.getHistogram("amenities");

        assertThat(result).containsEntry("Free WiFi", 10L);
    }

    @Test
    void getHistogram_unknown_throwsException() {
        assertThatThrownBy(() -> hotelService.getHistogram("unknown"))
                .isInstanceOf(InvalidHistogramParameterException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchHotels_withFilters() {
        when(hotelRepository.findAll(any(Specification.class))).thenReturn(List.of(hotel));
        when(hotelMapper.toBriefDto(hotel)).thenReturn(briefDto);

        List<HotelBriefDto> result = hotelService.searchHotels("Grand", "Marriott", "Minsk", "Belarus", List.of("Free WiFi"));

        assertThat(result).hasSize(1);
    }
}
