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

import java.util.StringJoiner;

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

    //Create DTO to Entity

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
     *
     * @param dto arrival time data from the request
     * @return {@link ArrivalTime} embeddable, or {@code null} if {@code dto} is {@code null}
     */
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
     *
     * @param arrivalTime the embeddable from the entity
     * @return DTO representation, or {@code null} if {@code arrivalTime} is {@code null}
     */
    ArrivalTimeDto toArrivalTimeDto(ArrivalTime arrivalTime);

    //Entity to brief DTO

    /**
     * Maps a {@link Hotel} entity to a {@link HotelBriefDto} used in list responses.
     * The structured {@link Address} embeddable is formatted into a single human-readable
     * string via {@link #formatAddress(Address)}.
     *
     * @param hotel the entity loaded from the database
     * @return brief DTO representation, or {@code null} if {@code hotel} is {@code null}
     */
    @Mapping(target = "address", source = "address", qualifiedByName = "formatAddress")
    @Mapping(target = "phone", source = "contacts.phone")
    HotelBriefDto toBriefDto(Hotel hotel);

    /**
     * Formats a structured {@link Address} embeddable into a single comma-separated string.
     * Only non-null components are included. Returns {@code null} when all components are absent.
     *
     * <p>Example: {@code "9 Main St, Minsk, 220001, Belarus"}
     *
     * @param address the address embeddable; may be {@code null}
     * @return formatted address string, or {@code null} if {@code address} is {@code null}
     *         or contains no data
     */
    @Named("formatAddress")
    default String formatAddress(Address address) {
        if (address == null) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(", ");

        String houseAndStreet = buildHouseAndStreet(address.getHouseNumber(), address.getStreet());
        if (houseAndStreet != null) {
            joiner.add(houseAndStreet);
        }
        if (address.getCity() != null) {
            joiner.add(address.getCity());
        }
        if (address.getPostCode() != null) {
            joiner.add(address.getPostCode());
        }
        if (address.getCountry() != null) {
            joiner.add(address.getCountry());
        }

        return joiner.length() > 0 ? joiner.toString() : null;
    }

    default String buildHouseAndStreet(Integer houseNumber, String street) {
        if (houseNumber == null && street == null) return null;
        if (houseNumber == null) return street;
        if (street == null) return String.valueOf(houseNumber);
        return houseNumber + " " + street;
    }
}