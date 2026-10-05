/*
 * File: IReservationApprovalRepository.cs
 * Component: Transaction Repositories
 * Description:
 * Declares persistence operations used for reservation approvals.
 */

using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public interface IReservationApprovalRepository
{
    // Finds a reservation by MongoDB ID or public reservation ID.
    Task<EnergyReservation?> GetByIdOrReservationIdAsync(string id);

    // Retrieves pending reservations for the same slot and date.
    Task<IEnumerable<EnergyReservation>> GetPendingForSlotDateAsync(string slotId, DateTime reservationDate);

    // Saves changes to an existing reservation.
    Task UpdateAsync(EnergyReservation reservation);
}