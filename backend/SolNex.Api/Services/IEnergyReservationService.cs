using SolNex.Api.DTOs;

namespace SolNex.Api.Services;

public interface IEnergyReservationService
{
    Task<IEnumerable<ReservationDto>> GetAllReservationsAsync();
    Task<ReservationDto?> GetReservationByIdAsync(string id);
    Task<IEnumerable<ReservationDto>> GetReservationsByNicAsync(string nic);
    Task<IEnumerable<ReservationDto>> GetPendingReservationsAsync();
    Task<ReservationDto> CreateReservationAsync(CreateReservationDto createDto);
    Task<ReservationDto?> UpdateReservationAsync(string id, UpdateReservationDto updateDto);
    Task<ReservationDto?> UpdateReservationDetailsAsync(string id, UpdateReservationDetailsDto updateDto);
    Task DeleteReservationAsync(string id);
    Task<ReservationDto?> RequestCancellationAsync(string id);
    Task<IEnumerable<ReservationDto>> SearchReservationsAsync(string? stationId, string? status);
}
