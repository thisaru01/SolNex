using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public interface IReservationApprovalRepository
{
    Task<EnergyReservation?> GetByIdOrReservationIdAsync(string id);
    Task<IEnumerable<EnergyReservation>> GetPendingForSlotDateAsync(string slotId, DateTime reservationDate);
    Task UpdateAsync(EnergyReservation reservation);
}