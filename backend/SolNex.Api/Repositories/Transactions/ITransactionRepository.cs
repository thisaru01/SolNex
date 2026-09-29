/*
 * File: ITransactionRepository.cs
 * Component: Transaction Repositories
 * Description:
 * Declares persistence operations available for energy transactions.
 */

using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public interface ITransactionRepository
{
    // Finds a transaction by MongoDB ID or public transaction ID.
    Task<EnergyTransaction?>
        GetByIdOrTransactionIdAsync(
            string id);

    // Finds a transaction by its QR token.
    Task<EnergyTransaction?>
        GetByQrTokenAsync(
            string qrToken);

    // Finds the transaction for a reservation.
    Task<EnergyTransaction?>
        GetByReservationIdAsync(
            string reservationId);

    // Retrieves transactions awaiting verification or completion.
    Task<IEnumerable<EnergyTransaction>>
        GetPendingAsync();

    // Retrieves completed transactions.
    Task<IEnumerable<EnergyTransaction>>
        GetCompletedAsync();

    // Retrieves active transactions for a Prosumer.
    Task<IEnumerable<EnergyTransaction>>
        GetActiveByProsumerNicAsync(
            string nic);

    // Persists a new transaction.
    Task CreateAsync(
        EnergyTransaction transaction);

    // Saves changes to an existing transaction.
    Task UpdateAsync(
        EnergyTransaction transaction);
}