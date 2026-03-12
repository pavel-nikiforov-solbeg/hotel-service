package com.example.hotelservice.mapper;

import com.example.hotelservice.dto.AddressDto;
import com.example.hotelservice.dto.ArrivalTimeDto;
import com.example.hotelservice.dto.ContactsDto;
import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import com.example.hotelservice.entity.Address;
import com.example.hotelservice.entity.ArrivalTime;
import com.example.hotelservice.entity.Contacts;
import com.example.hotelservice.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * MapStruct mapper for converting between {@link Hotel} entities and their DTO representations.
 *
 * <p>unmappedTargetPolicy = WARN ensures any field added to a target type without a corresponding
 * mapping produces a compiler warning, catching silent data-loss bugs early.
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface HotelMapper {

    // Create DTO to Entity

    /**
     * Maps a creation request DTO to a new {@link Hotel} entity.
     * {@code id} is excluded because it is assigned by the database.
     * {@code amenities} is excluded because {@link Hotel} initialises the list via
     * {@code @Builder.Default}; amenities are managed separately via a dedicated endpoint.
     *
     * @param dto the incoming creation payload
     * @return a transient {@link Hotel} ready to be persisted
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    Hotel toEntity(HotelCreateDto dto);

    /**
     * Maps an {@link AddressDto} to the {@link Address} embeddable.
     *
     * @param dto address data from the request
     * @return {@link Address} embeddable, or {@code null} if {@code dto} is {@code null}
     */
    Address toAddress(AddressDto dto);

    /**
     * Maps a {@link ContactsDto} to the {@link Contacts} embeddable.
     *
     * @param dto contacts data from the request
     * @return {@link Contacts} embeddable, or {@code null} if {@code dto} is {@code null}
     */
    Contacts toContacts(ContactsDto dto);

    /**
     * Maps an {@link ArrivalTimeDto} to the {@link ArrivalTime} embeddable.
     * Uses qualified mapping methods for time conversion.
     *
     * @param dto arrival time data from the request
     * @return {@link ArrivalTime} embeddable, or {@code null} if {@code dto} is {@code null}
     */
    @Mapping(target = "checkIn",  qualifiedByName = "localTimeToString")
    @Mapping(target = "checkOut", qualifiedByName = "localTimeToString")
    ArrivalTime toArrivalTime(ArrivalTimeDto dto);

    // Entity to Full DTO

    /**
     * Maps a {@link Hotel} entity to a {@link HotelFullDto} containing all fields,
     * including nested address, contacts, arrival time, and amenities.
     *
     * @param hotel the entity loaded from the database
     * @return full DTO representation, or {@code null} if {@code hotel} is {@code null}
     */
    HotelFullDto toFullDto(Hotel hotel);

    /**
     * Maps an {@link Address} embeddable to {@link AddressDto}.
     *
     * @param address the embeddable from the entity
     * @return DTO representation, or {@code null} if {@code address} is {@code null}
     */
    AddressDto toAddressDto(Address address);

    /**
     * Maps a {@link Contacts} embeddable to {@link ContactsDto}.
     *
     * @param contacts the embeddable from the entity
     * @return DTO representation, or {@code null} if {@code contacts} is {@code null}
     */
    ContactsDto toContactsDto(Contacts contacts);

    /**
     * Maps an {@link ArrivalTime} embeddable to {@link ArrivalTimeDto}.
     * Uses qualified mapping methods for time parsing.
     *
     * @param arrivalTime the embeddable from the entity
     * @return DTO representation, or {@code null} if {@code arrivalTime} is {@code null}
     */
    @Mapping(target = "checkIn",  qualifiedByName = "stringToLocalTime")
    @Mapping(target = "checkOut", qualifiedByName = "stringToLocalTime")
    ArrivalTimeDto toArrivalTimeDto(ArrivalTime arrivalTime);

    // Entity to brief DTO

    /**
     * Maps a {@link Hotel} entity to a {@link HotelBriefDto} used in list responses.
     * The structured {@link Address} embeddable is mapped directly to {@link AddressDto}
     * via {@link #toAddressDto(Address)}.
     *
     * @param hotel the entity loaded from the database
     * @return brief DTO representation, or {@code null} if {@code hotel} is {@code null}
     */
    @Mapping(target = "phone", source = "contacts.phone")
    HotelBriefDto toBriefDto(Hotel hotel);

    // Custom conversion methods

    @Named("localTimeToString")
    default String localTimeToString(LocalTime time) {
        return time != null ? time.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

    @Named("stringToLocalTime")
    default LocalTime stringToLocalTime(String time) {
        return time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")) : null;
    }
}