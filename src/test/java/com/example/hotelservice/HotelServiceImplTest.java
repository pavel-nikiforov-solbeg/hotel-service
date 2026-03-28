package com.example.hotelservice;

import com.example.hotelservice.dto.AddressDto;
import com.example.hotelservice.dto.ArrivalTimeDto;
import com.example.hotelservice.dto.ContactsDto;
import com.example.hotelservice.dto.HistogramEntry;
import com.example.hotelservice.dto.HistogramParam;
import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import com.example.hotelservice.entity.Address;
import com.example.hotelservice.entity.ArrivalTime;
import com.example.hotelservice.entity.Contacts;
import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.exception.HotelNotFoundException;
import com.example.hotelservice.mapper.HotelMapper;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.service.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
                .address(Address.builder().houseNumber("9").street("Main St").city("Minsk").country("Belarus").postCode("220001").build())
                .contacts(Contacts.builder().phone("+375-17-000-00-00").email("info@grand.by").build())
                .arrivalTime(ArrivalTime.builder().checkIn(LocalTime.of(14, 0)).checkOut(LocalTime.of(12, 0)).build())
                .amenities(new HashSet<>(Set.of("free wifi", "parking")))
                .build();

        briefDto = new HotelBriefDto(
                1L,
                "Grand Hotel",
                "Luxury hotel",
                new AddressDto("9", "Main St", "Minsk", "Belarus", "220001"),
                "+375-17-000-00-00");

        fullDto = new HotelFullDto(
                1L,
                "Grand Hotel",
                "Luxury hotel",
                "Marriott",
                new AddressDto("9", "Main St", "Minsk", "Belarus", "220001"),
                new ContactsDto("+375-17-000-00-00", "info@grand.by"),
                new ArrivalTimeDto(LocalTime.of(14, 0), LocalTime.of(12, 0)),
                Set.of("free wifi", "parking"));
    }

    @Test
    void getAllHotels_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(hotel), pageable, 1);

        when(hotelRepository.findAll(pageable)).thenReturn(hotelPage);
        when(hotelMapper.toBriefDto(hotel)).thenReturn(briefDto);

        Page<HotelBriefDto> result = hotelService.getAllHotels(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Grand Hotel");
        assertThat(result.getTotalElements()).isEqualTo(1);
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
        HotelCreateDto createDto = new HotelCreateDto(
                "Grand Hotel", null, "Marriott",
                new AddressDto("9", "Main St", "Minsk", "Belarus", "220001"),
                new ContactsDto("+375170000000", "info@grand.by"),
                new ArrivalTimeDto(LocalTime.of(14, 0), LocalTime.of(12, 0)));

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

        hotelService.addAmenities(1L, Set.of("Spa", "Pool"));

        // Amenities are normalized to lowercase
        assertThat(hotel.getAmenities()).contains("spa", "pool");
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void addAmenities_emptySet_doesNothing() {
        hotelService.addAmenities(1L, Set.of());

        // Early return before DB query — findById must never be called
        verify(hotelRepository, never()).findById(any());
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void addAmenities_nullSet_doesNothing() {
        hotelService.addAmenities(1L, null);

        // Early return before DB query — findById must never be called
        verify(hotelRepository, never()).findById(any());
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void addAmenities_blankAndNullElements_onlyValidAdded() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        Set<String> mixed = new HashSet<>();
        mixed.add(null);
        mixed.add("  ");
        mixed.add("  Pool  ");

        hotelService.addAmenities(1L, mixed);

        assertThat(hotel.getAmenities()).contains("pool");
        assertThat(hotel.getAmenities()).doesNotContain("  ", "  Pool  ");
    }

    @Test
    void addAmenities_notFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelService.addAmenities(99L, Set.of("Spa")))
                .isInstanceOf(HotelNotFoundException.class);
    }

    @Test
    void getHistogram_brand() {
        when(hotelRepository.countByBrand()).thenReturn(List.of(
                new HistogramEntry("Marriott", 3L),
                new HistogramEntry("Hilton", 2L)));

        Map<String, Long> result = hotelService.getHistogram(HistogramParam.BRAND);

        assertThat(result).containsEntry("Marriott", 3L).containsEntry("Hilton", 2L);
    }

    @Test
    void getHistogram_city() {
        when(hotelRepository.countByCity()).thenReturn(List.of(new HistogramEntry("Minsk", 10L)));

        Map<String, Long> result = hotelService.getHistogram(HistogramParam.CITY);

        assertThat(result).containsEntry("Minsk", 10L);
    }

    @Test
    void getHistogram_country() {
        when(hotelRepository.countByCountry()).thenReturn(List.of(new HistogramEntry("Belarus", 10L)));

        Map<String, Long> result = hotelService.getHistogram(HistogramParam.COUNTRY);

        assertThat(result).containsEntry("Belarus", 10L);
    }

    @Test
    void getHistogram_amenities() {
        when(hotelRepository.countByAmenity()).thenReturn(List.of(new HistogramEntry("Free WiFi", 10L)));

        Map<String, Long> result = hotelService.getHistogram(HistogramParam.AMENITIES);

        assertThat(result).containsEntry("Free WiFi", 10L);
    }

    @Test
    void getAllHotels_emptyPage_returnsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Hotel> emptyPage = Page.empty(pageable);

        when(hotelRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<HotelBriefDto> result = hotelService.getAllHotels(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchHotels_allNullFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(hotel), pageable, 1);

        when(hotelRepository.findAll(
                ArgumentMatchers.<Specification<Hotel>>any(),
                eq(pageable)
        )).thenReturn(hotelPage);
        when(hotelMapper.toBriefDto(hotel)).thenReturn(briefDto);

        Page<HotelBriefDto> result = hotelService.searchHotels(null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchHotels_withFilters_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(hotel), pageable, 1);

        when(hotelRepository.findAll(
                ArgumentMatchers.<Specification<Hotel>>any(),
                eq(pageable)
        )).thenReturn(hotelPage);
        when(hotelMapper.toBriefDto(hotel)).thenReturn(briefDto);

        Page<HotelBriefDto> result = hotelService.searchHotels(
                "Grand", "Marriott", "Minsk", "Belarus", Set.of("Free WiFi"), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Grand Hotel");
    }
}