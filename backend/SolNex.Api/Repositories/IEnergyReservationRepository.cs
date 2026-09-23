using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public interface IEnergyReservationRepository
{
    Task<IEnumerable<EnergyReservation>> GetAllReservationsAsync();
    Task<IEnumerable<EnergyReservation>> GetReservationsByNicAsync(string nic);
    Task<IEnumerable<EnergyReservation>> GetPendingReservationsAsync();
    Task<EnergyReservation?> GetReservationByIdAsync(string id);
    Task<EnergyReservation?> GetReservationByReservationIdAsync(string reservationId);
    Task CreateReservationAsync(EnergyReservation reservation);
    Task UpdateReservationAsync(string id, EnergyReservation reservation);
    Task DeleteReservationAsync(string id);
    Task<IEnumerable<EnergyReservation>> SearchReservationsAsync(string? stationId, string? status);
}
